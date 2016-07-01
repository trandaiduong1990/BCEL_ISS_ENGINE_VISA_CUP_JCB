/**
 * Copyright (c) 2007-2008 Trans-Info Pte Ltd. Singapore. All Rights Reserved.
 * This work contains trade secrets and confidential material of
 * Trans-Info Pte Ltd. Singapore and its use of disclosure in whole
 * or in part without express written permission of
 * Trans-Info Pte Ltd. Singapore. is prohibited.
 * Date of Creation   : Feb 25, 2008
 * Version Number     : 1.0
 * Modification History:
 * Date          Version No.         Modified By           Modification Details.
 */

package com.transinfo.tplus.messaging.debit;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.hsm.HSMIF;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardStatusValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings({"unused"})
public class PINActivateRequest extends RequestBaseHandler {


	public PINActivateRequest(){}

	public IParser execute(IParser objISO)throws TPlusException {

		try {

			System.out.println("PINActivateRequest ="+(String)objISO.getValue(0));
			TransactionDB objTranxDB = new TransactionDB();
			ISOMsg isoMap = objISO.clone();
			objISO.setCloneISO(isoMap);

			try
			{
				Validator validator = new Validator();
				validator.addValidator(new CardNumberValidator());
				validator.addValidator(new CardValidator());
				
				// add new card status validator
				validator.addValidator(new CardStatusValidator());
				
				validator.process(objISO);
			}
			catch(OnlineException exp) {
				System.out.println("Exception=="+exp);
				throw exp;
			}

			CardInfo objCardInfo = objISO.getCardDataBean();
			HSMIF hsmAdaptor = new HSMAdaptor();
			int res = hsmAdaptor.activatePIN(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),objISO.getCardNumber(),objISO.getValue(53),objISO.getTransactionDataBean().getAcqBinType());
			if(res == 0) {
				objISO.setValue(39,"00");
			}
			else {
				objISO.setValue(39,"96");
			}
			return objISO;


		}catch(OnlineException cex){
			throw new OnlineException(cex);
		}

		catch(TPlusException tplusExp) {
			System.out.println("PINActivateRequest: Exception while execute PINActivateRequest.."+tplusExp.getMessage());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINActivateRequest: Exception while execute PINActivateRequest.."+tplusExp.getMessage());
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch(Exception exp) {
			System.out.println("Exception while PINActivateRequest.."+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINActivateRequest: Exception while execute PINActivateRequest.."+exp);
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in PINActivateRequest: "+exp.getMessage());
		}

	}

}