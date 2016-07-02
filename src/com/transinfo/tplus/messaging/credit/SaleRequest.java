package com.transinfo.tplus.messaging.credit;

import java.io.PrintStream;
import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardCashPurseDataBean;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.GenerateARPC;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.validator.BlackListCardValidator;
import com.transinfo.tplus.messaging.validator.BlackListMerchantValidator;
import com.transinfo.tplus.messaging.validator.CVV2Validator;
import com.transinfo.tplus.messaging.validator.CVVOnTrack1Validator;
import com.transinfo.tplus.messaging.validator.CVVOnTrack2Validator;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardStatusValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.DataValidator;
import com.transinfo.tplus.messaging.validator.EComValidator;
import com.transinfo.tplus.messaging.validator.EMVValidator;
import com.transinfo.tplus.messaging.validator.PINValidator;
import com.transinfo.tplus.messaging.validator.SignOnValidator;
import com.transinfo.tplus.messaging.validator.TrackValidator;
import com.transinfo.tplus.messaging.validator.TranxValidator;
import com.transinfo.tplus.messaging.validator.Validator;
import com.transinfo.tplus.util.StringUtil;

@SuppressWarnings("static-access")
public class SaleRequest extends RequestBaseHandler {

	//IParser objISO = null;

	public SaleRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		TransactionDataBean objTranxBean=null;
		//CardInfo objCardInfo=null;


		if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Start Processing.....");

		try
		{
			System.out.println("In SaleRequest");

			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);
			cloneISO.dump(new PrintStream(System.out), "0");
			TransactionDB objTranxDB = new TransactionDB();

			String tranxType = "SALE";

			try
			{

				objTranxBean = objISO.getTransactionDataBean();

				// assign transaction code sub type
				objTranxBean.setTranxCodeSubType("SALE");

				Validator validator = new Validator();

				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());

				// add Transaction validator. validating sub type
				validator.addValidator(new TranxValidator());

				// add SIGN ON validation
				validator.addValidator(new SignOnValidator());

				validator.addValidator(new DataValidator());
				validator.addValidator(new TrackValidator());
//				validator.addValidator(new CVVOnTrack2Validator());
//				validator.addValidator(new CVVOnTrack1Validator());
//				validator.addValidator(new EMVValidator(false));
				
//				validator.addValidator(new PINValidator(false));

				validator.addValidator(new EComValidator());

				// add new card status validator
				validator.addValidator(new CardStatusValidator());

				validator.addValidator(new BlackListCardValidator());
				validator.addValidator(new BlackListMerchantValidator());

//				validator.addValidator(new CVV2Validator(false));

				validator.process(objISO);

				if(objISO.isEComTranx()){
					tranxType = "ECOMM";
				}

