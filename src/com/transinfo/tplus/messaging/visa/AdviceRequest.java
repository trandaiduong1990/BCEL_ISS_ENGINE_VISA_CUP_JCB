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

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;

public class AdviceRequest extends VisaRequestBaseHandler
{


	public AdviceRequest(){}

	public IParser execute(IParser objISO)throws TPlusException
	{

		try
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("AdviceRequest: Processing Topup Tranx...");
			ISOMsg cloneISO = objISO.clone();

		/*try
		{
        	 validateAndUpdateMessage(objISO,"9000000000");
	 	}
	 	catch(TPlusException tplus)
	 	{
			objISO.setMsgObject(cloneISO);
			System.out.println(tplus);
			String errCode = tplus.getErrorCode();
			System.out.println(errCode);
			if(errCode.length()>2)
			objISO.setValue(39,"96");
			else
			objISO.setValue(39,errCode);
			return objISO;
	     }*/
			objISO.setValue(3,"221000");
			objISO.setValue(2, objISO.getCardNumber());
			objISO.setValue(28,"D02000000");
			objISO.setValue(63,"2002");
			objISO.setValue(90,"020000000000000000001234567890100000000000");
			//objISO.setValue(14, objISO.getExpiryDate());


			ISOMsg issuerResObj =	sendAndReceiveDestination(objISO);

			objISO.setMsgObject(cloneISO);


			if(issuerResObj !=null)
			{
				return updateResponse(objISO,issuerResObj);
			}
			else
			{
				objISO = updateErrResponse(objISO,cloneISO);
				return null;
			}



		}
		catch(TPlusException tplusExp)
		{
			System.out.println("Exception while execute Sale Request.."+tplusExp.getMessage());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Error in process TopUp Tranx...");
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch(Exception exp)
		{
			System.out.println("Exception while Sale Request.."+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("SaleRequest: Error in process TopUp Tranx...");
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in SALE TRANX: "+exp.getMessage());
		}



	}

}