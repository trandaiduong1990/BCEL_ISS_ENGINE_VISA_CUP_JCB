package com.transinfo.tplus.messaging.credit.jcb;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.messaging.parser.IParser;

public class ReversalRequest extends com.transinfo.tplus.messaging.credit.ReversalRequest {

	public ReversalRequest() {
	}

	public IParser execute(IParser objISO) throws TPlusException {
		
		// only for timeout testing
		//String mti = objISO.getMTI();
		//String resCode = objISO.getValue(39);
		
		super.execute(objISO);
		
		/*if("0420".equals(mti) && "68".equals(resCode)){
			return null;
		}*/
		
		return objISO;
	}

}