				// check transaction type financial or account verification
				//if(!(new Double(objISO.getValue(4)).doubleValue()>0))
				if(objISO.isAccountVerification())
				{

					WriteLogDB objWriteLogDb = new WriteLogDB();

					if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Account verification request");
					// this is account verification request

					if(objISO.getF44() !=null)
					{
						objISO.setValue(44,objISO.getF44());
					}

					objTranxBean.setTraceNo2(objISO.getValue(11));

					// get the approval code
					String approvalCode = objISO.getRandomApprovalCode();

					String responseCode = "00";

					if("VI".equals(objISO.getCardProduct())){

						objTranxBean.setApprovalCode(approvalCode);
						objISO.setValue(38, approvalCode);

						responseCode = "85";

					}else if("CU".equals(objISO.getCardProduct())){
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("CU ACCVERFY");
						responseCode = "00";

						// get the order no from F48
						String f48 = objISO.getValue(48);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("f48 :: "+f48);
						
						f48 = new String(ISOUtil.hex2byte(f48));
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("f48 :: "+f48);


						if (DebugWriter.boolDebugEnabled) DebugWriter.write("before parseCUPF48");
						Map<String, String> f48Map = TPlusUtility.parseCUPF48(f48);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("after parseCUPF48");
						String orderNo = f48Map.get("ON");
						System.out.println("Order No :: " + orderNo);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("orderNo :: "+orderNo);

						String ao = f48Map.get("AO");
						System.out.println("AO :: " + ao);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("ao :: "+ao);

						// get the key from F61 AM tag
						String f61 = objISO.getValue(61);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("f61 :: "+f61);

						String key = f61.split("AM")[1].substring(19,39);
						key = key.trim();

						String customerId = objISO.getCardDataBean().getCustomerId();
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("customerId :: "+customerId);

						// mobile number of customer
						String mobileNo = objTranxDB.getMobileNo(customerId);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("mobileNo :: "+mobileNo);

						if(mobileNo != null && !"".equals(mobileNo)){

							String mobileNoRes = mobileNo.substring(mobileNo.length()-4);
							String res57 = "ASSE" + "023" + StringUtil.getSpace(3) + StringUtil.getSpecialChars(mobileNo.length()-4, "*") + mobileNoRes + StringUtil.getSpace(20-mobileNo.length());

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("res57 :: "+res57);
							// get the random number
							String dvc = objISO.getDataVerificationCode();			
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("dvc :: "+dvc);				

							objTranxBean.setKey(key);
							objTranxBean.setOrderNo(orderNo);
							objTranxBean.setDataVerificationCode(dvc);
							objTranxBean.setMobileNo(mobileNo);

							// insert into SMS DB
							try{

								objWriteLogDb.insertSMSDVC(objISO.getTransactionDataBean());
								responseCode = "00";

								// assign mobile number and card status
								objISO.setValue(57, res57);

							}catch(Exception e){

								responseCode = "05";

								e.printStackTrace();
								if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting DVC Transaction...");
								System.out.println("Error Inserting DVC Transaction...");
								throw new OnlineException("05","G0001","Error Inserting DVC Transaction...");

							}

						}else{

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("Mobile No NOT configured");
							System.out.println("Mobile No NOT configured");
							throw new OnlineException("P1","G0001","Mobile No NOT configured");

						}

						// if ACQ send F61 only need to send F61 on response
						String reqF61 = objISO.getValue(61);
						if(reqF61 != null && !"".equals(reqF61))
						{
							// assign field 61 to CUP transactions
							if(objISO.getStrF61() != null && !"".equals(objISO.getStrF61()))
							{
								objISO.setValue(61,objISO.getStrF61());
							}

						}

					}

					objTranxBean.setResponseCode(responseCode);
					objTranxBean.setRemarks("No reason to decline. Verification Request");

					objISO.setValue(39, responseCode);
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);

					objWriteLogDb.updateLog(objISO.getTransactionDataBean());

					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
					System.out.println("Transaction Inserted");

