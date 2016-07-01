package com.transinfo.tplus.messaging.validator;


import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.jpos.tlv.TLVMsg;

import vn.com.tivn.hsm.phw.NumberUtil;

import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.hsm.HSMIF;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.OnlineUtil;
import com.transinfo.tplus.messaging.parser.IParser;


public class EMVValidator implements BaseValidator
{
	boolean isMandatory = false;
	public EMVValidator(boolean isMandatory)
	{
		this.isMandatory = isMandatory;
	}

	public boolean process(IParser objISO) throws OnlineException
	{
		System.out.println(" In EMVValidator process...");
		try
		{

			System.out.println("EMVValidator - objISO.isEComTranx()..." + objISO.isEComTranx());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("EMVValidator - objISO.isEComTranx()..." + objISO.isEComTranx());

			if(objISO.isEComTranx())
			{

				System.out.println("EMVValidator - Return here");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("EMVValidator - Return here");

				return true;
			}

			if(!objISO.hasField(55) && !isMandatory)
			{
				return true;
			}

			if(!objISO.hasField(55) && isMandatory)
			{
				throw new OnlineException("05", "022822", "No EMV Data Available");
			}

			/*// update F55 data
			TransactionDataBean objTranxBean= objISO.getTransactionDataBean();
			objTranxBean.setF55Exist("Y");
			objTranxBean.setF55(objISO.getValue(55));*/

			// for JCB EMV validation
			String tag95 = "";

			String emvData="";
			TLVList tlv = new TLVList();

			//tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55)));
			//tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55).substring(6)));

			if(objISO.getCardProduct().equals("CU") || objISO.getCardProduct().equals("JC") )
			{
				tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55)));
			}
			else
			{
				tlv.unpack(ISOUtil.hex2byte(objISO.getValue(55).substring(6)));
			}

			TLVMsg msg = tlv.find(Integer.parseInt("9f02",16));
			System.out.println(Integer.toHexString(msg.getTag()) + " : " +ISOUtil.hexString(msg.getValue()));

			emvData = emvData+ISOUtil.hexString(msg.getValue());

			emvData = emvData+"000000000000";

			msg = tlv.find(Integer.parseInt("9f1a",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());

			msg = tlv.find(Integer.parseInt("95",16));
			emvData = emvData+ISOUtil.hexString(msg.getValue());

			System.out.println("Tag 95 :: " + ISOUtil.hexString(msg.getValue()));
			tag95 = ISOUtil.hexString(msg.getValue());

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
			objISO.setTranxCnt(ISOUtil.hexString(msg.getValue()));
			System.out.println("7");
			msg = tlv.find(Integer.parseInt("9f10",16));
			String str9f10 = ISOUtil.hexString(msg.getValue());

			if(objISO.getCardProduct().equals("CU"))
			{
				emvData = emvData+str9f10.substring(str9f10.length()-10,str9f10.length()-2);
				emvData = emvData+"800000";
			}
			else if(objISO.getCardProduct().equals("JC"))
			{
				emvData = emvData+str9f10.substring(str9f10.length()-10);
				emvData = emvData+"000000";
			}
			else
			{
				emvData = emvData+str9f10.substring(str9f10.length()-8);
				emvData = emvData+"000000";
			}


			msg = tlv.find(Integer.parseInt("9f26",16));
			String ARQC = ISOUtil.hexString(msg.getValue());
			objISO.setARQC(ARQC);

			msg = tlv.find(Integer.parseInt("9f36",16));
			String strATC = ISOUtil.hexString(msg.getValue());

			System.out.println(emvData);


			String strPANData =  objISO.getCardNumber()+objISO.getValue(23).substring(1);
			strPANData = strPANData.substring(strPANData.length()-16);
			System.out.println("PAN Data ="+strPANData);

			byte[] ARPC = new byte[8];
			HSMIF hsmAdaptor = new HSMAdaptor();
			String strF44 = objISO.getF44();

			if(objISO.getCardProduct().equals("CU"))
			{


				String strF61 = objISO.getStrF61();

				String reqF61 = objISO.getValue(61);
				if(reqF61 != null && !"".equals(reqF61)){
					String secValue = "";
					if(strF61.length()>32){
						secValue = strF61.substring(32, strF61.length());
					}
					strF61 = strF61.substring(0, 24)+reqF61.substring(24,27)+strF61.substring(27, 31)+strF61.substring(31, 32)+secValue;
				}else{
					//strF61 = "";
				}

				byte[] strEMVKey = new byte[16];
				System.out.println("strPANData="+strPANData+"   "+strATC);
				hsmAdaptor.getEMVKey(strPANData,strATC,strEMVKey);
				System.out.println("strEMVKey="+NumberUtil.hexString(strEMVKey));
				objISO.setEMVKey(NumberUtil.hexString(strEMVKey));
				if(!validateARQC(ARQC,emvData,NumberUtil.hexString(strEMVKey)))
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CUP EMV Validation Failed.....");
					System.out.println("**** EMV Validation Failed *****");

					if(!"".equals(strF61)){
						String secValue = "";
						if(strF61.length()>32){
							secValue = strF61.substring(32, strF61.length());
						}
						strF61 = strF61.substring(0, 24)+strF61.substring(24,27)+strF61.substring(27, 31)+"2"+secValue;
						objISO.setStrF61(strF61);
					}

					throw new OnlineException("05","A05374","CUP EMV Validation Failed ");
				}
				else
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Validation Successful.....");
					System.out.println("**** EMV Validation Successful *****");

					if(!"".equals(strF61)){
						String secValue = "";
						if(strF61.length()>32){
							secValue = strF61.substring(32, strF61.length());
						}
						strF61 = strF61.substring(0, 24)+strF61.substring(24,27)+strF61.substring(27, 31)+"1"+secValue;
						objISO.setStrF61(strF61);
					}

				}
			}
			else if(objISO.getCardProduct().equals("JC"))
			{

				// commented on 11-03-2016 by Nishandan to support ONUS JCB transaction. Since OFFUS JCB F23 4 digits and ONUS 3 digits
				//strPANData =  objISO.getCardNumber()+objISO.getValue(23).substring(2);

				String f23 = objISO.getValue(23);
				strPANData =  objISO.getCardNumber()+f23.substring(f23.length()-2);
				// end
				
				strPANData = strPANData.substring(strPANData.length()-16);
				System.out.println("PAN Data ="+strPANData);

				byte[] strEMVKey = new byte[16];
				System.out.println("strPANData="+strPANData+"   "+strATC);
				hsmAdaptor.getJCBEMVKey(strPANData,strATC,strEMVKey);
				System.out.println("strEMVKey="+NumberUtil.hexString(strEMVKey));
				objISO.setEMVKey(NumberUtil.hexString(strEMVKey));
				if(!validateARQC(ARQC,emvData,NumberUtil.hexString(strEMVKey)))
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> JCB EMV Validation Failed.....");
					System.out.println("**** EMV Validation Failed *****");


					throw new OnlineException("05","A05374","EMV Validation Failed ");
				}
				else
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> JCB EMV Validation Successful.....");
					System.out.println("**** EMV Validation Successful *****");

				}

				// here we check unsuccessful off line SDA failed or NOT
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("tag95 :: " + tag95);
				if(tag95 != null && !"".equals(tag95)){
					if(tag95.length() > 1){
						if("4".equals(tag95.substring(0, 1))){
							throw new OnlineException("05","A05374","Unsuccssful offline SDA");
						}
					}
				}

			}
			else
			{

				int verRes = hsmAdaptor.EMVValidation("01",1,"00",strPANData,"00","00","02",emvData,ARQC,"00","00","01","3030",ARPC);

				System.out.println(strF44.length());

				if(verRes!=0)
				{
					System.out.println(strF44);
					//strF44 = strF44.substring(0,8)+"1"+strF44.substring(9,11);
					strF44 = strF44.substring(0,8)+"1";
					objISO.setF44(strF44);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Validation Failed....."+verRes);
					System.out.println("**** EMV Validation Failed *****"+verRes);
					throw new OnlineException("05","A05374","EMV Validation Failed ");
				}
				else
				{
					System.out.println(strF44+"  "+strF44.substring(0,8));
					//strF44 = strF44.substring(0,8)+"2"+strF44.substring(9,11);
					strF44 = strF44.substring(0,8)+"2";
					System.out.println(strF44);
					objISO.setF44(strF44);
					System.out.println("EMV Validation is Successful");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EMV Validation Success....."+verRes);
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


	public boolean validateARQC(String strARQC,String strARQCSrc,String strKey)throws Exception
	{

		String I1 = strARQCSrc.substring(0,16);
		String De1="";

		for(int i =16 ;i<=strARQCSrc.length();i=i+16)
		{

			System.out.println(I1+"   "+i);
			De1 = NumberUtil.hexString(OnlineUtil.encrypt(NumberUtil.hex2byte(strKey.substring(0,16)),NumberUtil.hex2byte(I1)));

			if(i<80)
			{
				System.out.println("Source="+strARQCSrc.substring(i,i+16));
				I1 = TPlusUtility.strXor(De1,strARQCSrc.substring(i,i+16));
				if(I1.length()<16)
					I1 = TPlusUtility.lpad(I1,"0",16);
			}

		}

		String De2 = NumberUtil.hexString(OnlineUtil.decrypt(NumberUtil.hex2byte(strKey.substring(16,32)),NumberUtil.hex2byte(De1)));
		String ARQC=NumberUtil.hexString(OnlineUtil.encrypt(NumberUtil.hex2byte(strKey.substring(0,16)),NumberUtil.hex2byte(De2)));

		System.out.println("ARQC="+ARQC);

		if(strARQC.equals(ARQC))

		{
			return true;
		}
		else
		{
			return false;
		}



	}



}