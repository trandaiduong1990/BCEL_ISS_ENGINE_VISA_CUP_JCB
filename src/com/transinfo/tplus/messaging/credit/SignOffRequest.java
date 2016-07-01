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

package com.transinfo.tplus.messaging.credit;

import java.util.Date;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusConfig;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.log.SignOnLog;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.monitor.MonitorLogListener;

public class SignOffRequest extends VisaRequestBaseHandler
{


	String strDesc = "";
	String strConnName="";
	String strResCode="";
	public SignOffRequest(){}

	public IParser execute(IParser objISO)throws TPlusException
	{
		int N=1;

		try
		{
			ISOMsg objMsg = new ISOMsg();

			System.out.println("Processing for Singoff Request");
			Date d = new Date();
			TransactionDB tranx = new TransactionDB();
			String traceNo = tranx.getTraceNo();

			objMsg.set(0,"0800");
			objMsg.set(7,ISODate.getDateTime(d));
			objMsg.set(11,traceNo);

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
			};

			objMsg.set(70,"072");

			objISO.setMsgObject(objMsg);
			objISO.setParse(true);
			objISO.setMsgType("ISO");

			strConnName=objISO.getConnectionName();
			ISOMsg issuerResObj =	sendAndReceiveDestination(objISO);


			if(issuerResObj !=null)
			{

				strResCode = issuerResObj.getString(39);
				
				// update sign on status
				if("00".equals(strResCode)){
					tranx.UpdateSignOn(strConnName,"N");
					
					//after sign off get the sign on lists
					TPlusConfig.connectionMap = tranx.getSignOnList();
					
				}
				
				return updateResponse(objISO,issuerResObj);
			}
			else
			{
				/*// 07-10-2015 ONLY for TESTING
				tranx.UpdateSignOn(strConnName,"N");
				
				//after sign off get the sign on lists
				TPlusConfig.connectionMap = tranx.getSignOnList();
				//
				*/
				strDesc = " No Response from Host ";
				return null;
			}



		}
		catch(TPlusException tplusExp)
		{
			System.out.println("Exception while execute SignOff Request.."+tplusExp.getMessage());
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch(Exception exp)
		{
			System.out.println("Exception while SignOff Request ."+exp);
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in SignOff TRANX: "+exp.getMessage());
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
					mintorListener.sendSystemLog("SingoffRequest for "+strConnName+" Successfull", MonitorLogListener.BLUE);
				}
				else
				{
					mintorListener.sendSystemLog("SingoffRequest for "+strConnName+" Not Successfull", MonitorLogListener.RED);
				}

			}
			catch(Exception exp)
			{
				System.out.println("Error at finally in SingOffRequest "+exp);
			}



		}
	}

}