					return objISO;

				}

			}
			catch(OnlineException exp)
			{
				System.out.println("Exception=="+exp);
				throw new OnlineException(exp);
			}

			if(("0101".equals(objISO.getMTI())) || ("0121".equals(objISO.getMTI())))
			{
				if(objTranxDB.repeatTranxExists(objISO))
				{
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39,"00");
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);
					objISO.setRemarks(" Repeated Transaction ");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Repeated Transaction...");
					return objISO;
				}
			}

			System.out.println("CashRequest: Card Validation is Successful");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Card Validation is Successful");

			//objISO =  setProcessingCode(objISO);

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Processing Code Set "+objISO.getValue(3));

			CardCashPurseDataBean objCardCashPurseDataBean = new CardCashPurseDataBean();
			objCardCashPurseDataBean.setCardNo(objTranxBean.getCardNo());
			objCardCashPurseDataBean.execute();

			if(!objCardCashPurseDataBean.isRecordExist())
			{
				throw new OnlineException("09","A05374","Customer Account record not exist.");
			}

			//double amount = (Double.valueOf(objISO.getValue(4)).doubleValue())/100;
			//double amount = (Double.valueOf(objISO.getTransactionDataBean().getAmount()).doubleValue())/100;
			double amount = (Double.valueOf(objISO.getTransactionDataBean().getAmount()).doubleValue())/100;
			double currConvAmt =0.0;

			if(("0100".equals(objISO.getMTI()) || "0120".equals(objISO.getMTI())) && !"840".equals(objISO.getValue(49))){
				currConvAmt = (amount * objISO.getCardDataBean().getCurrConvFee())/100;
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Amount ConvAmount"+amount+"    "+currConvAmt);
			}

			amount = amount+currConvAmt;
			/*
			TransactionDataBean objTranxDataBean =objISO.getTransactionDataBean();
			objTranxDataBean.setTranxCurrConvAmt(new Double(amount).toString());*/

			objTranxBean.setTranxCurrConvAmt(new Double(amount).toString());

			System.out.println(objISO.getCardDataBean() + " " + objISO.getValue(18) + " " + tranxType + " " + currConvAmt+"  "+amount + " " + objISO.getValue(49) + " " + objISO.getCardDataBean().getIssuerId());

			boolean isDomTranx = true;

			String acqCountryCode = objTranxBean.getAcqCountryCode();

			if(!"418".equals(acqCountryCode))
			{
				isDomTranx = false;
			}


			// check CUP eCom transaction and validate the DVC
			if("CU".equals(objISO.getCardProduct()) && objISO.isEComTranx()){

				// check eCom mode
				String f60 = objISO.getValue(60);
				if(f60 != null && !"".equals(f60)){

					String eci = f60.substring(12,14);
					if("09".equals(eci)){

						if (DebugWriter.boolDebugEnabled) DebugWriter.write("Amount :: " + amount );

						if(amount <= 10){

							// as per BCEL request No need Account Verification request for less than 10 USD transactions
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("as per BCEL request No need Account Verification request for less than 10 USD transactions :: " + amount );

						}else{

							boolean res = objTranxDB.eComTranxCheck(objISO);

							// ONLY FOR REVERSAL TESTING
							//boolean res = true;
							//

							if(!res){
								if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error on objTranxDB.eComTranxCheck(objISO)");
								throw new OnlineException("05", "G50001", "eComTranxCheck failed");
							}

						}

					}

				}

			}

			boolean limitCheck = objTranxDB.withdrawalLimitCheck(objISO.getCardDataBean(), objISO.getValue(18), tranxType, amount, objISO.getValue(49), objISO.getCardDataBean().getIssuerId(), isDomTranx);

			double purchaseAmount = amount;
			double cashAmount = 0;

			if(limitCheck){

				try
				{
					int res = objTranxDB.updateLimitUsed(objCardCashPurseDataBean.getAccountId(), purchaseAmount, cashAmount, "+",objTranxBean.getCardNo(),"SALE");

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

							if("901".equals(objISO.getValue(22))){
								objISO.unset(57);
								objISO.unset(61);
							}

							// Recurring F57 send back on Response
							if("INSTAL".equals(objISO.getTransactionDataBean().getTranxCodeSubType()))
							{
								String res57 = "ASIP" + "064" + "000000001200" + "840" + "000000000000" + "000000000000" + "0" + "000000000000" + "000000000000";
								objISO.setValue(57, res57);
							}

						}

						// update some data
						//updateData(objISO);

						// insert limit used
						objTranxBean.setLimitUsed(objTranxDB.getLimitUsed(objCardCashPurseDataBean.getAccountId()));

						// insert new trace no
						objTranxBean.setTraceNo2(objISO.getValue(11));

						// get the approval code
						String approvalCode = objISO.getRandomApprovalCode();

						//set response code
						String resCode = "00";

						objTranxBean.setResponseCode(resCode);
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

						objISO.setValue(39, resCode);
						GenerateARPC objGenARPC = new GenerateARPC(false);
						objGenARPC.generateARPC(objISO);

						WriteLogDB objWriteLogDb = new WriteLogDB();
						objWriteLogDb.updateLog(objISO.getTransactionDataBean());

						objISO.setValue(38, approvalCode);
						objISO.setValue(39, resCode);

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