package com.transinfo.tplus.messaging;


import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.jpos.tlv.TLVMsg;

import vn.com.tivn.hsm.phw.NumberUtil;

import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.hsm.HSMIF;
import com.transinfo.tplus.log.TransactionDataBean;
import com.transinfo.tplus.messaging.parser.IParser;

public class GenerateRevARPC
{
	boolean isMandatory = false;
	public GenerateRevARPC(boolean isMandatory)
	{
		this.isMandatory = isMandatory;
	}

	public boolean generateARPC(IParser objISO) throws OnlineException
	{
		System.out.println(" In Reversal EMVValidator process...");
		if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Reversal Response Generation.....");
		try
		{

			if(!objISO.hasField(55) && !isMandatory)
			{
				return true;
			}

			if(!objISO.hasField(55) && isMandatory)
			{
				throw new OnlineException("05", "022822", "No EMV Data Available");
			}

			if(objISO.getCardProduct().equals("CU"))
			{
				System.out.println("CUP emv reversal");
				generateARPCCUP(objISO, objISO.getValue(39));
			}
			else if(objISO.getCardProduct().equals("JC"))
			{
				generateARPCJCB(objISO, objISO.getValue(39));
			}
			else
			{
				generateARPCVISA(objISO);
			}

		}catch (OnlineException e){
			e.printStackTrace();
			throw new OnlineException(e);
		}catch (Exception e){
			e.printStackTrace();
			throw new OnlineException("96","G0001","System Error");
		}
		return true;
	}

	public boolean generateARPCVISA(IParser objISO)throws Exception
	{
		try
		{

			String emvData="";
			TLVList tlv = new TLVList();

			tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55).substring(6)));

			TLVMsg msg = tlv.find(Integer.parseInt("9f36",16));
			if(msg == null)
			{
				return true;
			}
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			String strTranxCounter= ISOUtil.hexString(msg.getValue());

			String strResF55 = "9F3602"+strTranxCounter;

			int length = (strResF55.length()/2);

			String hexlen = Integer.toHexString(length);
			if(hexlen.length()< 2)
			{
				hexlen = "0"+hexlen;
			}

			strResF55 = "0100"+hexlen+strResF55;

			// assign F55 response
			TransactionDataBean objTransactionDataBean = objISO.getTransactionDataBean();
			objTransactionDataBean.setF55Res(strResF55);

			objISO.setBinaryValue(55,ISOUtil.hex2byte(strResF55));
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Reversal Response Generation Successfuly.....");

		}catch (OnlineException e){
			e.printStackTrace();
			throw new OnlineException(e);
		}catch (Exception e){
			e.printStackTrace();
			throw new OnlineException("96","G0001","System Error");
		}
		return true;

	}

	public boolean generateARPCCUP(IParser objISO,String strResCode)throws Exception
	{
		try{

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> generateARPCCUP.....");
			
			String emvData="";
			TLVList tlv = new TLVList();

			//tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55).substring(6)));
			tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55)));

			TLVMsg msg = tlv.find(Integer.parseInt("9f36",16));
			if(msg == null)
			{
				return true;
			}
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			String strTranxCounter= ISOUtil.hexString(msg.getValue());

			String strResF55 = "9F3602"+strTranxCounter;

			int length = (strResF55.length()/2);

			String hexlen = Integer.toHexString(length);
			if(hexlen.length()< 2)
			{
				hexlen = "0"+hexlen;
			}

			// assign F55 response
			TransactionDataBean objTransactionDataBean = objISO.getTransactionDataBean();
			objTransactionDataBean.setF55Res(strResF55);

			objISO.setBinaryValue(55,ISOUtil.hex2byte(strResF55));
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Reversal Response Generation Successfuly.....");
			
		}catch (OnlineException e){
			e.printStackTrace();
			throw new OnlineException(e);
		}catch (Exception e){
			e.printStackTrace();
			throw new OnlineException("96","G0001","System Error");
		}

		return true;
	}

	public boolean generateARPCJCB(IParser objISO,String strResCode)throws Exception
	{
		try{

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> generateARPCJCB.....");
			
			String emvData="";
			TLVList tlv = new TLVList();

			//tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55).substring(6)));
			tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55)));

			TLVMsg msg = tlv.find(Integer.parseInt("9f36",16));
			if(msg == null)
			{
				return true;
			}
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			String strTranxCounter= ISOUtil.hexString(msg.getValue());

			String strResF55 = "9F3602"+strTranxCounter;

			int length = (strResF55.length()/2);

			String hexlen = Integer.toHexString(length);
			if(hexlen.length()< 2)
			{
				hexlen = "0"+hexlen;
			}

			// assign F55 response
			TransactionDataBean objTransactionDataBean = objISO.getTransactionDataBean();
			objTransactionDataBean.setF55Res(strResF55);

			objISO.setBinaryValue(55,ISOUtil.hex2byte(strResF55));
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Reversal Response Generation Successfuly.....");
			
		}catch (OnlineException e){
			e.printStackTrace();
			throw new OnlineException(e);
		}catch (Exception e){
			e.printStackTrace();
			throw new OnlineException("96","G0001","System Error");
		}

		return true;
	}

	public static void main(String s[])
	{

		System.out.println(ISOUtil.hexString("00".getBytes()));
	}


}