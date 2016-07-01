package com.transinfo.tplus.messaging.credit.cup;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.messaging.parser.IParser;

public class RefundReversalRequest extends com.transinfo.tplus.messaging.credit.RefundReversalRequest {

	public RefundReversalRequest() {
	}

	public IParser execute(IParser objISO) throws TPlusException {
		super.execute(objISO);
		return objISO;
	}

}