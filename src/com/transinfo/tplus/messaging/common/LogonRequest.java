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

package com.transinfo.tplus.messaging.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.messaging.OnlineUtil;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;

@SuppressWarnings({"unused","deprecation"})
public class LogonRequest extends RequestBaseHandler
{


	public LogonRequest(){}

	public IParser execute(IParser objISO)throws TPlusException
	{

		try
		{

			System.out.println("Processing Code From getValue="+(String)objISO.getValue(0));

			ISOMsg isoMap = objISO.clone();

			/*try
		{
        	 validateAndUpdateMessage(objISO,"9000000000");
	 	}
	 	catch(TPlusException tplus)
	 	{
			System.out.println(tplus);
			String errCode = tplus.getErrorCode();
			System.out.println(errCode);
			if(errCode.length()>2)
			objISO.setValue(39,"96");
			else
			objISO.setValue(39,errCode);
			return objISO;
	     }*/

			TransactionDB objTranx = new TransactionDB();
			String masterKey = objTranx.getTerminalMK(objISO.getValue(41));

			if(masterKey == null)
			{
				throw new TPlusException(TPlusCodes.NO_MASTER_KEY," For Teminal:"+objISO.getValue(41));
			}

			String terminalWorkingKey = "1111111111111111";

			//String issuerWorkingKey = objTranx.getLogonIssuerWK();

			/*if(issuerWorkingKey == null)
		{
			throw new TPlusException(TPlusCodes.NO_ISSUER_WKEY," For Teminal:"+objISO.getValue(41));
		}*/


			try
			{


				byte[] encryptedWorkingKey  = OnlineUtil.encrypt(ISOUtil.hex2byte(masterKey), ISOUtil.hex2byte(terminalWorkingKey));
				terminalWorkingKey = ISOUtil.hexString(encryptedWorkingKey);

				System.out.println(masterKey+"   "+"   "+terminalWorkingKey);

				if(terminalWorkingKey!=null && terminalWorkingKey.length()>0)
				{

					//arow.setAttribute("WorkingKey",issuerWorkingKey);
					objISO.setValue(38,"000000");
					objISO.setValue(39,"00");

					Date now = new Date(System.currentTimeMillis());
					SimpleDateFormat df = new SimpleDateFormat("DDDHH");
					SimpleDateFormat df1 = new SimpleDateFormat("HHmmss");
					String refNo = "" + (now.getYear() % 10) + df.format(now) + df1.format(now);
					objISO.setValue(37, refNo);
					//responseMsg.set(53, eKTM); comment by Hiep
					objISO.setBinaryValue(53, ISOUtil.hex2byte(terminalWorkingKey)); // for BCEL only

					if(objTranx.storeTerminalWK(objISO.getValue(41),terminalWorkingKey,"123") < 0)
					{
						objISO.setValue(39,"96");
						return objISO;
					}

					System.out.println("TerminalWorking Stored");

				}
				else
				{
					objISO.setValue(39,"05");

				}

			}catch (Exception nfe)
			{

				System.out.println("Exp="+nfe);
				objISO.setValue(38,"000000");
				objISO.setValue(39,"00");
				Date now = new Date(System.currentTimeMillis());
				SimpleDateFormat df = new SimpleDateFormat("DDDHH");
				SimpleDateFormat df1 = new SimpleDateFormat("HHmmss");
				String refNo = "" + (now.getYear() % 10) + df.format(now) + df1.format(now);
				objISO.setValue(37, refNo);
				objISO.setBinaryValue(53, ISOUtil.hex2byte(masterKey));

			}


			return objISO;

		}
		catch(TPlusException tplusExp)
		{
			System.out.println("Exception while execute Sale Request.."+tplusExp.getMessage());
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch(Exception exp)
		{
			System.out.println("Exception while Risk Management Check.."+exp);
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in SALE TRANX: "+exp.getMessage());
		}
		finally
		{

			objISO.setTranxLog(false);
		}

	}

}