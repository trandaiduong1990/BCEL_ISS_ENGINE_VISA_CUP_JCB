package com.transinfo.tplus.messaging.debit;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
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

	public ReversalRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		TransactionDataBean objTranxBean=null;
		
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("Sale Reversal Start Processing :");
		System.out.println("Sale Reversal Start Processing :");

		try
		{
			TransactionDB objTranx = new TransactionDB();
			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			objTranxBean = objISO.getTransactionDataBean();
			//objTranx.checkOffusTransaction(objISO);

			Validator validator = new Validator();
			validator.addValidator(new CardNumberValidator());
			validator.addValidator(new CardValidator());
			validator.process(objISO);

			if(objISO.getMTI().equals("0401"))
			{
				if(objTranx.repeatTranxExists(objISO))
				{
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39,"00");
					objTranx.getApprovalCode(objISO);
					objISO.setRemarks(" Repeated Transaction ");
					System.out.println("Repeated Transaction");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Repeated Tranx Successful");
					GenerateRevARPC objGenARPC = new GenerateRevARPC(false);
					objGenARPC.generateARPC(objISO);
					return objISO;
				}
			}

			TranxInfo objTranxInfo = objTranx.recordExistsReversal(objISO);

			String orgTraceNo = objISO.getValue(11);
			String traceNo2 = objISO.getValue(11);

			if(objTranxInfo != null){
				
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Reversal Tranx found and assigning original trace no & trace no 2.");
				
				orgTraceNo = objTranxInfo.getOrgTraceNo();
				traceNo2 = objTranxInfo.getTraceNo();
				
				objISO.setObjTranxInfo(objTranxInfo);
				
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("objTranxInfo.getOrgTraceNo() :: " + objTranxInfo.getOrgTraceNo());
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("objTranxInfo.getTraceNo() :: " + objTranxInfo.getTraceNo());
				
			}else{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Reversal Tranx not found but sending to CB");
			}

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("orgTraceNo :: " + orgTraceNo);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("traceNo2 :: " + traceNo2);

			System.out.println("Before F63 in reversal Request..");
			//because of Some string casting fro iso msg - @Jana from Laos

			String acqBin = objISO.getValue(32);
			System.out.println("Acq BIN :: " + acqBin);

			if(!"437733".equals(acqBin)){

				if(objISO.hasField(63))
				{
					System.out.println("Getting ISOMsg.");

					ISOMsg isomsg = (ISOMsg)objISO.getISOObject(63);
					System.out.println("Getting ISOMsg.Fld 3  ");
					System.out.println("Getting ISOMsg.Fld 3 : "+ isomsg.getValue(3));

					System.out.println("Getting ISOMsg.Fld 3 : "+ isomsg.getString(3));
					System.out.println("After ISOMsg.Fld 3  ");
					if(!isomsg.getString(3).equals("2501") && !isomsg.getString(3).equals("2502") && !isomsg.getString(3).equals("2503") && !isomsg.getString(3).equals("2504"))
						//if(!isomsg.getValue(3).equals("2501") && !isomsg.getValue(3).equals("2502") && !isomsg.getValue(3).equals("2503") && !isomsg.getValue(3).equals("2504"))

					{
						System.out.println("Throwing Online exception..... ");
						throw new OnlineException("05","G0001","F63.3 does not exists..");
					}
				}
			}

			System.out.println("passed HasField(63) ISOMsg.Fld 3  ");
			objISO =  setProcessingCode(objISO);
			System.out.println("set processing code in reversal ");
			if(objISO.hasField(95))
			{
				objISO.setValue(4,objISO.getValue(95).substring(0,12));
			}


			System.out.println("----------------- ReversalRequest: Processing Code Set ----------------");

			objISO.unset(55);

			ISOMsg objRes = sendAndReceiveDestination(objISO);
			if(objRes!=null)
			{
				if(objRes.getValue(39).equals("00"))
				{
					System.out.println("Reversal Is Successful for terminal :: "+objISO.getValue(41));
					//objTranx.deleteLogDebit(objISO);
					objTranx.deleteLogDebitVoidReversal(objISO, orgTraceNo);
				}

				objISO.setMsgObject(cloneISO);
				System.out.println("objISO.getValue(22)"+objISO.getValue(22));
				objISO= updateResponse(objISO,objRes);

				try
				{
					
					objTranxBean.setTraceNo2(traceNo2);

					GenerateRevARPC objGenARPC = new GenerateRevARPC(false);
					objGenARPC.generateARPC(objISO);

					WriteLogDB objWriteLogDb = new WriteLogDB();
					objWriteLogDb.updateLog(objISO.getTransactionDataBean());
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
					System.out.println("Transaction Inserted -Reversal");
				
				}
				catch(Exception e)
				{
					e.printStackTrace();
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
					System.out.println("Error Inserting Transaction...");
					throw new OnlineException("05","G0001","System Error");

				}

			}else {

				objISO.getTransactionDataBean().setResponseCode("-1");
				objISO.getTransactionDataBean().setRemarks("No Response From CB. Timeout");

				WriteLogDB objWriteLogDb = new WriteLogDB();
				objWriteLogDb.updateLog(objISO.getTransactionDataBean());
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
				System.out.println("Transaction Inserted");

				return null;
			}

		}catch(OnlineException cex){
			throw new OnlineException(cex);
		}catch(Exception exp) {
			System.out.println("Throwing exception with res code 96..");
			throw new OnlineException("96","G0001","System Error while processing Reversal "+exp.getMessage());
		}
		
		System.out.println("Reversal Request Finished..");
		return objISO;

	}

}