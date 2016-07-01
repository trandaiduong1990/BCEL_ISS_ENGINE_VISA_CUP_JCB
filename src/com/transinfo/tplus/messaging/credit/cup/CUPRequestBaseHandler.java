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

package com.transinfo.tplus.messaging.credit.cup;


import java.util.Date;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.TPlusPrintOutput;
import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.debug.DebugMsgWriter;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.mux.IMux;
import com.transinfo.tplus.messaging.parser.IParser;



/*
To be done in this class

1. The merchant currency to be validated
2. Check other validation to be taken
3. PIN transformation to be done
4. Field 126 should be properly formated and put in the ISO msg
 */



/**
 * This class is extended by all the different message handler(credit/debit).
 *
 */

@SuppressWarnings("unused")
public abstract class CUPRequestBaseHandler extends RequestBaseHandler
{

	public CardInfo objCardInfo;
	private static int N=0;
	Date now = new Date(System.currentTimeMillis());

	//public abstract byte[] execute(TPlusISOParser objISO,String Header)throws TPlusException;

	//public abstract IParser execute(IParser objISO)throws TPlusException;

	public ISOMsg sendAndReceiveDestination(IParser objISO)throws TPlusException
	{
		byte[] resByte=null;
		boolean TranxValidation =true;
		ISOMsg objResISO = null;

		if (DebugWriter.boolDebugEnabled) DebugWriter.write("Send and Receive Destination Processing Started.....:");
		TPlusPrintOutput.printMessage("VisaRequestBaseHandler","Send and Receive Destination Processing Started.");

		try
		{


			DebugMsgWriter.write("******** REQUEST TO HOST ***********\n");
			DebugMsgWriter.write("Host Name="+objISO.getIssuer()+" Host IP Address="+objISO.getIssuerHost() +"Host Port="+objISO.getIssuerPort()+"\n");


			ISOMsg muxiso = objISO.getMsgObject();

			System.out.println("88888888888"+objISO.getConnectionName());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("VisaRequestBaseHandler:Connection Name= "+objISO.getConnectionName());
			//IMux  mux = MuxFactory.getMux(objISO.getConnectionName());
			IMux mux = (IMux)TPlusUtility.createObject(objISO.getClassName());
			mux.initialize(objISO);
			System.out.println("Initilized "+muxiso+"  "+mux);
			objResISO = mux.process(muxiso);

			System.out.println("************************************RECEIVED CUP RESPONSE**************************************");



			if(objResISO != null)
			{
				String issuerMsg = ISOUtil.hexString(objResISO.pack());

				if (DebugWriter.boolDebugEnabled) DebugWriter.write("VisaRequestBaseHandler:Issuer Response= "+issuerMsg);
				TPlusPrintOutput.printMessage("VisaRequestBaseHandler"," ISSUER RESPONSE="+ issuerMsg);
				TPlusPrintOutput.printMessage("\n\n************************* ISSUER RESPONSE ********************************\n\n");
				DebugMsgWriter.write("******** RESPONSE FROM HOST ***********\n");


				TPlusPrintOutput.printMessage("\n\n************************* ISSUER RESPONSE ********************************\n\n");
				TPlusPrintOutput.printMessage("VisaRequestBaseHandler","Returned Issuer Response Object..");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("sendAndReceiveDestination:Returned Issuer Response Object..");
			}
			else
			{

				if (DebugWriter.boolDebugEnabled) DebugWriter.write("VisaRequestBaseHandler:Issuer Response= NULL RESPONSE");
				TPlusPrintOutput.printMessage("VisaRequestBaseHandler"," ISSUER RESPONSE= NULL RESPONSE");

			}

			//Thread.sleep(5000);

		}catch(Exception exp)
		{
			throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: in VisaRequestBaseHandler:"+exp.toString());
		}

		System.out.println("Response send from Handler.."+objResISO);
		return objResISO;

	}





	public static void main(String s[])throws Exception
	{

		Date d = new Date();
		ISOMsg m = new ISOMsg("0100");
		m.set (new ISOField (3,  "000000"));
		m.set (new ISOField (7,  ISODate.getDateTime(d)));

		// alternate way of setting fields
		m.set (11, "0001");
		m.set (12, "e");
		m.set (13, ISODate.getDate(d));
		ISOChannel clientSideChannel=null;

		/*IMux visamux = new VisaMux();
		        //visamux.initialize();
		        ISOMsg resmsg =visamux.process(m);

				if(resmsg == null)
				{
					System.out.println("NO Response");
				}
				else
				{
					System.out.println("Response received");
				}*/

	}

}
