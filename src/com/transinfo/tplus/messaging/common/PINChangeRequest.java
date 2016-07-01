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

package com.transinfo.tplus.messaging.common;

import org.jpos.iso.ISOMsg;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.hsm.HSMIF;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.RequestBaseHandler;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.messaging.validator.CardNumberValidator;
import com.transinfo.tplus.messaging.validator.CardStatusValidator;
import com.transinfo.tplus.messaging.validator.CardValidator;
import com.transinfo.tplus.messaging.validator.Validator;

@SuppressWarnings("static-access")
public class PINChangeRequest extends RequestBaseHandler {

	public PINChangeRequest(){}
	private final boolean HSMON = true;

	public IParser execute(IParser objISO)throws TPlusException {

		TransactionDataBean objTranxBean=null;

		try
		{
			TransactionDB objTranxDB = new TransactionDB();
			ISOMsg isoMap = objISO.clone();
			objISO.setCloneISO(isoMap);
			try
			{
				objTranxBean = objISO.getTransactionDataBean();
				
				// assign transaction code sub type
				objTranxBean.setTranxCodeSubType("PINCHG");

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
			String strPinChangeStatus = "Y";

			String responseCode = "";
			String remarks = "";

			if(HSMON)
			{
				//if(objCardInfo != null && objCardInfo.isPinBlocked().equals("Y")) {
				HSMIF hsmAdaptor = new HSMAdaptor();
				int verRes = hsmAdaptor.verifyPIN_PVV(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),objISO.getCardNumber(),objCardInfo.getPVVOFFSET(),objISO.getValue(52),objISO.getTransactionDataBean().getAcqBinType());
				if(verRes==0)
				{
					objTranxDB.UpdatePinCnt( objISO.getCardNumber(),"Y", 0); //correct PIN

					int res = hsmAdaptor.changePIN(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),objISO.getCardNumber(),objISO.getValue(47),objISO.getTransactionDataBean().getAcqBinType());

					if(res == 0)
					{
						responseCode = "00";
						remarks = "Tranx Approved";
						//objISO.setValue(39,"00");
					}
					else
					{
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINChangeRequest Error:  PIN Change not Successfull");
						System.out.println("PINChangeRequest Error: PIN Change not Successfull");
						responseCode = "96";
						remarks = "Error on changePIN";
						//objISO.setValue(39,"96");
					}
				}
				else
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINChangeRequest Error: OLD PIN  Validation not Successfull");
					System.out.println("PINChangeRequest Error: OLD PIN is Validation not Successfull");

					if(Integer.parseInt(objCardInfo.getWrongPINCount())+1>2)
					{
						strPinChangeStatus = "N";
					}

					objTranxDB.UpdatePinCnt(objISO.getCardNumber(),strPinChangeStatus ,(Integer.parseInt(objCardInfo.getWrongPINCount())+1)); //wrong PIN
					responseCode = "55";
					remarks = "Incorrect PIN ";
					//objISO.setValue(39,"55");
				}

			}else
			{
				responseCode = "00";
				remarks = "Tranx Approved";
				//objISO.setValue(39,"00");
			}

			try
			{

				// get the approval code
				String approvalCode = objISO.getRandomApprovalCode();

				objTranxBean.setResponseCode(responseCode);
				objTranxBean.setRemarks(remarks);
				objTranxBean.setApprovalCode(approvalCode);

				objISO.setValue(39,responseCode);

				WriteLogDB objWriteLogDb = new WriteLogDB();
				objWriteLogDb.updateLog(objISO.getTransactionDataBean());

				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Transaction Inserted....");
				System.out.println("Transaction Inserted");
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("Error Inserting Transaction...");
				System.out.println("Error Inserting Transaction...");
				throw new OnlineException("96","G0001","System Error");
			}

			return objISO;

		}catch(OnlineException cex){
			throw new OnlineException(cex);
		}catch(TPlusException tplusExp) {
			System.out.println("Exception while execute PINChangeRequest.."+tplusExp.getMessage());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINChangeRequest: Exception while execute PINChangeRequest.."+tplusExp.getMessage());
			objISO.setValue(TPlusISOCode.RESPONSE_CODE,tplusExp.getErrorCode());
			throw tplusExp;

		}catch(Exception exp) {
			System.out.println("Exception while PINChangeRequest.."+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINChangeRequest: Exception while execute PINChangeRequest.."+exp);
			throw new TPlusException(TPlusCodes.APPL_ERR," Error: in PINChangeRequest: "+exp.getMessage());
		}

	}

}