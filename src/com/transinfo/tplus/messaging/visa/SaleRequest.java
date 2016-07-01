package com.transinfo.tplus.messaging.visa;

import java.io.PrintStream;
import java.util.ArrayList;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.DataValidator;
import com.transinfo.tplus.messaging.validator.PINValidator;
import com.transinfo.tplus.messaging.validator.Validator;

public class SaleRequest extends RequestBaseHandler {

    IParser objISO = null;

    public SaleRequest(){}

    public IParser execute(IParser objISO)throws TPlusException {
        ArrayList riskAction=null;
        boolean TranxValidation =true;
        TransactionDataBean objTranxBean=null;
        CardInfo objCardInfo=null;


        if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Start Processing.....");

        try
        {
			System.out.println("In Sale Request");

            ISOMsg cloneISO = objISO.clone();
            objISO.setCloneISO(cloneISO);

            cloneISO.dump(new PrintStream(System.out), "0");
            TransactionDB objTranxDB = new TransactionDB();

			try
			{

				objTranxBean = objISO.getTransactionDataBean();
		    	objTranxBean =objTranxDB.checkOffusTransaction(objISO);

				Validator validator = new Validator();
				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());
				validator.addValidator(new DataValidator());
				//validator.addValidator(new TrackValidator());
				
				//validator.addValidator(new CVVOnTrack2Validator());
				//validator.addValidator(new CVVOnTrack1Validator());
				//validator.addValidator(new CVV2Validator(false));
				validator.addValidator(new PINValidator(false));

				validator.process(objISO);
			}
			catch(OnlineException exp)
			{
				System.out.println("Exception=="+exp);
				throw exp;
			}

			if(objISO.getMTI().equals("0101"))
			{
				if(objTranxDB.repeatTranxExists(objISO))
				{
					objISO.setMsgObject(cloneISO);
					objISO.setValue(39,"00");
					objISO.setValue(38,objTranxDB.getApprovalCode(objISO));
					objISO.setRemarks(" Repeated Transaction ");
					 if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Repeated Transaction...");
					return objISO;
				}
				else
				{
					objISO.setMTI("0200");
				}
			}

            System.out.println("SaleRequest: Card Validation is Successful");
            if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Card Validation is Successful");

           //objISO =  setProcessingCode(objISO);

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Processing Code Set "+objISO.getValue(3));

			ISOMsg objRes = sendAndReceiveDestination(objISO);

			if(objRes!=null)
			{
				objISO.setMsgObject(cloneISO);
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

    }catch(OnlineException cex){
		System.out.println("exp online");
        throw new OnlineException(cex);
    }catch(Exception ge){
		System.out.println("exp");
        ge.printStackTrace();
        throw new OnlineException("96","G0001","System Error");

    }
        System.out.println("*** Returning OBJISO ****");

        return objISO;

    }


}