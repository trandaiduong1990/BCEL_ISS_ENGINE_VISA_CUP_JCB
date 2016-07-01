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
public class WithDrawalRequest extends RequestBaseHandler {

	IParser objISO = null;

	public WithDrawalRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		ArrayList riskAction=null;
		boolean TranxValidation =true;
		TransactionDataBean objTranxBean=null;
		CardInfo objCardInfo=null;


		if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Start Processing.....");

		try
		{
			System.out.println("In Sale Request");
			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			cloneISO.dump(new PrintStream(System.out), "0");
			TransactionDB objTranxDB = new TransactionDB();

			try
			{

				objTranxBean = objISO.getTransactionDataBean();
				//objTranxBean =objTranxDB.checkOffusTransaction(objISO);

				Validator validator = new Validator();
				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());
				
				// add new card status validator
				validator.addValidator(new CardStatusValidator());
				
				validator.addValidator(new DataValidator());
				validator.addValidator(new TrackValidator());
				validator.addValidator(new CVVOnTrack2Validator());
				validator.addValidator(new CVVOnTrack1Validator());
				validator.addValidator(new CVV2Validator(false));

				boolean PINValidateStatus = false;

				String mcc = objISO.getValue(TPlusISOCode.MERCHANT_TYPE);
				if("6011".equalsIgnoreCase(mcc)){
					PINValidateStatus = true;
				}

				validator.addValidator(new PINValidator(PINValidateStatus));
				validator.addValidator(new EMVValidator(false));
				validator.addValidator(new BlackListCardValidator());
				validator.addValidator(new BlackListMerchantValidator());

				validator.process(objISO);


				System.out.println("WithdrawalLimitCheck====");
				objCardInfo = objISO.getCardDataBean();
				String currCode = getCurrencyCode(objISO);
				System.out.println("WithdrawalLimitCheck===="+objCardInfo.getCardnumber()+"  "+objCardInfo.getCardProductId()+"  "+objCardInfo.getCustomerTypeId()+"  "+objISO.getTranxType()+"  "+objISO.getAmount());

				/*if(!objTranxDB.withdrawRulesLimitCheck(objCardInfo.getCardnumber(),objCardInfo.getCardProductId(),
						objCardInfo.getCustomerTypeId(),"0000",
						objISO.getTranxType(),
						//Integer.parseInt(objISO.getAmount())/100,
						(Double.valueOf(objISO.getTransactionDataBean().getAmount()).doubleValue())/100,
						currCode,objCardInfo.getIssuerId()))
				{
					System.out.println("*** Withdrawal Limit Rules Validation Failed");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WithDrawalRequest:Withdrawal Limit Rules Validation Failed.....");

					objISO.setValue(TPlusISOCode.RESPONSE_CODE,"61");
					return objISO;
				}*/

				double amount = (Double.valueOf(objISO.getTransactionDataBean().getAmount()).doubleValue())/100;
				boolean isDomTranx = true;

				if(!"418".equals(objISO.getValue(19)))
				{
					isDomTranx = false;
				}
				boolean limitCheck = objTranxDB.withdrawalLimitCheck(objISO.getCardDataBean(), objISO.getValue(18), "CASH", amount, objISO.getValue(49), objISO.getCardDataBean().getIssuerId(),isDomTranx);

				if(!limitCheck){
					objISO.setValue(TPlusISOCode.RESPONSE_CODE,"61");
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

			System.out.println("SaleRequest: Card Validation is Successful");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Card Validation is Successful");

			objISO =  setProcessingCode(objISO);

			//if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Processing Code Set "+objISO.getValue(3));

			objISO.unset(55);

			ISOMsg objRes = sendAndReceiveDestination(objISO);

			if(objRes!=null)
			{
				objISO.setMsgObject(cloneISO);
				objISO= updateResponse(objISO,objRes);

				try
				{


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