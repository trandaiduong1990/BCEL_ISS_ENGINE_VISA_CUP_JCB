package com.transinfo.tplus.messaging.visa;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.Validator;

public class ReversalRequest extends RequestBaseHandler {


	public ReversalRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("Sale Reversal Start Processing :");
		System.out.println("Sale Reversal Start Processing :");

		try
		{
			TransactionDB objTranx = new TransactionDB();
			ISOMsg cloneISO = objISO.clone();
			objISO.setCloneISO(cloneISO);

			objTranx.checkOffusTransaction(objISO);

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
					return objISO;
				}
				else
				{
					objISO.setMTI("0400");
				}
			}

			int recStatus = objTranx.recordExists("REVERSAL",objISO);

			System.out.println("Record Status =="+recStatus);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("Record Status =="+recStatus);

			/*if(recStatus == 2)// Record doesn't exists
		{
			throw new OnlineException("00","A05374","Record does not exists for Reversal"); //21 Res
		}
		else if(recStatus == 1)// Already Reversed
		{
			objISO.setMsgObject(cloneISO);
			objISO.setValue(39,"00");
			return objISO;
		}*/
			System.out.println("Before F63 in reversal Request..");
			//because of Some string casting fro iso msg - @Jana from Laos

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

			System.out.println("passed HasField(63) ISOMsg.Fld 3  ");
			objISO =  setProcessingCode(objISO);
			System.out.println("set processing code in reversal ");
			if(objISO.hasField(95))
			{
				objISO.setValue(4,objISO.getValue(95).substring(0,12));
			}


			System.out.println("----------------- ReversalRequest: Processing Code Set ----------------");

			ISOMsg objRes = sendAndReceiveDestination(objISO);
			if(objRes!=null)
			{
				if(objRes.getValue(39).equals("00"))
				{
					System.out.println("Reversal Is Successful for terminal"+objISO.getValue(41));
					TransactionDB objTranxDB = new TransactionDB();
					objTranxDB.deleteLog(objISO);
				}

				objISO.setMsgObject(cloneISO);
				System.out.println("objISO.getValue(22)"+objISO.getValue(22));
				objISO= updateResponse(objISO,objRes);

				try
				{

					WriteLogDB objWriteLogDb = new WriteLogDB();
					objWriteLogDb.updateLog(objISO.getTransactionDataBean());
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
					System.out.println("Transaction Inserted -Reversal");
					/*objISO.getTransactionDataBean().setGLType("D");
					objTranx.insertDebitGL(objISO.getTransactionDataBean());
					System.out.println("Transaction Inserted into Debit GL");*/
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
				System.out.println("obj response null so returning null");

				return null;
			}
			/*  if(objRes!=null) {

                if(objRes.getValue(39).equals("00")) {
System.out.println("REversal Is Successful for terminal"+objISO.getValue(41));
                    TransactionDB objTranxDB = new TransactionDB();
                    objTranxDB.deleteFailedLog(objISO);
                    objISO.setMsgObject(cloneISO);
                    objISO.setValue(38,objRes.getString(38));
                    objISO.setValue(39,objRes.getString(39));

                    System.out.println("Validation Successful");

                }

            }
            else {
                return null;
            }*/


		}catch(OnlineException cex){
			throw new OnlineException(cex);
		}

		catch(Exception exp) {
			System.out.println("Throwing exception with res code 96..");
			throw new OnlineException("96","G0001","System Error while processing Reversal "+exp.getMessage());
		}
		System.out.println("Reversal Request Finished..");
		return objISO;

	}

}