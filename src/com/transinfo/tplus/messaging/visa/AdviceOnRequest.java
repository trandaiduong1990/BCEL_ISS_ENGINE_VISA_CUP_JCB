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

package com.transinfo.tplus.messaging.visa;

import java.util.Date;
import java.util.TimeZone;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCalendar;
import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.log.SignOnLog;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.monitor.MonitorLogListener;

public class AdviceOnRequest extends VisaRequestBaseHandler
{


	String strDesc = "";
	String strConnName="";
	String strResCode="";
	public AdviceOnRequest(){}

	public IParser execute(IParser objISO)throws TPlusException
	{
		int N=1;
		try
		{
			ISOMsg objMsg = new ISOMsg();
			ISOMsg issuerResObj = null;


			System.out.println("Processing for AdviceOn Request");
			Date d = new Date();
			TransactionDB tranx = new TransactionDB();
			String traceNo = tranx.getTraceNo();

			objMsg.set(0,"0800");

			TPlusCalendar tcalendar = new TPlusCalendar();

			objMsg.set(7,ISODate.getDateTime(new Date(), TimeZone.getTimeZone("GMT")));
			objMsg.set(11,traceNo);
			objMsg.set(70,"078");

			objISO.setMsgObject(objMsg);
			objISO.setParse(true);
			objISO.setMsgType("ISO");


			TransactionDB objTranx = new TransactionDB();
			String jDate = objTranx.getJulianDate();

			if(N==9)
			  N=0;

			  if (jDate.length()<11 && N<9)
			  {
				objMsg.set(37, jDate+"0"+(++N));
				objISO.setRefNo(jDate+"0"+N);
			  }
			else
			{
			  objMsg.set(37, jDate+""+(++N));
			  objISO.setRefNo(jDate+""+N);
			}



			strConnName=objISO.getConnectionName();
			issuerResObj =	sendAndReceiveDestination(objISO);


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
				System.out.println("Exception while execute SignOn Request.."+tplusExp.getMessage());
				objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
				throw tplusExp;

		}catch(Exception exp)
		{
				System.out.println("Exception while SignOn Request ."+exp);
				throw new TPlusException(TPlusCodes.APPL_ERR," Error: in SignOn TRANX: "+exp.getMessage());
		}
		finally
		{
				try
				{

					SignOnLog signlog = new SignOnLog();
					signlog.addLogItem(strConnName,strResCode,strDesc);
					//WriteLogDB.updateLog(signlog);
					MonitorLogListener mintorListener = MonitorLogListener.getMonitorLogListener(8005);
					if(strResCode.equals("00"))
					{
						mintorListener.sendSystemLog("SingonRequest for "+strConnName+" Successfull", MonitorLogListener.BLUE);
					}
					else
					{
						mintorListener.sendSystemLog("SingonRequest for "+strConnName+" Not Successfull", MonitorLogListener.RED);
					}

				}
				catch(Exception exp)
				{
					System.out.println("Error at finally in SingOnRequest "+exp);
			 	}



		}
	}

	public static void main(String s[])
	{
		System.out.println("in static");
 	}

}