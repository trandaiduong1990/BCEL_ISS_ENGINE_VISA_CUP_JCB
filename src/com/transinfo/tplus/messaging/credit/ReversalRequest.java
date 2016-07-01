package com.transinfo.tplus.messaging.credit;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardCashPurseDataBean;
import com.transinfo.tplus.javabean.TranxInfo;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.GenerateRevARPC;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings("static-access")
public class ReversalRequest extends RequestBaseHandler {

	public ReversalRequest() {
	}

	public IParser execute(IParser objISO) throws TPlusException {
		TransactionDataBean objTranxBean=null;

		if (DebugWriter.boolDebugEnabled)
			DebugWriter.write("Reversal Start Processing :");
		System.out.println("Reversal Start Processing :");

		boolean isDeleteRec = true;

		try {

			CardCashPurseDataBean objCardCashPurseDataBean = new CardCashPurseDataBean();
			TransactionDB objTransactionDB = new TransactionDB();

			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			objTranxBean = objISO.getTransactionDataBean();
			//objTransactionDB.checkOffusTransaction(objISO);

			// assign transaction code sub type
			objTranxBean.setTranxCodeSubType("REVERSAL");

			Validator validator = new Validator();
			validator.addValidator(new CardNumberValidator());
			validator.addValidator(new CardValidator());
			validator.process(objISO);

			if (("0401".equals(objISO.getMTI())) || ("0421".equals(objISO.getMTI()))) {
				if (objTransactionDB.repeatTranxExists(objISO)) {
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39, "00");
					objTransactionDB.getApprovalCode(objISO);
					objISO.setRemarks(" Repeated Transaction ");
					System.out.println("Repeated Transaction");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Repeated Tranx Successful");
					GenerateRevARPC objGenARPC = new GenerateRevARPC(false);
					objGenARPC.generateARPC(objISO);
					return objISO;
				}
			}
			
			String acqBin = objISO.getValue(32);
			System.out.println("Acq BIN :: " + acqBin);

			if(!"437733".equals(acqBin)){
				System.out.println("Before F63");
				
				if (objISO.hasField(63)) {
					
					System.out.println("Getting ISOMsg.");
					ISOMsg isomsg = (ISOMsg) objISO.getISOObject(63);
					System.out.println("Getting ISOMsg.Fld 3 :: " + isomsg.getString(3));
					
					if (isomsg.getString(3) == null || (!isomsg.getString(3).equals("2501")
							&& !isomsg.getString(3).equals("2502")
							&& !isomsg.getString(3).equals("2503")
							&& !isomsg.getString(3).equals("2504"))) {
						throw new OnlineException("05", "G0001", "F63.3 does not exists..");
					}
				}
				
			}

			TranxInfo objTranxInfo = null;

			if((objTranxInfo = objTransactionDB.recordExistsReversal(objISO)) != null){

				String isAuthComVoid = objTranxInfo.getIsAuthComVoid();

				if("Y".equals(isAuthComVoid)){

					objISO.setObjTranxInfo(objTranxInfo);

					System.out.println("com.transinfo.tplus.messaging.credit.PreAuthCompleteReversalRequest");
					PreAuthCompleteReversalRequest objAuthCompleteReversalRequest = new PreAuthCompleteReversalRequest();
					objAuthCompleteReversalRequest.execute(objISO);

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
				String orgTraceNo = objTranxInfo.getOrgTraceNo();

				String tranxCode = objTranxInfo.getTranxCode();
				
				// set curr conversion amount - amount+fee
				objTranxBean.setTranxCurrConvAmt(new Double(amount).toString());

				// check CUP OFFUS void reversal
				if("CU".equals(objISO.getCardProduct()) && "VOID".equals(tranxCode) && "0100".equals(objTranxInfo.getMTI())){
					objTranxBean.setTranxSettledAmt(String.valueOf(objTranxInfo.getTranxCardHolderAmt()));
					objTranxBean.setTranxCHAmt(String.valueOf(objTranxInfo.getTranxCardHolderAmt()));
					objTranxBean.setTranxFee("0.0");
					objTranxBean.setTranxSettledCurr(objTranxInfo.getTranxCardHolderCurr());
				}

				// check partial reversal
				if(objISO.hasField(95)){

					System.out.println("Field 95 available " + objISO.getValue(95));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Field 95 available " + objISO.getValue(95));

					String actAmtTra = objISO.getValue(95).substring(0, 12);
					double lActAmtTra = (Double.valueOf(actAmtTra).doubleValue()/100);

					System.out.println("lActAmtTra " + lActAmtTra);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("lActAmtTra " + lActAmtTra);

					String f4 = objISO.getValue(4);
					double lF4 = (Double.valueOf(f4).doubleValue());

					if(lActAmtTra > 0){

						if(lActAmtTra < lF4){

							String actAmtBill = objISO.getValue(95).substring(24, 36);
							double dActAmtBill = (Double.valueOf(actAmtBill).doubleValue())/100;

							System.out.println("dActAmtBill " + dActAmtBill);
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("dActAmtBill " + dActAmtBill);
							
							double currConvAmt =0.0;

							if(dActAmtBill > 0){

								if(("0100".equals(objTranxInfo.getMTI()) || "0120".equals(objTranxInfo.getMTI())) && !"840".equals(objISO.getValue(49))){
									currConvAmt = (dActAmtBill * objISO.getCardDataBean().getCurrConvFee())/100;
									if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Amount ConvAmount"+amount+"    "+currConvAmt);
								}
								
								// amount to credit back to customer
								amount = amount-(dActAmtBill+currConvAmt);

								// don't delete original transaction
								isDeleteRec = false;

							}

							System.out.println("amount " + amount);
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("amount " + amount);

							// set partial amount value
							objTranxBean.setPartialAmt(String.valueOf(dActAmtBill));
							objTranxBean.setTranxCurrConvAmt(new Double(dActAmtBill+currConvAmt).toString());

						}else{
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("Actual Amount is greater than F4 on F95");
							throw new OnlineException("05", "Actual Amount is greater than F4 on F95", "Actual Amount is greater than F4 on F95");
						}

					}

				}

				if("PREAUTH".equals(tranxCode)){

					//double purchaseAmount = amount;
					//double cashAmount = 0;

					// for pre auth no void. only reversal

					// update customer account
					//objTransactionDB.updatePreAuth(objCardCashPurseDataBean.getAccountId(), purchaseAmount, cashAmount, "-");

					// update preauth tranx table
					objTransactionDB.updatePreAuthTranx(objTranxInfo.getTranxLogId(), "R");

				}

				// update the customer account will be done inside the below method
				// delete the original transaction
				//objTransactionDB.updateReversalAndVoid(objISO,orgTraceNo,objCardCashPurseDataBean.getAccountId(), amount, tranxCode);

				// this is with partial amount. NOT delete if partial
				objTransactionDB.updateReversalAndVoidPartial(objISO,orgTraceNo,objCardCashPurseDataBean.getAccountId(), amount, tranxCode, isDeleteRec);


				// insert original trace no
				objTranxBean.setTraceNo2(objTranxInfo.getTraceNo());
				objTranxBean.setResponseCode("00");

			}
			else
			{
				System.out.println("Record NOT FOUND FOR REVERSAL");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Reversal: Record Not Found for Reversal...");
				throw new OnlineException("00","G0001","Record Not Found for Reversal");
			}

			try {

				// insert limit used
				objTranxBean.setLimitUsed(objTransactionDB.getLimitUsed(objCardCashPurseDataBean.getAccountId()));

				// get the approval code
				String approvalCode = objTranxInfo.getApprovalCode();

				objTranxBean.setResponseCode("00");
				objTranxBean.setRemarks("Tranx Approved");
				
				if("JC".equals(objISO.getCardProduct())){
					
					String reqF39 = objISO.getValue(39);
					
					// over write
					objTranxBean.setRemarks("Tranx Approved_Req F39 " + reqF39);
					
				}
								
				objTranxBean.setApprovalCode(approvalCode);


				objISO.setValue(39, "00");
				GenerateRevARPC objGenARPC = new GenerateRevARPC(false);
				objGenARPC.generateARPC(objISO);


				WriteLogDB objWriteLogDb = new WriteLogDB();
				objWriteLogDb.updateLog(objISO.getTransactionDataBean());

				objISO.setValue(39, "00");
				objISO.setValue(38, approvalCode);

				// only for testing.  need to remove
				//objISO.unset(41);
				//objISO.unset(37);
				// end

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