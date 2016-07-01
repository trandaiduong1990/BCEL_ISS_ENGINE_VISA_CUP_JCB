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

package com.transinfo.tplus.messaging;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.TPlusPrintOutput;
import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.db.WriteLogDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.TPlusISOCode;
import com.transinfo.tplus.messaging.parser.TPlusISOMsg;

@SuppressWarnings({"static-access","unused"})
public class RequestProcessor extends IRequestProcessor
{

	public RequestProcessor(){}

	public RequestBaseHandler getRequestHandler(IParser objISO)throws TPlusException
	{

		String reqHandler =null;

		/*try
		{

		Class classObj =  Class.forName("com.transinfo.tplus.messaging.credit.jcb.SaleRequest");
		RequestBaseHandler objReqHandler = (RequestBaseHandler)classObj.newInstance();
		return objReqHandler;

		}
		catch(Exception exp)
		{
			throw new OnlineException("96","A05374","Error in RequestProcessor:Unable to create request handler");

		}*/

		try
		{
			System.out.println(objISO.getMTI());

			TransactionDB objTranx = new TransactionDB();


			objISO =objTranx.getRequestDetails(objISO);

			if(objISO ==null)
			{
				return null;
			}

			ISOMsg actualReq = objISO.clone();

			System.out.println("&*&*&*&*&*&"+objISO.getConnectionName());


			objISO.setMsgObject(actualReq);
			reqHandler = objISO.getReqHandler();



			if(reqHandler !=null)
			{

				TPlusPrintOutput.printMessage("RequestBaseHandler","Creating Handler Class for="+reqHandler);
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("RequestProcessor: Creating Handler Class for "+reqHandler);

				Class classObj =  Class.forName(reqHandler);
				RequestBaseHandler objReqHandler = (RequestBaseHandler)classObj.newInstance();
				return objReqHandler;
			}
			else
			{

				return null;
			}

		}
		catch(TPlusException tplusexce)
		{
			throw new OnlineException("12","A05374","Error in RequestProcessor:Unable to create request handler");
		}catch (OnlineException e) {
			throw e;
		}
		catch(Exception exp)
		{
			throw new OnlineException("96","A05374","Error in RequestProcessor:Unable to create request handler");

		}


	}

