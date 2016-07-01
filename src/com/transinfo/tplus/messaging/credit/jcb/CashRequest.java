package com.transinfo.tplus.messaging.credit.jcb;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.messaging.parser.IParser;

public class CashRequest extends com.transinfo.tplus.messaging.credit.CashRequest {

	public CashRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {
		
		// only for timeout testing
		//String mti = objISO.getMTI();
		
		super.execute(objISO);
		
		/*if("0120".equals(mti)){
			return null;
		}*/
		
		return objISO;
		//return null;
	}

}