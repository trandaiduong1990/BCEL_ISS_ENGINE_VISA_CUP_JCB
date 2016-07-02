package com.transinfo.tplus.messaging.credit;

import java.io.PrintStream;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardCashPurseDataBean;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.GenerateARPC;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.messaging.validator.BlackListCardValidator;
import com.transinfo.tplus.messaging.validator.BlackListMerchantValidator;
import com.transinfo.tplus.messaging.validator.CVV2Validator;
import com.transinfo.tplus.messaging.validator.CVVOnTrack1Validator;
import com.transinfo.tplus.messaging.validator.CVVOnTrack2Validator;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardStatusValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.DataValidator;
import com.transinfo.tplus.messaging.validator.EMVValidator;
import com.transinfo.tplus.messaging.validator.PINValidator;
import com.transinfo.tplus.messaging.validator.SignOnValidator;
import com.transinfo.tplus.messaging.validator.TrackValidator;
import com.transinfo.tplus.messaging.validator.Validator;

/**
 * The Class CashRequest.
 */
@SuppressWarnings("static-access")
public class CashRequest extends RequestBaseHandler {

	/** The obj iso. */
	IParser objISO = null;

	/**
	 * Instantiates a new cash request.
	 */
	public CashRequest(){}

	/* (non-Javadoc)
	 * @see com.transinfo.tplus.messaging.RequestBaseHandler#execute(com.transinfo.tplus.messaging.parser.IParser)
	 */
	public IParser execute(IParser objISO)throws TPlusException {
		TransactionDataBean objTranxBean=null;
		//CardInfo objCardInfo=null;


		if (DebugWriter.boolDebugEnabled) DebugWriter.write("CashRequest: Start Processing.....");

		try
		{
			System.out.println("In Cash Request");

			TransactionDB objDB = new TransactionDB();

			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);
			cloneISO.dump(new PrintStream(System.out), "0");
			
			TransactionDB objTranxDB = new TransactionDB();

			try
			{

				objTranxBean = objISO.getTransactionDataBean();
				//objTranxBean =objTranxDB.checkOffusTransaction(objISO);
				
				// assign transaction code sub type
				objTranxBean.setTranxCodeSubType("CASH");

				Validator validator = new Validator();
				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());

				// add SIGN ON validation
				validator.addValidator(new SignOnValidator());

				validator.addValidator(new DataValidator());
				validator.addValidator(new TrackValidator());
//				validator.addValidator(new CVVOnTrack2Validator());
//				validator.addValidator(new CVVOnTrack1Validator());
//				validator.addValidator(new CVV2Validator(false));
				validator.addValidator(new EMVValidator(false));
				
				// add new card status validator
				validator.addValidator(new CardStatusValidator());

				boolean PINValidateStatus = false;

				String mcc = objISO.getValue(TPlusISOCode.MERCHANT_TYPE);
				if("6011".equalsIgnoreCase(mcc)){
					PINValidateStatus = true;
				}

//				validator.addValidator(new PINValidator(PINValidateStatus));

				validator.addValidator(new BlackListCardValidator());
				validator.addValidator(new BlackListMerchantValidator());

				validator.process(objISO);
			}
			catch(OnlineException exp)
			{
				System.out.println("Exception=="+exp);
				throw new OnlineException(exp);
			}

