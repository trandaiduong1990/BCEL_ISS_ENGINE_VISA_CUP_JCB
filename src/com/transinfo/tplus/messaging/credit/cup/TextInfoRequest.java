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
import java.util.TimeZone;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;

public class TextInfoRequest extends CUPRequestBaseHandler
{

	int N=1;
	String strDesc = "";
	String strConnName="";
	String strResCode="";
	public TextInfoRequest(){}

	public IParser execute(IParser objISO)throws TPlusException
	{

		try
		{
			ISOMsg objMsg = new ISOMsg();

			Date d = new Date();
			TransactionDB tranx = new TransactionDB();
			String traceNo = tranx.getTraceNo();

			objMsg.set(0,"0620");
			objMsg.set(7,ISODate.getDateTime(d,TimeZone.getTimeZone("GMT+8")));
			objMsg.set(11,traceNo);
			objMsg.set (33, "30470418");

			objMsg.set (48, "This Message is from  BCEL");
			
			objMsg.set(70,"800");

			objISO.setMsgObject(objMsg);
			objISO.setParse(true);
			objISO.setMsgType("ISO");

			strConnName=objISO.getConnectionName();
			ISOMsg issuerResObj =	sendAndReceiveDestination(objISO);


			if(issuerResObj !=null)
			{

				strResCode = issuerResObj.getString(39);
				return updateResponse(objISO,issuerResObj);
			}
			else
			{
				strDesc = " No Response from Host ";
				return null;
			}

		}
		catch(TPlusException tplusExp)
		{

			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch(Exception exp)
		{
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in Echo TRANX: "+exp);
		}
		finally
		{

		}
	}

}