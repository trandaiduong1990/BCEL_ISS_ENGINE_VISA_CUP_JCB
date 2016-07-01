/**
 * Copyright (c) 2007-2008 Trans-Info Pte Ltd. Singapore. All Rights Reserved.
 * This work contains trade secrets and confidential material of
 * Trans-Info Pte Ltd. Singapore and its use of disclosure in whole
 * or in part without express written permission of
 * Trans-Info Pte Ltd. Singapore. is prohibited.
 * Date of Creation   : Feb 25, 2008
 * Version Number     : 1.0
 *                   Modification History:
 * Date          Version No.         Modified By           Modification Details.
 */

package com.transinfo.tplus.messaging.credit;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
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
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings("static-access")
public class PreAuthCompleteVoidRequest extends RequestBaseHandler
{


	public PreAuthCompleteVoidRequest(){}

	public IParser execute(IParser objISO)throws TPlusException
	{
		TransactionDataBean objTranxBean=null;
		if (DebugWriter.boolDebugEnabled)
			DebugWriter.write("RefundVoidRequest Start Processing :");
		System.out.println("RefundVoidRequest Start Processing :");

		try
		{

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

			System.out.println("Before Existing Tranx");
			/*if((objTranxInfo =objTransactionDB.getExistingTranx(objISO.getValue(TPlusISOCode.TERMINAL_ID),objISO.getCardNumber(),objISO.getValue(TPlusISOCode.REF_NO),objISO.getValue(TPlusISOCode.APPROVAL_CODE))) != null)
			{*/

			TranxInfo objTranxInfo = objISO.getObjTranxInfo();

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

			// insert new trace no
			objTranxBean.setTraceNo2(orgTraceNo);

			String sign = "+";
			double limitUsedAmt = 0;

			if(preAuthAmount < amount){
				sign = "-";
				limitUsedAmt = amount-preAuthAmount;
			}else{
				sign = "+";
				limitUsedAmt = preAuthAmount-amount;					
			}

			TransactionDB objTranx = new TransactionDB();

			objTranx.updateLimitUsed(objCardCashPurseDataBean.getAccountId(), limitUsedAmt, 0, sign,objTranxBean.getCardNo(),"AUTHCOMPLETE");

			objTranx.deleteAuthLog(objISO);

			// update customer account
			objTranx.updatePreAuthComplete(objCardCashPurseDataBean.getAccountId(), preAuthAmount, "+");

			// update preauth tranx table
			objTranx.updatePreAuthTranx(preAuthTranxId, "N");

			/*}
			else
			{
				System.out.println("Record NOT FOUND FOR Refund Void Request");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Reversal: Record Not Found for Reversal...");
				throw new OnlineException("21","G0001","Record Not Found for Refund Void Request");
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
				String approvalCode = objISO.getRandomApprovalCode();

				objTranxBean.setResponseCode("00");
				objTranxBean.setRemarks("Tranx Approved");
				objTranxBean.setApprovalCode(approvalCode);
				objTranxBean.setIsAuthComVoid("Y");

				WriteLogDB objWriteLogDb = new WriteLogDB();
				objWriteLogDb.updateLog(objISO.getTransactionDataBean());

				objISO.setValue(38, approvalCode);
				objISO.setValue(39, "00");

				if (DebugWriter.boolDebugEnabled)
					DebugWriter.write("Transaction Inserted....");
				System.out.println("Transaction Inserted");

			} catch (Exception e) {
				if (DebugWriter.boolDebugEnabled)
					DebugWriter.write("Error Inserting Transaction...");
				System.out.println("Error Inserting Transaction...");
				throw new OnlineException("05", "G0001", "System Error");

			}


		}
		catch(TPlusException tplusExp)
		{
			System.out.println("Exception while execute Sale Request.."+tplusExp.getMessage());
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch (OnlineException cex) {
			System.out.println("exp online");
			throw new OnlineException(cex);
		}catch(Exception exp)
		{
			System.out.println("Exception while Risk Management Check.."+exp);
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in SALE TRANX: "+exp.getMessage());
		}

		System.out.println("Request Finished..");
		return objISO;

	}

}