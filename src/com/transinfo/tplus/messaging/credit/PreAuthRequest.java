package com.transinfo.tplus.messaging.credit;

import java.io.PrintStream;

import org.jpos.iso.ISOMsg;

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

@SuppressWarnings("static-access")
public class PreAuthRequest extends RequestBaseHandler {

    IParser objISO = null;

    public PreAuthRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
        TransactionDataBean objTranxBean=null;
		//CardInfo objCardInfo=null;


        if (DebugWriter.boolDebugEnabled) DebugWriter.write("PreAuthRequest: Start Processing.....");

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
				validator.addValidator(new EMVValidator(false));
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

					 if (DebugWriter.boolDebugEnabled) DebugWriter.write("CashRequest: Repeated Transaction...");
					return objISO;
				}

			}

            System.out.println("CashRequest: Card Validation is Successful");
            if (DebugWriter.boolDebugEnabled) DebugWriter.write("CashRequest: Card Validation is Successful");

           //objISO =  setProcessingCode(objISO);

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

			System.out.println(objISO.getCardDataBean() + " " + objISO.getValue(18) + " " + "SALE" + " " + amount + " " + objISO.getValue(49) + " " + objISO.getCardDataBean().getIssuerId());

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
			
			boolean limitCheck = objDB.withdrawalLimitCheck(objISO.getCardDataBean(), objISO.getValue(18), "PREAUTH", amount, objISO.getValue(49), objISO.getCardDataBean().getIssuerId(), isDomTranx);

			double purchaseAmount = amount;
			double cashAmount = 0;

	        if(limitCheck){

				try
				{
					//int res = objDB.updatePreAuth(objCardCashPurseDataBean.getAccountId(), purchaseAmount, cashAmount, "+");
					int res = objDB.updateLimitUsed(objCardCashPurseDataBean.getAccountId(), purchaseAmount, cashAmount, "+",objTranxBean.getCardNo(),"PREAUTH");

					if(res > 0){

						/*// update some data
						updateData(objISO);*/

						// insert limit used
						objTranxBean.setLimitUsed(objDB.getLimitUsed(objCardCashPurseDataBean.getAccountId()));

						// get the approval code
						String approvalCode = objISO.getRandomApprovalCode();

			    		objTranxBean.setResponseCode("00");
			    		objTranxBean.setRemarks("Tranx Approved");
			    		objTranxBean.setApprovalCode(approvalCode);

						objISO.setValue(39, "00");
			    		GenerateARPC objGenARPC = new GenerateARPC(false);
						objGenARPC.generateARPC(objISO);

						WriteLogDB objWriteLogDb = new WriteLogDB();
						objWriteLogDb.updateLog(objISO.getTransactionDataBean());

						// insert into PREAUTH_TRANX table
						objWriteLogDb.insertPreAuthTranx(objISO.getTransactionDataBean());

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