			if(("0101".equals(objISO.getMTI())) || ("0201".equals(objISO.getMTI())) || ("0121".equals(objISO.getMTI())))
			{
				if(objTranxDB.repeatTranxExists(objISO))
				{
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39,"00");
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);
					objISO.setRemarks(" Repeated Transaction ");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("CashRequest: Repeated Transaction...");
					return objISO;
				}

			}

			System.out.println("CashRequest: Card Validation is Successful");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("CashRequest: Card Validation is Successful");

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("CashRequest: Processing Code Set "+objISO.getValue(3));

			CardCashPurseDataBean objCardCashPurseDataBean = new CardCashPurseDataBean();
			objCardCashPurseDataBean.setCardNo(objTranxBean.getCardNo());
			objCardCashPurseDataBean.execute();

			if(!objCardCashPurseDataBean.isRecordExist())
			{
				throw new OnlineException("09","A05374","Customer Account record not exist.");
			}

			//double amount = (Double.valueOf(objISO.getValue(4)).doubleValue())/100;
			double amount = (Double.valueOf(objISO.getTransactionDataBean().getAmount()).doubleValue())/100;

			double currConvAmt =0.0;

			if(("0100".equals(objISO.getMTI()) || "0120".equals(objISO.getMTI())) && !"840".equals(objISO.getValue(49))){
				currConvAmt = (amount * objISO.getCardDataBean().getCurrConvFee())/100;
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Amount ConvAmount"+amount+"    "+currConvAmt);
			}

			amount = amount+currConvAmt;

			//TransactionDataBean objTranxDataBean =objISO.getTransactionDataBean();
			//objTranxDataBean.setTranxCurrConvAmt(new Double(amount).toString());

			objTranxBean.setTranxCurrConvAmt(new Double(amount).toString());

			System.out.println(objISO.getCardDataBean() + " " + objISO.getValue(18) + " " + "CASH" + " " + amount + " " + objISO.getValue(49) + " " + objISO.getCardDataBean().getIssuerId());

			boolean isDomTranx = true;
			
			String acqCountryCode = objTranxBean.getAcqCountryCode();
			/*String acqCountryCode = objISO.getValue(19);

			// JCB F19 is NOT Support. So getting from F61
			if(objISO.getValue(19) == null && ("JC".equals(objISO.getCardProduct()))){
				acqCountryCode = objISO.getValue(61).substring(3,6);
				
				// JCB ACQ country code assign
				objTranxBean.setAcqCountryCode(acqCountryCode);
			}*/

			if(!"418".equals(acqCountryCode))
			{
				isDomTranx = false;
			}

			boolean limitCheck = objDB.withdrawalLimitCheck(objISO.getCardDataBean(), objISO.getValue(18), "CASH", amount, objISO.getValue(49), objISO.getCardDataBean().getIssuerId(),isDomTranx);

			double purchaseAmount = 0;
			double cashAmount = amount;

			if(limitCheck){

				try
				{
					int res = objDB.updateLimitUsed(objCardCashPurseDataBean.getAccountId(), purchaseAmount, cashAmount, "+",objTranxBean.getCardNo(),"CASH");

					if(res > 0){

						if("VI".equals(objISO.getCardProduct())){

							if(objISO.getF44() !=null)
							{
								objISO.setValue(44,objISO.getF44());
							}

						} else if("JC".equals(objISO.getCardProduct())){

							if(objISO.getStrJcbF44() !=null && !"".equals(objISO.getStrJcbF44()))
							{
								objISO.setValue(44,objISO.getStrJcbF44());
							}

						}

						// if ACQ send F61 only need to send F61 on response
						String reqF61 = objISO.getValue(61);
						if(reqF61 != null && !"".equals(reqF61)){

							// assign field 61 to CUP transactions
							if(objISO.getStrF61() != null && !"".equals(objISO.getStrF61()))
							{
								objISO.setValue(61,objISO.getStrF61());
							}

						}

						// assign F14 to CUP transaction if not available on request
						if("CU".equals(objISO.getCardProduct())){
							String reqExpDate = objISO.getValue(14);
							if(reqExpDate == null || "".equals(reqExpDate)){
								String expDate = objISO.getCardDataBean().getExpDate().substring(2, 4) + objISO.getCardDataBean().getExpDate().substring(0, 2);
								objISO.setValue(14, expDate);
							}
						}

						// insert new trace no
						objTranxBean.setTraceNo2(objISO.getValue(11));

						// insert limit used
						objTranxBean.setLimitUsed(objDB.getLimitUsed(objCardCashPurseDataBean.getAccountId()));

						// get the approval code
						String approvalCode = objISO.getRandomApprovalCode();

						objTranxBean.setResponseCode("00");
						objTranxBean.setRemarks("Tranx Approved");
						
						if(("JC".equals(objISO.getCardProduct())) && ("0120".equals(objISO.getMTI()) || "0121".equals(objISO.getMTI()))){
							
							String f60 = objISO.getValue(60);
							String f60Asc = ISOUtil.ebcdicToAscii(ISOUtil.hex2byte(f60));
							
							// over write
							objTranxBean.setRemarks("Tranx Approved_STIP F60 " + f60Asc);
							
							// stand in process approved transaction will have approval code.
							if(objISO.getValue(38) != null){
								approvalCode = objISO.getValue(38);
							}
							
						}
						objTranxBean.setApprovalCode(approvalCode);

						objISO.setValue(39, "00");
						GenerateARPC objGenARPC = new GenerateARPC(false);
						objGenARPC.generateARPC(objISO);

						WriteLogDB objWriteLogDb = new WriteLogDB();
						objWriteLogDb.updateLog(objISO.getTransactionDataBean());

						objISO.setValue(38, approvalCode);


						if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
						System.out.println("Transaction Inserted");

					}else{

						if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Updateing Customer account...");
						System.out.println("Error Updateing Customer account...");
						throw new OnlineException("96","G0001","System Error");
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
					System.out.println("Error Inserting Transaction...");
					throw new OnlineException("96","G0001","System Error");
				}

			}else{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
				throw new OnlineException("61", "Exceeded Total Credit Limit", "Exceeded Total Credit Limit");
			}

		}catch(OnlineException cex){
			System.out.println("exp online");
			throw new OnlineException(cex);
		}catch(TPlusException tpexe){
			System.out.println("TPlusException.....");
			throw new OnlineException(tpexe.getErrorCode(), tpexe.getStrReasonCode(), tpexe.getMessage());
		}catch(Exception ge){
			System.out.println("exp");
			ge.printStackTrace();
			throw new OnlineException("96","G0001","System Error");
		}
		System.out.println("*** Returning OBJISO ****");
		return objISO;
	}


}