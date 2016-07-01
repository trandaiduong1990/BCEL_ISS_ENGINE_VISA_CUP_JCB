package com.transinfo.tplus.messaging.credit.cup;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.messaging.parser.IParser;

public class ReversalRequest extends com.transinfo.tplus.messaging.credit.ReversalRequest {

	public ReversalRequest() {
	}

	public IParser execute(IParser objISO) throws TPlusException {
		super.execute(objISO);
		return objISO;
	}

}