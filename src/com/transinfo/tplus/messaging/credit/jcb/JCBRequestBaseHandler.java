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

package com.transinfo.tplus.messaging.credit.jcb;

import java.util.Date;

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
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.mux.IMux;
import com.transinfo.tplus.messaging.parser.IParser;

/**
 * This class is extended by all the different message handler(credit/debit).
 *
 */

public abstract class JCBRequestBaseHandler extends RequestBaseHandler {

	public ISOMsg sendAndReceiveDestination(IParser objISO)throws TPlusException {

		ISOMsg objResISO = null;

		if (DebugWriter.boolDebugEnabled) DebugWriter.write("Send and Receive Destination Processing Started.....:");
		TPlusPrintOutput.printMessage("JCBRequestBaseHandler","Send and Receive Destination Processing Started.");

		try {

			DebugMsgWriter.write("******** REQUEST TO HOST ***********\n");
			DebugMsgWriter.write("Host Name="+objISO.getIssuer()+" Host IP Address="+objISO.getIssuerHost() +"Host Port="+objISO.getIssuerPort()+"\n");

			ISOMsg muxiso = objISO.getMsgObject();

			System.out.println("Connection Name = "+objISO.getConnectionName());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("JCBRequestBaseHandler : Connection Name = "+objISO.getConnectionName());

			IMux mux = (IMux)TPlusUtility.createObject(objISO.getClassName());
			mux.initialize(objISO);

			System.out.println("Initilized "+muxiso+"  "+mux);

			objResISO = mux.process(muxiso);

			System.out.println("************************************RECEIVED JCB RESPONSE**************************************");

			if(objResISO != null) {

				String issuerMsg = ISOUtil.hexString(objResISO.pack());

				if (DebugWriter.boolDebugEnabled) DebugWriter.write("JCBRequestBaseHandler:Issuer Response= "+issuerMsg);
				TPlusPrintOutput.printMessage("JCBRequestBaseHandler"," ISSUER RESPONSE="+ issuerMsg);
				TPlusPrintOutput.printMessage("\n\n************************* ISSUER RESPONSE ********************************\n\n");
				DebugMsgWriter.write("******** RESPONSE FROM HOST ***********\n");


				TPlusPrintOutput.printMessage("\n\n************************* ISSUER RESPONSE ********************************\n\n");
				TPlusPrintOutput.printMessage("JCBRequestBaseHandlerr","Returned Issuer Response Object..");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("sendAndReceiveDestination:Returned Issuer Response Object..");

			} else {
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("JCBRequestBaseHandler:Issuer Response= NULL RESPONSE");
				TPlusPrintOutput.printMessage("JCBRequestBaseHandler"," ISSUER RESPONSE= NULL RESPONSE");
			}

			//Thread.sleep(5000);

		}catch(Exception exp){
			throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: in JCBRequestBaseHandler:"+exp.toString());
		}

		System.out.println("Response send from Handler.."+objResISO);
		return objResISO;

	}

	public static void main(String s[])throws Exception	{

		Date d = new Date();
		ISOMsg m = new ISOMsg("0100");
		m.set (new ISOField (3,  "000000"));
		m.set (new ISOField (7,  ISODate.getDateTime(d)));

		// alternate way of setting fields
		m.set (11, "0001");
		m.set (12, "e");
		m.set (13, ISODate.getDate(d));

	}

}
