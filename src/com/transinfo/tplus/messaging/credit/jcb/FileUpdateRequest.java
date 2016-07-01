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
import java.util.TimeZone;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;

public class FileUpdateRequest extends JCBRequestBaseHandler {

	String strDesc = "";
	String strConnName="";
	String strResCode="";

	// Field120 data
	String f120Data="";

	public FileUpdateRequest(String str){
		f120Data = str;
	}

	public IParser execute(IParser objISO)throws TPlusException {

		try {

			ISOMsg objMsg = new ISOMsg();

			Date d = new Date();
			TransactionDB tranx = new TransactionDB();
			String traceNo = tranx.getTraceNo();

			objMsg.set(0,"0302");

			objMsg.set(7,ISODate.getDateTime(d,TimeZone.getTimeZone("GMT")));
			objMsg.set(11,traceNo);
			
			objMsg.set(32, "88480000");
			objMsg.set(33, "88480000");
			
			//objMsg.set(63,"08SCPPPSEX");
			objMsg.set(63,"SCPPPSEX");
			
			objMsg.set(100, "88070000");
			
			objMsg.set(101, "6332"); // File Name
			
			//objMsg.set(120, "1356971000000007605201412"); // add
			objMsg.set(120, f120Data); // inquiry
			
			objMsg.set(127, "356970"); // Negative access code

			objISO.setMsgObject(objMsg);
			objISO.setParse(true);
			objISO.setMsgType("ISO");

			strConnName=objISO.getConnectionName();
			ISOMsg issuerResObj =	sendAndReceiveDestination(objISO);

			if(issuerResObj !=null) {
				strResCode = issuerResObj.getString(39);
				return updateResponse(objISO,issuerResObj);
			} else {
				strDesc = " No Response from Host ";
				return null;
			}

		} catch(TPlusException tplusExp) {
			System.out.println("Exception while execute EchoRequest Request.."+tplusExp.getMessage());
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;
		}catch(Exception exp) {
			System.out.println("Exception while EchoRequest Request ."+exp);
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in JCBEcho TRANX: "+exp.getMessage());
		}finally {

		}

	}

}