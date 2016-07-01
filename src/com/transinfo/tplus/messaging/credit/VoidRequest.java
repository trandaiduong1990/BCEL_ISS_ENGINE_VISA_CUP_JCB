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
public class VoidRequest extends RequestBaseHandler
{


	public VoidRequest(){}

	public IParser execute(IParser objISO)throws TPlusException
	{
		TransactionDataBean objTranxBean=null;
		if (DebugWriter.boolDebugEnabled)
			DebugWriter.write("VoidRequest Start Processing :");
		System.out.println("VoidRequest Start Processing :");

		try
		{

			CardCashPurseDataBean objCardCashPurseDataBean = new CardCashPurseDataBean();
			TransactionDB objTransactionDB = new TransactionDB();

			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			objTranxBean = objISO.getTransactionDataBean();
			//objTransactionDB.checkOffusTransaction(objISO);

			// assign transaction code sub type
			objTranxBean.setTranxCodeSubType("VOID");

			Validator validator = new Validator();
			validator.addValidator(new CardNumberValidator());
			validator.addValidator(new CardValidator());

			//validator.addValidator(new PINValidator(false));

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

			TranxInfo objTranxInfo = null;

			System.out.println("Before Existing Tranx");

			String cardScheme = objISO.getCardProduct();
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("VoidRequest cardScheme :: " + cardScheme);
			
			if("CU".equals(cardScheme)){
				objTranxInfo =objTransactionDB.getExistingCUPTranx(objISO);
			}else{
				objTranxInfo = objTransactionDB.getExistingTranx(objISO.getValue(TPlusISOCode.TERMINAL_ID),objISO.getCardNumber(),objISO.getValue(TPlusISOCode.REF_NO),objISO.getValue(TPlusISOCode.APPROVAL_CODE));
			}

			//if((objTranxInfo =objTransactionDB.getExistingTranx(objISO.getValue(TPlusISOCode.TERMINAL_ID),objISO.getCardNumber(),objISO.getValue(TPlusISOCode.REF_NO),objISO.getValue(TPlusISOCode.APPROVAL_CODE))) != null)
			if(objTranxInfo != null)
			{

				String tranxCode = objTranxInfo.getTranxCode();

				if("AUTHCOMPLETE".equals(tranxCode)){

					objISO.setObjTranxInfo(objTranxInfo);

					System.out.println("com.transinfo.tplus.messaging.credit.PreAuthCompleteVoidRequest");
					PreAuthCompleteVoidRequest objPreAuthCompleteVoidRequest = new PreAuthCompleteVoidRequest();
					objPreAuthCompleteVoidRequest.execute(objISO);

					return objISO;

				}

				objCardCashPurseDataBean.setCardNo(objTranxBean.getCardNo());
				objCardCashPurseDataBean.execute();

				if(!objCardCashPurseDataBean.isRecordExist())
				{
					throw new OnlineException("09","A05374","Customer Account record not exist.");
				}

				//double amount = objTranxInfo.getAmount();
				//double amount = objTranxInfo.getTranxCardHolderAmt();
				double amount = objTranxInfo.getTranxCurrConvAmt();

				objTranxBean.setTranxCurrConvAmt(new Double(amount).toString());

				// assign the values from original transaction since CUP OFFUS void will not send these details
				if("CU".equals(objISO.getCardProduct()) && "0100".equals(objISO.getMTI())){
					objTranxBean.setTranxSettledAmt(String.valueOf(objTranxInfo.getTranxCardHolderAmt()));
					objTranxBean.setTranxCHAmt(String.valueOf(objTranxInfo.getTranxCardHolderAmt()));
					objTranxBean.setTranxFee("0.0");
					objTranxBean.setTranxSettledCurr(objTranxInfo.getTranxCardHolderCurr());

					// check original transaction POS entry mode
					/*String posEntryMode  = objTranxInfo.getPosEntryMode();
					if(("901".equals(posEntryMode) || "902".equals(posEntryMode)) && "902".equals(objTranxBean.getPOSEntryMode())){
						throw new OnlineException("55","G0001","Pos Entry mode 902");
					}*/

				}

				String orgTraceNo = objTranxInfo.getOrgTraceNo();

				// insert new trace no
				objTranxBean.setTraceNo2(orgTraceNo);

				TransactionDB objTranx = new TransactionDB();
				
				if("CU".equals(cardScheme)){
					objTranx.deleteCUPAuthLogNew(objISO);
				}else{
					objTranx.deleteAuthLog(objISO);
				}
				
				double purchaseAmount = amount;
				double cashAmount = 0;

				if("CASH".equals(tranxCode)){
					purchaseAmount = 0;
					cashAmount = amount;
				}

				// update the customer limit value
				objTranx.updateLimitUsed(objCardCashPurseDataBean.getAccountId(), purchaseAmount, cashAmount, "-",objTranxBean.getCardNo(),tranxCode);

			}
			else
			{
				System.out.println("Record NOT FOUND FOR REVERSAL");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Reversal: Record Not Found for Reversal...");
				throw new OnlineException("21","G0001","Record Not Found for Reversal");
			}


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

				if(objISO.getF44() !=null)
				{
					objISO.setValue(44,objISO.getF44());
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

				// insert limit used
				objTranxBean.setLimitUsed(objTransactionDB.getLimitUsed(objCardCashPurseDataBean.getAccountId()));

				// get the approval code
				String approvalCode = objISO.getRandomApprovalCode();

				objTranxBean.setResponseCode("00");
				objTranxBean.setRemarks("Tranx Approved");
				objTranxBean.setApprovalCode(approvalCode);

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