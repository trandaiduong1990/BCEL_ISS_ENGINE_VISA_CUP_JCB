
package com.transinfo.tplus.messaging.validator;


import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;

public class ISOValidator
{


	public boolean validateCard(IParser objISO)throws TPlusException
	{

		String strCardNo =  objISO.getCardNumber();
		if( strCardNo== null || strCardNo.equals(""))
			return false;
		//objISO.setValue(39,"14");
		//throw new TPlusException(TPlusCodes.INVALID_CARDNUMBER,TPlusCodes.getErrorDesc(TPlusCodes.INVALID_CARDNUMBER)+". Error:Invalid Card No from Terminal:"+objISO.getValue(TPlusISOCode.TERMINAL_ID));
		return true;


	}

	public boolean validateExpiry(IParser objISO )throws TPlusException,Exception
	{

		String strExpDate = objISO.getExpiryDate();
		if(strExpDate == null || strExpDate.equals(""))
			return false;
		//return objISO.setValue(39,"14");
		//throw new TPlusException(TPlusCodes.INVALID_CARDNUMBER,TPlusCodes.getErrorDesc(TPlusCodes.INVALID_CARDNUMBER)+". Error:Invalid Card Expiry Date from Terminal:"+objISO.getValue(TPlusISOCode.TERMINAL_ID));
		return true;


	}


	public boolean validate(IParser objISO )throws TPlusException
	{
		String strSYSTraceNo =	objISO.getValue(TPlusISOCode.SYSTEM_TRACE_NO);
		if( strSYSTraceNo == null || strSYSTraceNo.equals(""))
			return false;
		//return objISO.setValue(39,"05");
		//throw new TPlusException(TPlusCodes.DO_NOT_HONOUR,TPlusCodes.getErrorDesc(TPlusCodes.DO_NOT_HONOUR)+". Error:System Trace No is null or invalid from terminal:"+objISO.getValue(TPlusISOCode.TERMINAL_ID));
		return true;


	}


}