	public IParser processRequest(IParser objISO)  throws TPlusException
	{

		byte[] byteResponse = null;
		IParser objResISO=null;

		try
		{

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("RequestProcessor:In ProcessRequest...");
			TPlusPrintOutput.printMessage("RequestProcessor","In ProcessRequest...");

			System.out.println("Populating Data1..");
			com.transinfo.tplus.log.TransactionDataBean objTransactionDataBean = new com.transinfo.tplus.log.TransactionDataBean();
			objTransactionDataBean.populateData(objISO);

			TransactionDB objTranxDB = new TransactionDB();

			objISO.setTransactionDataBean(objTransactionDataBean);
			TransactionDataBean objTranxBean = objISO.getTransactionDataBean();

			if(!objISO.getMTI().equals("0800"))
			{
				objTranxBean =objTranxDB.checkOffusTransaction(objISO);
			}

			System.out.println("Populating Data Completed..");

			RequestBaseHandler objReqHandler = getRequestHandler(objISO);

			objTransactionDataBean.setTranxCode(objISO.getTranxType());

			if(objReqHandler !=null)
			{

				objISO = objReqHandler.execute(objISO);

				TPlusPrintOutput.printMessage("RequestProcessor","Request Processed Successfully and Response sent"+objISO);
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("RequestProcessor:Request Processed Successfully and Response sent... ");
			}
			else
			{
				objISO.setValue(39,"12");
				objISO.setRemarks("Invalid Transaction (MTI or ProcessingCode)");
				
				// Since NO Processor found, manually check card and assign scheme
				String scheme = objISO.getCardScheme();
				objISO.setCardProduct(scheme);

				TPlusPrintOutput.printMessage("RequestProcessor","Invalid Transaction  Conn-MTI-ProcessingCode  "+ objISO.getConnectionId()+" - "+objISO.getValue(TPlusISOCode.MTI)+" - "+objISO.getValue(TPlusISOCode.PROCESSING_CODE));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("RequestProcessor :Invalid Transaction  Conn-MTI-ProcessingCode  "+ objISO.getConnectionId()+" - "+objISO.getValue(TPlusISOCode.MTI)+"- "+objISO.getValue(TPlusISOCode.PROCESSING_CODE));
				//MonitorLogListener monitorLogListener = MonitorLogListener.getMonitorLogListener(8005);
				//monitorLogListener.sendSystemLog("Invalid Transaction  Conn-MTI-ProcessingCode  "+ objISO.getConnectionId()+" - "+objISO.getValue(TPlusISOCode.MTI)+" - "+objISO.getValue(TPlusISOCode.PROCESSING_CODE),MonitorLogListener.RED);
				throw new OnlineException("12","A05374","Error in RequestProcessor:unable to found request handler");

			}
		}
		catch(OnlineException onlineExp)
		{
			System.out.println("OnLINBE Exp Happened");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("Online Exception Happened.."+onlineExp.getDescription());

			objISO.getTransactionDataBean().setResponseCode(onlineExp.getResponseCode());
			objISO.getTransactionDataBean().setRemarks(onlineExp.getDescription());

			System.out.println("IS getF44= "+objISO.getF44());

			System.out.println(onlineExp.getResponseCode()+"      "+onlineExp.getDescription());

			String f14 = objISO.getValue(14);
			System.out.println("f14 = " + f14);

			if(objISO.getCloneISO() !=null)
			{
				objISO.setMsgObject(objISO.getCloneISO());
			}

			if(f14 != null && !"".equals(f14)){
				objISO.setValue(14, f14);
			}

			objISO.setValue(39,onlineExp.getResponseCode());

			if("VI".equals(objISO.getCardProduct())){

				if(objISO.getF44() !=null)
				{
					objISO.setValue(44,objISO.getF44());
				}

			} else if("JC".equals(objISO.getCardProduct())){

				if(objISO.getStrJcbF44() !=null && !"".equals(objISO.getStrJcbF44()))
				{
					objISO.setValue(44,objISO.getStrJcbF44());
				}

				// for JCB Bit 38 on response is must
				if(objISO.getValue(38) == null){
					objISO.setValue(38,"000000");
				}

			}

			if(objISO.getStrF60() != null && !"".equals(objISO.getStrF60()))
			{
				objISO.setValue(60,objISO.getStrF60());
			}

			String reqF61 = objISO.getValue(61);
			if(reqF61 != null && !"".equals(reqF61)){

				// assign field 61 to CUP transactions
				if(objISO.getStrF61() != null && !"".equals(objISO.getStrF61()))
				{
					objISO.setValue(61,objISO.getStrF61());
				}

			}

			if("CU".equals(objISO.getCardProduct())){

				String reqF14 = objISO.getValue(14);
				if(reqF14 == null || "".equals(reqF14)){
					objISO.setValue(14,"0000");
				}

			}

			try
			{

				System.out.println(objISO.getMTI());

				if(objISO.getMTI().equals("0400") || objISO.getMTI().equals("0401") || objISO.getMTI().equals("0420") || objISO.getMTI().equals("0421"))
				{
					GenerateRevARPC objGenARPC = new GenerateRevARPC(false);
					objGenARPC.generateARPC(objISO);
				}
				else
				{
					GenerateARPC objGenARPC = new GenerateARPC(false);
					objGenARPC.generateARPC(objISO);
				}

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
				objISO.setValue(39,"96");
			}

		}
		catch(TPlusException tplusExp)
		{
			//Set Response
			System.out.println("###############ERROR="+tplusExp.getMessage());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("RequestProcessor:Error in processing the message: "+tplusExp.getMessage());
			throw tplusExp;
		}
		catch(Exception exp)
		{
			//Set Response
			System.out.println("ERROR="+exp.getMessage());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("RequestProcessor:Error in processing the message: "+exp);
			throw new TPlusException(TPlusCodes.ERR_REQ,"Error: in RequestProcessor= "+exp.toString());
		}
		finally
		{
			System.out.println("----------------------Finally -----------in RequestProcessor----------------");
			if(objISO != null){
				if(!"6011".equals(objISO.getValue(18)) && objISO.getValue(32).equals("437733"))
				{
					System.out.println("Finally 1");
					objISO.unset(54);
				}

				System.out.println("++++++++++F 54 in Req Processor ++++++"+objISO.getValue(54));

				// ONUS or OFFUS
				String tranxType = objISO.getTransactionDataBean().getAcqBinType();

				if("ONUS".equals(tranxType))
					//if(("437733".equals(objISO.getValue(32))) && "0200".equals(objISO.getMTI()))
					//	if(objISO.getValue(32).equals("437733"))
				{
					System.out.println("Acq ID :: 437733, ONUS Tranx");

					int unsetFld[] = {10,14,18,22,26,28,35,43,45,52,53,60,126};
					objISO.unset(unsetFld);

				}else{

					System.out.println("OFFUS Tranx");

					// un set fields from response
					objISO.unsetFieldsOnResponse(objISO);

				}

			}else{
				System.out.println("objISO is NULL");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("objISO is NULL");
			}
		}

		System.out.println("------------------- Response SEND-----------------------");
		return objISO;
	}


}