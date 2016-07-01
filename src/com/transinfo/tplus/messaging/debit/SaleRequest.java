package com.transinfo.tplus.messaging.debit;

import java.io.PrintStream;
import java.util.ArrayList;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardInfo;
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
import com.transinfo.tplus.messaging.validator.TrackValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings({"unused","unchecked","static-access"})
public class SaleRequest extends RequestBaseHandler {

	IParser objISO = null;

	public SaleRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		ArrayList riskAction=null;
		boolean TranxValidation =true;
		TransactionDataBean objTranxBean=null;
		CardInfo objCardInfo=null;


		if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Start Processing.....");

		try
		{
			System.out.println("In Sale Request -Debit");
			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			cloneISO.dump(new PrintStream(System.out), "0");
			TransactionDB objTranxDB = new TransactionDB();

			String tranxType = "SALE";

			try
			{

				objTranxBean = objISO.getTransactionDataBean();
				//objTranxBean =objTranxDB.checkOffusTransaction(objISO);

				Validator validator = new Validator();
				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());
				
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Before new CardStatusValidator()");
				// add new card status validator
				validator.addValidator(new CardStatusValidator());
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("After new CardStatusValidator()");

				//if(!objISO.getValue(25).equals("59"))
					if(!objISO.isEComTranx())
				{
					validator.addValidator(new DataValidator());
					validator.addValidator(new TrackValidator());
					validator.addValidator(new CVVOnTrack2Validator());
					validator.addValidator(new CVVOnTrack1Validator());
					validator.addValidator(new PINValidator(false));
					validator.addValidator(new EMVValidator(false));
				}

				validator.addValidator(new BlackListCardValidator());
				validator.addValidator(new BlackListMerchantValidator());

				validator.addValidator(new CVV2Validator(false));

				validator.process(objISO);

				String f25 = objISO.getValue(25);
				if("59".equals(f25)){
					tranxType = "ECOMM";
					//CVV2ValidateStatus = true;
				}

				System.out.println("WithdrawalLimitCheck in Sale request - Debit====");
				objCardInfo = objISO.getCardDataBean();
				String currCode = getCurrencyCode(objISO);
				System.out.println("" + "WithdrawalLimitCheck===="+ "tranx type  "+objISO.getTranxType() +" Amount "+objISO.getAmount() );

				/*if(!objTranxDB.withdrawRulesLimitCheck(objCardInfo.getCardnumber(),objCardInfo.getCardProductId(),
						objCardInfo.getCustomerTypeId(),"0000",
						objISO.getTranxType(),Integer.parseInt(objISO.getAmount())/100,currCode,objCardInfo.getIssuerId()))
				{
					System.out.println("*** Withdrawal Limit Rules Validation Failed");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WithDrawalRequest:Withdrawal Limit Rules Validation Failed.....");

					objISO.setValue(TPlusISOCode.RESPONSE_CODE,"61");
					return objISO;
				}*/

				// check the amount on Field 4
				if(!(new Double(objISO.getValue(4)).doubleValue()>0))
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Account verification request");
					// this is account verification request

					if(objISO.getF44() !=null)
					{
						objISO.setValue(44,objISO.getF44());
					}

					objTranxBean.setTraceNo2(objISO.getValue(11));

					// get the approval code
					String approvalCode = objISO.getRandomApprovalCode();

					//objTranxBean.setResponseCode("00");
					objTranxBean.setResponseCode("85");
					objTranxBean.setRemarks("No reason to decline. F38 From CACIS. Verification Request");
					objTranxBean.setApprovalCode(approvalCode);

					//objISO.setValue(39, "00");
					objISO.setValue(39, "85");
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);

					WriteLogDB objWriteLogDb = new WriteLogDB();
					objWriteLogDb.updateLog(objISO.getTransactionDataBean());

					objISO.setValue(38, approvalCode);
					//objISO.setValue(39, "00");
					objISO.setValue(39, "85");

					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
					System.out.println("Transaction Inserted");

					return objISO;

				}

			}
			catch(OnlineException exp)
			{
				System.out.println("Exception=="+exp);
				throw exp;
			}

			if(objISO.getMTI().equals("0101"))
			{
				if(objTranxDB.repeatTranxExists(objISO))
				{
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39,"00");
					objISO.setValue(38,objTranxDB.getApprovalCode(objISO));
					objISO.setRemarks(" Repeated Transaction ");
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Repeated Transaction...");
					return objISO;
				}

			}

			double amount = (Double.valueOf(objISO.getTransactionDataBean().getAmount()).doubleValue())/100;
			boolean isDomTranx = true;

			if(!"418".equals(objISO.getValue(19)))
			{
				isDomTranx = false;
			}

			boolean limitCheck = objTranxDB.withdrawalLimitCheck(objISO.getCardDataBean(), objISO.getValue(18), tranxType, amount, objISO.getValue(49), objISO.getCardDataBean().getIssuerId(), isDomTranx);

			if(!limitCheck){
				objISO.setValue(TPlusISOCode.RESPONSE_CODE,"61");
				return objISO;
			}

			System.out.println("SaleRequest: Card Validation is Successful");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Card Validation is Successful");

			objISO =  setProcessingCode(objISO);

			//if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Processing Code Set "+objISO.getValue(3));

			objISO.unset(55);

			ISOMsg objRes = sendAndReceiveDestination(objISO);

			if(objRes!=null)
			{
				try
				{
					objISO.setMsgObject(cloneISO);
					objISO= updateResponse(objISO,objRes);

					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);

					WriteLogDB objWriteLogDb = new WriteLogDB();
					objWriteLogDb.updateLog(objISO.getTransactionDataBean());
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
					System.out.println("Transaction Inserted");

				}
				catch(Exception e)
				{
					e.printStackTrace();
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
					System.out.println("Error Inserting Transaction...");
					throw new OnlineException("05","G0001","System Error");

				}

			}
			else
			{

				objTranxBean.setResponseCode("-1");
				objTranxBean.setRemarks("No Response From CB. Timeout");

				WriteLogDB objWriteLogDb = new WriteLogDB();
				objWriteLogDb.updateLog(objISO.getTransactionDataBean());
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
				System.out.println("Transaction Inserted");

				return null;
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