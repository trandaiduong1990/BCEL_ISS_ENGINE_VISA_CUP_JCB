package com.transinfo.tplus.messaging.credit;

import java.io.PrintStream;
import java.math.BigDecimal;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardCashPurseDataBean;
import com.transinfo.tplus.javabean.TranxInfo;
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
import com.transinfo.tplus.messaging.validator.PINValidator;
import com.transinfo.tplus.messaging.validator.TrackValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings("static-access")
public class PreAuthCompleteRequest extends RequestBaseHandler {

	IParser objISO = null;

	public PreAuthCompleteRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		TransactionDataBean objTranxBean=null;
		//CardInfo objCardInfo=null;


		if (DebugWriter.boolDebugEnabled) DebugWriter.write("PreAuthCompleteRequest: Start Processing.....");

		try
		{
			System.out.println("In PreAuthComplete Request");

			TransactionDB objDB = new TransactionDB();

			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			System.out.println("objISO.getTranxType() --> " +objISO.getTranxType());

			cloneISO.dump(new PrintStream(System.out), "0");
			TransactionDB objTranxDB = new TransactionDB();

			try
			{

				objTranxBean = objISO.getTransactionDataBean();
				//objTranxBean =objTranxDB.checkOffusTransaction(objISO);

				Validator validator = new Validator();
				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());
				validator.addValidator(new DataValidator());
				validator.addValidator(new TrackValidator());
				validator.addValidator(new CVVOnTrack2Validator());
				validator.addValidator(new CVVOnTrack1Validator());
				validator.addValidator(new CVV2Validator(false));

				// add new card status validator
				validator.addValidator(new CardStatusValidator());
				
				validator.addValidator(new PINValidator(false));
				validator.addValidator(new BlackListCardValidator());
				validator.addValidator(new BlackListMerchantValidator());

				validator.process(objISO);
			}
			catch(OnlineException exp)
			{
				System.out.println("Exception=="+exp);
				throw new OnlineException(exp);
			}

			if(objISO.getMTI().equals("0101"))
			{
				if(objTranxDB.repeatTranxExists(objISO))
				{
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39,"00");
					objISO.setRemarks(" Repeated Transaction ");
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);

