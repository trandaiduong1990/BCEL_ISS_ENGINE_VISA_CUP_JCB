package com.transinfo.tplus.messaging.debit;

import java.util.ArrayList;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.messaging.validator.CVV2Validator;
import com.transinfo.tplus.messaging.validator.CVVOnTrack2Validator;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardStatusValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings({"unused","unchecked"})
public class MiniStatementRequest extends RequestBaseHandler {

	IParser objISO = null;
	private final boolean HSMON = true;
	TransactionDataBean objTranxBean=null;

	public MiniStatementRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		ArrayList riskAction=null;
		boolean TranxValidation =true;

		if (DebugWriter.boolDebugEnabled) DebugWriter.write("MiniStatementRequest Start Processing :");

		try {
			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			/*if(!validateMessage(objISO)) {
                return objISO;
            }*/

			System.out.println(" Card Validation is Successful");
			// Sale transaction
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
				
				if(objISO.getCardNumber().startsWith("4"))
				{
					//validator.addValidator(new TrackValidator());
					validator.addValidator(new CVVOnTrack2Validator());
					validator.addValidator(new CVV2Validator(false));
					//validator.addValidator(new DataValidator());
				}

				//validator.addValidator(new PINValidator(false));

				validator.process(objISO);
			}
			catch(OnlineException exp)
			{
				System.out.println("Exception=="+exp);
				throw exp;
			}
			//String currCode = getCurrencyCode(objISO);
			CardInfo objCardInfo =objISO.getCardDataBean();
			String savingsAccount = objCardInfo.getSavingAcct();
			String checkingAccount = objCardInfo.getCheckingAcct();
			DebugWriter.write("savingsAccount >> " + savingsAccount);
			DebugWriter.write("checkingAccount >> " + checkingAccount);
			int cLen = 0;
			int sLen = 0;
			if(objCardInfo.getSavingAcct() != null) sLen = savingsAccount.length();
			if(objCardInfo.getCheckingAcct() != null) cLen = checkingAccount.length();

			if(objISO.getValue(3).equals("311000")) {
				if(sLen > 0) objISO.setValue(102,savingsAccount);
				else TranxValidation=false;
			}
			else if(objISO.getValue(3).equals("312000")) {
				if(cLen > 0) objISO.setValue(102,checkingAccount);
				else TranxValidation=false;
			}
			else {
				TranxValidation=false;
			}

			if(!TranxValidation){
				objISO.setValue(TPlusISOCode.RESPONSE_CODE,TPlusCodes.DO_NOT_HONOUR);
				return objISO;
			}
			objISO.setMTI("0100");
			ISOMsg objRes = sendAndReceiveDestination(objISO);

			if(objRes!=null) {
				objISO.setMsgObject(cloneISO);
				objISO.setValue(39,objRes.getString(39));
				objISO.setValue(62,objRes.getString(62));
				objISO.setValue(TPlusISOCode.APPROVAL_CODE,getApprovalCode());
			}

			System.out.println(" Response ="+ objRes.getString(39));
		}
		catch(OnlineException cex){
			System.out.println("exp online");
			throw new OnlineException(cex);
		}
		catch(TPlusException tplusExp) {
			System.out.println("Exception while execute MiniStatementRequest.."+tplusExp.getMessage());
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch(Exception exp) {
			System.out.println("Exception while MiniStatementRequest.."+exp);
			throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: in MiniStatement TRANX:"+exp.getMessage());
		}

		return objISO;

	}


}