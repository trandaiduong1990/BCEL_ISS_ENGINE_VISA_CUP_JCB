package com.transinfo.tplus.messaging.credit;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardCashPurseDataBean;
import com.transinfo.tplus.javabean.TranxInfo;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings("static-access")
public class PreAuthCompleteReversalRequest extends RequestBaseHandler {

	public PreAuthCompleteReversalRequest() {
	}

	public IParser execute(IParser objISO) throws TPlusException {
		TransactionDataBean objTranxBean=null;

		if (DebugWriter.boolDebugEnabled)
			DebugWriter.write("Reversal Start Processing :");
		System.out.println("Reversal Start Processing :");

		try {

			TransactionDB objTransactionDB = new TransactionDB();
			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			objTranxBean = objISO.getTransactionDataBean();
			//objTransactionDB.checkOffusTransaction(objISO);

			Validator validator = new Validator();
			validator.addValidator(new CardNumberValidator());
			validator.addValidator(new CardValidator());
			validator.process(objISO);

			if (objISO.getMTI().equals("0401")) {
				if (objTransactionDB.repeatTranxExists(objISO)) {
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39, "00");
					objTransactionDB.getApprovalCode(objISO);
					objISO.setRemarks(" Repeated Transaction ");
					System.out.println("Repeated Transaction");
					return objISO;
				}
			}

			TranxInfo objpreAuthTranxInfo = null;
			CardCashPurseDataBean objCardCashPurseDataBean = new CardCashPurseDataBean();

			//if((objTranxInfo = objTransactionDB.recordExistsReversal(objISO)) != null){

			TranxInfo objTranxInfo = objISO.getObjTranxInfo();

			if(objTranxInfo == null){
				if((objTranxInfo = objTransactionDB.recordExistsReversal(objISO)) == null){
					System.out.println("Record NOT FOUND FOR REVERSAL");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Reversal: Record Not Found for Reversal...");
					throw new OnlineException("21","G0001","Record Not Found for Reversal");
				}
			}

			objCardCashPurseDataBean.setCardNo(objTranxBean.getCardNo());
			objCardCashPurseDataBean.execute();

			if(!objCardCashPurseDataBean.isRecordExist())
			{
				throw new OnlineException("09","A05374","Customer Account record not exist.");
			}

			objpreAuthTranxInfo = objTransactionDB.getPreAuthTranx(objISO, objTranxInfo.getRefNo());

			if(objpreAuthTranxInfo == null){

				System.out.println("Record Not Found for PreAuth");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Record Not Found for PreAuth");
				throw new OnlineException("21","G0001","Record Not Found for PreAuth");
			}

			//double amount = objTranxInfo.getAmount();
			//double amount = objTranxInfo.getTranxCardHolderAmt();
			double amount = objTranxInfo.getTranxCurrConvAmt();

			double preAuthAmount = objpreAuthTranxInfo.getTranxCurrConvAmt();

			objTranxBean.setTranxCurrConvAmt(new Double(amount).toString());

			String orgTraceNo = objTranxInfo.getOrgTraceNo();

			String preAuthTranxId = objpreAuthTranxInfo.getTranxLogId();

			// update the customer account will be done inside the below method
			// delete the original transaction
			objTransactionDB.updateAuthCompleteReversalAndVoid(objISO,orgTraceNo,objCardCashPurseDataBean.getAccountId(), amount, preAuthTranxId, preAuthAmount);

			// insert original trace no
			objTranxBean.setTraceNo2(objTranxInfo.getTraceNo());
			objTranxBean.setResponseCode("00");

			/*}
			else
			{
				System.out.println("Record NOT FOUND FOR REVERSAL");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Reversal: Record Not Found for Reversal...");
				throw new OnlineException("21","G0001","Record Not Found for Reversal");
			}*/


			String acqBin = objISO.getValue(32);
			System.out.println("Acq BIN :: " + acqBin);

			if(!"437733".equals(acqBin)){

				System.out.println("Before F63");
				if (objISO.hasField(63)) {
					System.out.println("Getting ISOMsg.");
					ISOMsg isomsg = (ISOMsg) objISO.getISOObject(63);
					System.out.println("Getting ISOMsg.Fld 3" + isomsg.getString(3));
					if (!isomsg.getString(3).equals("2501")
							&& !isomsg.getString(3).equals("2502")
							&& !isomsg.getString(3).equals("2503")
							&& !isomsg.getString(3).equals("2504")) {
						throw new OnlineException("05", "G0001", "F63.3 does not exists..");
					}
				}
			}

			try {

				// insert limit used
				objTranxBean.setLimitUsed(objTransactionDB.getLimitUsed(objCardCashPurseDataBean.getAccountId()));

				// get the approval code
				String approvalCode = objTranxInfo.getApprovalCode();

				objTranxBean.setResponseCode("00");
				objTranxBean.setRemarks("Tranx Approved");
				objTranxBean.setApprovalCode(approvalCode);

				WriteLogDB objWriteLogDb = new WriteLogDB();
				objWriteLogDb.updateLog(objISO.getTransactionDataBean());

				objISO.setValue(39, "00");
				objISO.setValue(38, approvalCode);

				if (DebugWriter.boolDebugEnabled)
					DebugWriter.write("Transaction Inserted....");
				System.out.println("Transaction Inserted");

			} catch (Exception e) {
				if (DebugWriter.boolDebugEnabled)
					DebugWriter.write("Error Inserting Transaction...");
				System.out.println("Error Inserting Transaction...");
				throw new OnlineException("05", "G0001", "System Error");

			}

		} catch (OnlineException cex) {
			System.out.println("exp online");
			throw new OnlineException(cex);
		}catch(TPlusException tpexe){
			System.out.println("TPlusException.....");
			throw new OnlineException(tpexe.getErrorCode(), tpexe.getStrReasonCode(), tpexe.getMessage());
		}catch (Exception exp) {
			throw new OnlineException("96", "G0001", "System Error while processing Reversal " + exp.getMessage());
		}
		System.out.println("Request Finished..");
		return objISO;

	}

}