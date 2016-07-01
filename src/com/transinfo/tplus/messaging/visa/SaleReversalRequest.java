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

public class SaleReversalRequest extends RequestBaseHandler {


    public SaleReversalRequest(){}

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
					objISO.setRemarks(" Repeated Transaction ");
					return objISO;
				}
				else
				{
					objISO.setMTI("0400");
				}
			}

		int recStatus = objTranx.recordExists("REVERSAL",objISO);

		System.out.println("Record Status =="+recStatus);
		System.out.println("F95="+objISO.getValue(95)+"   "+objISO.getValue(95).substring(0,12));

		if(recStatus == 2)
		{
			throw new OnlineException("21","A05374","Record does not exists for Reversal");
		}
		else if(recStatus == 1)
		{
			objISO.setMsgObject(cloneISO);
			objISO.setValue(39,"00");
			return objISO;
		}

		objISO =  setProcessingCode(objISO);
		if(objISO.hasField(95))
		{
			objISO.setValue(4,objISO.getValue(95).substring(0,12));
		}

		System.out.println("----------------- WithDrawalRequest: Processing Code Set ----------------");

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

             throw new OnlineException("96","G0001","System Error while processing Revesal");
        }
System.out.println("Request Finished..");
        return objISO;

    }

}