					if (DebugWriter.boolDebugEnabled) DebugWriter.write("PreAuthCompleteRequest: Repeated Transaction...");
					return objISO;
				}

			}

			System.out.println("CashRequest: Card Validation is Successful");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PreAuthCompleteRequest: Card Validation is Successful");

			//objISO =  setProcessingCode(objISO);

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PreAuthCompleteRequest: Processing Code Set "+objISO.getValue(3));

			CardCashPurseDataBean objCardCashPurseDataBean = new CardCashPurseDataBean();
			objCardCashPurseDataBean.setCardNo(objTranxBean.getCardNo());
			objCardCashPurseDataBean.execute();

			if(!objCardCashPurseDataBean.isRecordExist())
			{
				throw new OnlineException("09","A05374","Customer Account record not exist.");
			}

			//double amount = (Double.valueOf(objISO.getValue(4)).doubleValue())/100;
			double amount = (Double.valueOf(objISO.getTransactionDataBean().getAmount()).doubleValue())/100;

			objTranxBean.setTranxCurrConvAmt(new Double(amount).toString());

			boolean isDomTranx = true;
			
			String acqCountryCode = objISO.getValue(19);
			
			// JCB F19 is NOT Support. So getting from F61
			if(objISO.getValue(19) == null && ("JC".equals(objISO.getCardProduct()))){
				acqCountryCode = objISO.getValue(61).substring(3,6);;
			}

			if(!"418".equals(acqCountryCode))
			{
				isDomTranx = false;
			}

			System.out.println(objISO.getCardDataBean() + " " + objISO.getValue(18) + " " + "SALE" + " " + amount + " " + objISO.getValue(49) + " " + objISO.getCardDataBean().getIssuerId());

			//boolean limitCheck = objDB.withdrawalLimitCheck(objISO.getCardDataBean(), objISO.getValue(18), "SALE", amount, objISO.getValue(49), objISO.getCardDataBean().getIssuerId());

			// if(limitCheck){


			TranxInfo objTranxInfo =null;

			if((objTranxInfo =objDB.getExistingTranxByApprCode(objISO.getValue(TPlusISOCode.TERMINAL_ID),objISO.getCardNumber(),objISO.getValue(TPlusISOCode.APPROVAL_CODE),"PREAUTH"))!=null)
			{
				System.out.println("Pre-Auth Record available");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Pre-Auth Record available");

				try
				{

					// check the PREAUTH transaction status
					String status = objTranxInfo.getPreAuthStatus();
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Pre-Auth Transaction Status :: " + status);

					if(!"N".equals(status)){

						System.out.println("PRE AUTH TRANX STATUS NOT 'N'");
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("PRE AUTH TRANX STATUS NOT 'N'");
						throw new OnlineException("21","G0001","PRE AUTH TRANX STATUS NOT 'N'");

					}else{

						double oriPreAuthAmt = objTranxInfo.getTranxCurrConvAmt();
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("Pre-Auth Amt :: " + oriPreAuthAmt + ", Auth Complete Amt :: " + amount);

						double extraPreAuthAmtPer = 15;

						boolean limitCheck = false;
						String sign = "+";
						double limitUsedAmt = 0;

						if(oriPreAuthAmt >= amount){
							limitCheck = true;
							sign = "-";
							limitUsedAmt = oriPreAuthAmt-amount;
						}else{

							sign = "+";
							limitUsedAmt = amount-oriPreAuthAmt;
							
							BigDecimal biglimitUsedAmt = BigDecimal.valueOf(limitUsedAmt);
							biglimitUsedAmt = biglimitUsedAmt.setScale(2, BigDecimal.ROUND_HALF_EVEN);

							if(biglimitUsedAmt.doubleValue() <= ((oriPreAuthAmt*extraPreAuthAmtPer)/100)){

								limitCheck = objDB.withdrawalLimitCheck(objISO.getCardDataBean(), objISO.getValue(18), "PREAUTH", biglimitUsedAmt.doubleValue(), objISO.getValue(49), objISO.getCardDataBean().getIssuerId(), isDomTranx);

							}else{
								System.out.println("AUTH COMPLETE EXTRA AMT IS MORE");
								if (DebugWriter.boolDebugEnabled) DebugWriter.write("AUTH COMPLETE EXTRA AMT IS MORE");
								throw new OnlineException("05","G0001","AUTH COMPLETE EXTRA AMT IS MORE");
							}

						}

						if(limitCheck){
							
							BigDecimal biglimitUsedAmt = BigDecimal.valueOf(limitUsedAmt);
							biglimitUsedAmt = biglimitUsedAmt.setScale(2, BigDecimal.ROUND_HALF_EVEN);

							//objDB.updateLimitUsed(objCardCashPurseDataBean.getAccountId(), limitUsedAmt, 0, sign,objTranxBean.getCardNo(),"PREAUTH");
							objDB.updateLimitUsed(objCardCashPurseDataBean.getAccountId(), biglimitUsedAmt.doubleValue(), 0, sign,objTranxBean.getCardNo(),"AUTHCOMPLETE");

							int res = objDB.updatePreAuthComplete(objCardCashPurseDataBean.getAccountId(), oriPreAuthAmt, "-");

							if(res > 0){

								// update preauth tranx table
								objDB.updatePreAuthTranx(objTranxInfo.getTranxLogId(), "C");

								// insert limit used
								objTranxBean.setLimitUsed(objDB.getLimitUsed(objCardCashPurseDataBean.getAccountId()));

								// get the approval code
								String approvalCode = objISO.getRandomApprovalCode();

								objTranxBean.setResponseCode("00");
								objTranxBean.setRemarks("Tranx Approved");
								objTranxBean.setApprovalCode(approvalCode);

								GenerateARPC objGenARPC = new GenerateARPC(false);
								objGenARPC.generateARPC(objISO);

								WriteLogDB objWriteLogDb = new WriteLogDB();
								objWriteLogDb.updateLog(objISO.getTransactionDataBean());

								objISO.setValue(38, approvalCode);
								objISO.setValue(39, "00");

								if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
								System.out.println("Transaction Inserted");

							}else{

								if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Updateing Customer account...");
								System.out.println("Error Updateing Customer account...");
								throw new OnlineException("96","G0001","System Error");
							}

						}else{
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
							throw new OnlineException("61", "Exceeded Total Credit Limit", "Exceeded Total Credit Limit");
						}

					}

				}catch(OnlineException cex){
					System.out.println("exp online");
					throw new OnlineException(cex);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
					System.out.println("Error Inserting Transaction...");
					throw new OnlineException("96","G0001","System Error");
				}

			}else {
				System.out.println("Record NOT FOUND FOR PRE AUTH");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Record NOT FOUND FOR PRE AUTH");
				throw new OnlineException("21","G0001","Record NOT FOUND FOR PRE AUTH");
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