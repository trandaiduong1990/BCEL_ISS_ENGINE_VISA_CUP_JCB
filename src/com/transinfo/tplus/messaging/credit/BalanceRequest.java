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
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.messaging.validator.BlackListCardValidator;
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
import com.transinfo.tplus.util.StringUtil;

/**
 * @author WCCTTI-NISHAN
 *
 */
@SuppressWarnings("static-access")
public class BalanceRequest extends RequestBaseHandler {

	IParser objISO = null;

	public BalanceRequest(){}

	/* (non-Javadoc)
	 * @see com.transinfo.tplus.messaging.RequestBaseHandler#execute(com.transinfo.tplus.messaging.parser.IParser)
	 */
	public IParser execute(IParser objISO)throws TPlusException {

		TransactionDataBean objTranxBean=null;

		if (DebugWriter.boolDebugEnabled) DebugWriter.write("BalanceRequest: Start Processing.....");

		try {

			System.out.println("In Balance Request");

			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			cloneISO.dump(new PrintStream(System.out), "0");
			TransactionDB objTranxDB = new TransactionDB();

			try {

				objTranxBean = objISO.getTransactionDataBean();
				//objTranxBean =objTranxDB.checkOffusTransaction(objISO);

				// assign transaction code sub type
				objTranxBean.setTranxCodeSubType("ENQUIRY");

				Validator validator = new Validator();
				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());

				// add SIGN ON validation
				validator.addValidator(new SignOnValidator());

				validator.addValidator(new DataValidator());
//				validator.addValidator(new TrackValidator());
//				validator.addValidator(new CVVOnTrack2Validator());
//				validator.addValidator(new CVVOnTrack1Validator());
//				validator.addValidator(new CVV2Validator(false));

				// add new card status validator
				validator.addValidator(new CardStatusValidator());

				boolean PINValidateStatus = false;

				String mcc = objISO.getValue(TPlusISOCode.MERCHANT_TYPE);
				if("6011".equalsIgnoreCase(mcc)){
					PINValidateStatus = true;
				}

				validator.addValidator(new EMVValidator(false));
				//validator.addValidator(new PINValidator(PINValidateStatus));
				validator.addValidator(new BlackListCardValidator());

				validator.process(objISO);

			} catch(OnlineException exp) {
				System.out.println("Exception=="+exp);
				throw new OnlineException(exp);
			}

			if(objISO.getMTI().equals("0101")) {

				if(objTranxDB.repeatTranxExists(objISO)) {

					objISO.setMsgObject(cloneISO);
					objISO.setValue(39,"00");
					objISO.setRemarks(" Repeated Transaction ");
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);

					if (DebugWriter.boolDebugEnabled) DebugWriter.write("BalanceRequest: Repeated Transaction...");
					return objISO;
				}

			}

			System.out.println("BalanceRequest: Card Validation is Successful");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("BalanceRequest: Card Validation is Successful");

			//objISO =  setProcessingCode(objISO);

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("BalanceRequest: Processing Code Set "+objISO.getValue(3));

			CardCashPurseDataBean objCardCashPurseDataBean = new CardCashPurseDataBean();
			objCardCashPurseDataBean.setCardNo(objTranxBean.getCardNo());
			objCardCashPurseDataBean.execute();

			if(!objCardCashPurseDataBean.isRecordExist()) {
				throw new OnlineException("09","A05374","Customer Account record not exist.");
			}

			//double balance = Double.valueOf(objCardCashPurseDataBean.getBalance()).doubleValue()*100;

			//String F54=StringUtil.RPAD(String.valueOf(balance), 12, "0");

			String fromAccType = objISO.getValue(3).substring(2, 4);

			String balISOFormat = StringUtil.getISOAmtNew(objCardCashPurseDataBean.getBalance());
			String avaiBal = fromAccType+"01840C"+balISOFormat;
			String currBal = fromAccType+"02840C"+balISOFormat;
			
			//String currBalJCB = fromAccType+"02840<"+balISOFormat;
			String currBalJCB = fromAccType+"02840C"+balISOFormat;

			//String F54="C"+StringUtil.getISOAmtNew(objCardCashPurseDataBean.getBalance());
			String cardScheme = objISO.getCardProduct();

			String F54=avaiBal+currBal;
			
			// JCb supports one part
			if("JC".equals(cardScheme)){
				F54=currBalJCB;
			}
			
			System.out.println("F54 = "+F54);

			objISO.setValue(54,F54);

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
				if(objISO.getStrF61() != null && !"".equals(objISO.getStrF61())) {
					objISO.setValue(61,objISO.getStrF61());
				}

			}

			try {

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

				// get the approval code
				String approvalCode = objISO.getRandomApprovalCode();

				String resCode = "00";
				if("JC".equals(cardScheme)){
					resCode = "00";
				}

				objTranxBean.setResponseCode(resCode);
				objTranxBean.setRemarks("Tranx Approved");

				// to CUP request no need approval code
				if(!"CU".equals(cardScheme)){
					objTranxBean.setApprovalCode(approvalCode);
					objISO.setValue(38, approvalCode);
				}

				// to CUP transaction no need to send fields 57, 61
				if("CU".equals(cardScheme)){
					objISO.unset(57);
					objISO.unset(61);
				}

				objISO.setValue(39, resCode);
				GenerateARPC objGenARPC = new GenerateARPC(false);
				objGenARPC.generateARPC(objISO);

				WriteLogDB objWriteLogDb = new WriteLogDB();
				objWriteLogDb.updateLog(objISO.getTransactionDataBean());

				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
				System.out.println("Transaction Inserted");

			} catch(Exception e) {
				e.printStackTrace();
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
				System.out.println("Error Inserting Transaction...");
				throw new OnlineException("96","G0001","System Error");
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