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

public class GenerateARPC
{
	boolean isMandatory = false;
	public GenerateARPC(boolean isMandatory)
	{
		this.isMandatory = isMandatory;
	}

	public boolean generateARPC(IParser objISO) throws OnlineException
	{
		if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Response Generation.....");
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
		try{


			String emvData="";
			TLVList tlv = new TLVList();

			tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55).substring(6)));

			TLVMsg msg = tlv.find(Integer.parseInt("9f02",16));
			System.out.println(Integer.toHexString(msg.getTag()) + " : " +ISOUtil.hexString(msg.getValue()));
			emvData = emvData+ISOUtil.hexString(msg.getValue());

			emvData = emvData+"000000000000";

			msg = tlv.find(Integer.parseInt("9f1a",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());

			msg = tlv.find(Integer.parseInt("95",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			System.out.println("1");
			msg = tlv.find(Integer.parseInt("5f2a",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			System.out.println("2");
			msg = tlv.find(Integer.parseInt("9a",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			System.out.println("3");
			msg = tlv.find(Integer.parseInt("9c",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			System.out.println("4");
			msg = tlv.find(Integer.parseInt("9f37",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());

			System.out.println("5");
			msg = tlv.find(Integer.parseInt("82",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			System.out.println("6");
			msg = tlv.find(Integer.parseInt("9f36",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());
			System.out.println("7");
			String strTranxCounter= ISOUtil.hexString(msg.getValue());

			msg = tlv.find(Integer.parseInt("9f10",16));
			String str9f10 = ISOUtil.hexString(msg.getValue());
			emvData = emvData+str9f10.substring(str9f10.length()-8);
			emvData = emvData+"000000";

			msg = tlv.find(Integer.parseInt("9f26",16));
			String ARQC = ISOUtil.hexString(msg.getValue());

			System.out.println(emvData);

			String strPANData =  objISO.getCardNumber()+objISO.getValue(23).substring(1);
			strPANData = strPANData.substring(strPANData.length()-16);
			System.out.println("PAN Data ="+strPANData);

			byte[] ARPC = new byte[8];
			HSMIF hsmAdaptor = new HSMAdaptor();
			System.out.println("RESPONSE CODE..."+ISOUtil.hexString(objISO.getValue(39).getBytes()));
			int verRes = hsmAdaptor.EMVValidation("02",1,"00",strPANData,"00","00","02",emvData,ARQC,"00","00","01",ISOUtil.hexString(objISO.getValue(39).getBytes()),ARPC);

			if(verRes != 0){
				System.out.println("**** ARPC Validation Failed *****"+verRes);
				throw new OnlineException("05","A05374","ARPC Validation Failed");
			}

			System.out.println(ISOUtil.hexString(ARPC));
			String strResF55 = "9F3602"+strTranxCounter+"910A"+ISOUtil.hexString(ARPC)+ISOUtil.hexString(objISO.getValue(39).getBytes());

			int length = (strResF55.length()/2);

			String hexlen = Integer.toHexString(length);
			if(hexlen.length()< 2)
			{
				hexlen = "0"+hexlen;
			}

			strResF55 = "0100"+hexlen+strResF55;
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Response Generation Successfully .....");

			// assign F55 response
			TransactionDataBean objTransactionDataBean = objISO.getTransactionDataBean();
			objTransactionDataBean.setF55Res(strResF55);

			objISO.setBinaryValue(55,ISOUtil.hex2byte(strResF55));

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

			System.out.println("generateARPC = "+objISO.getARQC()+"  "+objISO.getTranxCnt());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("generateARPC = "+objISO.getARQC()+"  "+objISO.getTranxCnt());

			if(objISO.getARQC() == null || "".equals(objISO.getARQC()) || objISO.getEMVKey() == null || "".equals(objISO.getEMVKey())){

				System.out.println("generateARPCCUP :: CUP Emv validation NOT perform. So ARPC will not generate");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("generateARPCCUP :: CUP Emv validation NOT perform. So ARPC will not generate");

			}else{

				String ARPCData = TPlusUtility.strXor(objISO.getARQC(),TPlusUtility.rpad(NumberUtil.hexString(strResCode.getBytes()),"0",16));
				System.out.println("ARPC Data="+ARPCData+"   "+objISO.getEMVKey());
				String skey =objISO.getEMVKey();
				skey = skey+skey.substring(0,16);
				System.out.println("ARPC="+NumberUtil.hexString(OnlineUtil.encrypt3DES(NumberUtil.hex2byte("6EFB61342F6D9175BA0BD940D9BFD6D66EFB61342F6D9175"),NumberUtil.hex2byte(ARPCData))));
				String ARPC=NumberUtil.hexString(OnlineUtil.encrypt3DES(NumberUtil.hex2byte(skey),NumberUtil.hex2byte(ARPCData)));
				//String strF55Res = "9F3602"+objISO.getTranxCnt()+"9108"+ARPC;

				String strF55Res = "9F3602"+objISO.getTranxCnt()+"910A"+ARPC+ISOUtil.hexString(objISO.getValue(39).getBytes());

				System.out.println("strF55Res="+strF55Res);
				
				// assign F55 response
				TransactionDataBean objTransactionDataBean = objISO.getTransactionDataBean();
				objTransactionDataBean.setF55Res(strF55Res);
				
				objISO.setBinaryValue(55,strF55Res);

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

	public boolean generateARPCJCB(IParser objISO,String strResCode)throws Exception {
		
		System.out.println("Inside generateARPCJCB");
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("Inside generateARPCJCB");
		
		System.out.println("strResCode :: " + strResCode);
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("strResCode :: " + strResCode);
		
		try{

			System.out.println("objISO.getARQC() = "+objISO.getARQC()+"  "+objISO.getTranxCnt());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("objISO.getARQC() = "+objISO.getARQC()+"  "+objISO.getTranxCnt());

			if(objISO.getARQC() == null || "".equals(objISO.getARQC()) || objISO.getEMVKey() == null || "".equals(objISO.getEMVKey())){

				System.out.println("generateARPCJCB :: JCB Emv validation NOT perform. So ARPC will not generate");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("generateARPCJCB :: JCB Emv validation NOT perform. So ARPC will not generate");

			}else{

				String mti = objISO.getMTI();
				System.out.println("generateARPCJCB :: mti :: " + mti);
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("generateARPCJCB :: mti :: " + mti);

				if("0100".equals(mti)){

					String ARPCData = TPlusUtility.strXor(objISO.getARQC(),TPlusUtility.rpad(NumberUtil.hexString(strResCode.getBytes()),"0",16));
										
					System.out.println("ARPCData.length() = "+ARPCData.length());
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("ARPCData.length() = "+ARPCData.length());
					
					if(ARPCData.length() < 16) {						
						ARPCData = TPlusUtility.lpad(ARPCData,"0",16);
					}

					System.out.println("ARPC Data objISO.getEMVKey() = "+ARPCData+"   "+objISO.getEMVKey());
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("ARPC Data objISO.getEMVKey() = "+ARPCData+"   "+objISO.getEMVKey());
					
					String skey =objISO.getEMVKey();
					skey = skey+skey.substring(0,16);

					System.out.println("skey :: " + skey);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("skey :: " + skey);
					
					System.out.println("ARPC="+NumberUtil.hexString(OnlineUtil.encrypt3DES(NumberUtil.hex2byte("6EFB61342F6D9175BA0BD940D9BFD6D66EFB61342F6D9175"),NumberUtil.hex2byte(ARPCData))));
					String ARPC=NumberUtil.hexString(OnlineUtil.encrypt3DES(NumberUtil.hex2byte(skey),NumberUtil.hex2byte(ARPCData)));
					//String strF55Res = "9F3602"+objISO.getTranxCnt()+"9108"+ARPC;

					System.out.println("ARPC after encrypt = "+ARPC);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("ARPC after encrypt = "+ARPC);

					//String strF55Res = "9F3602"+objISO.getTranxCnt()+"910A"+ARPC+ISOUtil.hexString(objISO.getValue(39).getBytes());
					String strF55Res = "910A"+ARPC+ISOUtil.hexString(objISO.getValue(39).getBytes());

					System.out.println("strF55Res = "+strF55Res);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("strF55Res = "+strF55Res);
					
					// assign F55 response
					TransactionDataBean objTransactionDataBean = objISO.getTransactionDataBean();
					objTransactionDataBean.setF55Res(strF55Res);
					
					objISO.setBinaryValue(55,strF55Res);

				}

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



}