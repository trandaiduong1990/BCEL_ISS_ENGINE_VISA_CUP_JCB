package com.transinfo.tplus.messaging.credit.jcb;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.messaging.parser.IParser;

public class BalanceRequest extends com.transinfo.tplus.messaging.credit.BalanceRequest {

    public BalanceRequest(){
    	
    }

	public IParser execute(IParser objISO)throws TPlusException {
		super.execute(objISO);
		return objISO;
	}

}