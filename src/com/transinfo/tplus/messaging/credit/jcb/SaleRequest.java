package com.transinfo.tplus.messaging.credit.jcb;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.messaging.parser.IParser;

public class SaleRequest extends com.transinfo.tplus.messaging.credit.SaleRequest {

	public SaleRequest(){}
	
	public IParser execute(IParser objISO)throws TPlusException {
		System.out.println("Nishandan JCB Sale Test");
		super.execute(objISO);
		return objISO;
		//return null;
	}

}
