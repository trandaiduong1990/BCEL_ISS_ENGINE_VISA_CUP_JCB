package com.transinfo.tplus.messaging.validator;


import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.util.StringUtil;

@SuppressWarnings("unused")
public class CVV2Validator implements BaseValidator {

	boolean isMandatory = false;
	public CVV2Validator(boolean isMandatory)
	{
		this.isMandatory = isMandatory;
	}

	private  String rpad(String st, int length)
	{
		while (st.length()<length)
		{
			st = st + "0";
		}
		return st;
	}

	public boolean process(IParser objISO) throws OnlineException
	{
		try
		{
			//String strF44_10 ="";
			//String strF44_1_9="           ";

			String cardScheme = objISO.getCardProduct();

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> Card Scheme..... "+cardScheme);
			System.out.println(">>> Card Scheme..... "+cardScheme);

			if("VI".equals(cardScheme)){

				if(objISO.hasField(44)){

					String visaCVV2Res = "";

					String reqF44 = objISO.getValue(44);
					if(reqF44 != null && reqF44.length()>= 11){
						visaCVV2Res = reqF44.substring(10);

						if("M".equals(visaCVV2Res)){

							if(objISO.hasField(126)){

								ISOMsg objCVV2 = (ISOMsg)objISO.getISOObject(126);
								String strFullCVV2 = objCVV2.getString(10);

								if(strFullCVV2 == null){
									throw new OnlineException("05", "022822", "No CVV2 Data Available");
								}else{
									if(strFullCVV2.length() != 6){
										throw new OnlineException("05", "022822", "No CVV2 Data Available");
									}
								}

							}else{
								throw new OnlineException("05", "022822", "No CVV2 Data Available");
							}

						}
					}

				}

				if(!objISO.hasField(126) && !isMandatory)
				{
					return true;
				}

				if(!objISO.hasField(126) && isMandatory)
				{
					throw new OnlineException("05", "022822", "No CVV2 Data Available");
				}

				if(objISO.hasField(126)){
					ISOMsg objCVV2 = (ISOMsg)objISO.getISOObject(126);
					String strFullCVV2 = objCVV2.getString(10);

					if((strFullCVV2 == null) && !isMandatory){
						return true;
					}

					if((strFullCVV2 == null) && isMandatory){
						throw new OnlineException("05", "022822", "No CVV2 Data Available");
					}

				}

				String strF44_1_9="";//objISO.getF44().substring(0,10);
				if(objISO.getF44().length()>=10){
					strF44_1_9=objISO.getF44().substring(0,10);
				}else{
					strF44_1_9=StringUtil.RPAD(objISO.getF44(), 10, " ");
				}

				// CVV result passing here before start the CVV2 result
				strF44_1_9=strF44_1_9.substring(0,10);

				/*if(objISO.hasField(44))
			{
				if( objISO.getValue(44).length()==11)
				{
					//strF44_10 = objISO.getValue(44).substring(10,11);
					//strF44_1_9 = objISO.getValue(44).substring(0,10);
					strF44_1_9 = "5"+objISO.getValue(44).substring(1,10);
				}
				else
				{
					//strF44_1_9= ("           "+objISO.getValue(44)).substring(objISO.getValue(44).length());
					//strF44_1_9= ("5         "+objISO.getValue(44)).substring(objISO.getValue(44).length());
					//strF44_1_9="5"+StringUtil.RPAD(objISO.getF44(), 10, " ").substring(1, 10);
					strF44_1_9="5"+StringUtil.RPAD(objISO.getValue(44), 10, " ").substring(1, 10);
				}
			}*/

				ISOMsg objCVV2 = (ISOMsg)objISO.getISOObject(126);

				String strFullCVV2 = objCVV2.getString(10);
				String F126Pos1 = strFullCVV2.substring(0,1);
				String F126Pos2 = strFullCVV2.substring(1,2);
				String CVV2=null;

				String CVV2ResStatus="N";
				if(strFullCVV2.length() == 6)
				{
					CVV2 = strFullCVV2.substring(3,6);
				}
				System.out.println(F126Pos1+"  "+F126Pos2+"   "+CVV2);

				CardInfo objCardInfo = objISO.getCardDataBean();
				HSMAdaptor hsmAdaptor = new HSMAdaptor();

				String oc = objCardInfo.getOc();
				String expDate = objISO.getExpiryDate();

				String f44 = strF44_1_9;

				if(CVV2!=null && !CVV2.trim().equals(""))
				{

					int verRes = hsmAdaptor.verifyCVV2(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),objISO.getCardNumber(),expDate,"000",CVV2,objISO.getTransactionDataBean().getAcqBinType(),oc);
					if(verRes!=0)
					{
						if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation Failed....."+verRes);
						System.out.println("**** CVV2 Validation Failed *****"+verRes);
						f44 = f44+"N";
						objISO.setF44(f44);
						//objISO.setF44(strF44_1_9+"N");
						throw new OnlineException("N7", "022822", "Invalid CVV2 in the Message ");
					}
					else
					{

						//objISO.setF44(strF44_1_9+"N");
						f44 = f44+"M";
						objISO.setF44(f44);
						//objISO.setF44(strF44_1_9+"M");
						System.out.println("CVV2 Validation is Successful");
						if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation success....."+verRes);
					}
				}

				System.out.println("f44 :: " + f44);
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("f44 :: " + f44);

			} else if ("CU".equals(cardScheme)){

				// CUP CVV2 validation

				if(!objISO.hasField(61) && !isMandatory)
				{
					return true;
				}

				if(!objISO.hasField(61) && isMandatory)
				{
					throw new OnlineException("05", "022822", "No CVV2 Data Available");
				}
				
				// CVV2 indicator validation
				// get the key from F61 AM tag
				String f61 = objISO.getValue(61);

				String CVV2 = f61.substring(27, 30);
				
				if(f61.contains("AM")){
				
					// AM tag
					String strAM = f61.split("AM")[1];

					// get the indicator
					String verificationMode = strAM.substring(0, 16);
					
					String cvv2VerInd = verificationMode.substring(5, 6);
					
					if("1".equals(cvv2VerInd) && (CVV2 == null || "".equals(CVV2))){
						throw new OnlineException("05", "022822", "No CVV2 Data Available. but Indicator ON");
					}
					
				}

				if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVV2..... "+CVV2);
				System.out.println(">>> CVV2..... "+CVV2);

				CardInfo objCardInfo = objISO.getCardDataBean();
				HSMAdaptor hsmAdaptor = new HSMAdaptor();

				String oc = objCardInfo.getOc();
				String expDate = objISO.getExpiryDate();

				// CUP validation
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

				if(CVV2!=null && !CVV2.trim().equals(""))
				{

					int verRes = hsmAdaptor.verifyCVV2(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),objISO.getCardNumber(),expDate,"000",CVV2,objISO.getTransactionDataBean().getAcqBinType(),oc);
					if(verRes!=0)
					{
						if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation Failed....."+verRes);
						System.out.println("**** CVV2 Validation Failed *****"+verRes);

						if(!"".equals(strF61)){
							String secValue = "";
							if(strF61.length()>32){
								secValue = strF61.substring(32, strF61.length());
							}
							strF61 = strF61.substring(0, 24)+strF61.substring(24,27)+strF61.substring(27, 30)+"2"+strF61.substring(31, 32)+secValue;
							objISO.setStrF61(strF61);
						}

						throw new OnlineException("05", "022822", "Invalid CVV2 in the Message ");
					}
					else
					{

						if(!"".equals(strF61)){

							String secValue = "";
							if(strF61.length()>32){
								secValue = strF61.substring(32, strF61.length());
							}
							strF61 = strF61.substring(0, 24)+strF61.substring(24,27)+strF61.substring(27, 30)+"1"+strF61.substring(31, 32)+secValue;
							objISO.setStrF61(strF61);

						}

						System.out.println("CVV2 Validation is Successful");
						if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation success....."+verRes);
					}
				}else{

					// commented on 27-11-2015 By Nishandan after MOTO test case
					/*if(objISO.getValue(25).equals("08")) // MOTO Indicator Checking
					{

						if(!"".equals(strF61)){
							String secValue = "";
							if(strF61.length()>32){
								secValue = strF61.substring(32, strF61.length());
							}
							strF61 = strF61.substring(0, 24)+strF61.substring(24,27)+strF61.substring(27, 30)+"2"+strF61.substring(31, 32)+secValue;
							objISO.setStrF61(strF61);
						}

						throw new OnlineException("05", "022822", "Empty CVV2 in the Message ");

					}*/
					// end

				}

			} else if ("JC".equals(cardScheme)){

				// JCB CVV2 validation

				if(!objISO.hasField(48) && !isMandatory)
				{
					return true;
				}

				if(!objISO.hasField(48) && isMandatory)
				{
					throw new OnlineException("05", "022822", "No CVV2 Data Available");
				}

				String CVV2 = "";

				if(objISO.getValue(48) != null){
					CVV2 = TPlusUtility.parseJCBF48New(objISO.getValue(48)).get("01");
				}

				if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVV2..... "+CVV2);
				System.out.println(">>> CVV2..... "+CVV2);

				CardInfo objCardInfo = objISO.getCardDataBean();
				HSMAdaptor hsmAdaptor = new HSMAdaptor();

				String oc = objCardInfo.getOc();
				String expDate = objISO.getExpiryDate();

				// CUP validation
				String strF44 = objISO.getStrJcbF44();

				if(CVV2!=null && !CVV2.trim().equals(""))
				{

					int verRes = hsmAdaptor.verifyCVV2(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),objISO.getCardNumber(),expDate,"000",CVV2,objISO.getTransactionDataBean().getAcqBinType(),oc);
					if(verRes!=0)
					{
						if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation Failed....."+verRes);
						System.out.println("**** CVV2 Validation Failed *****"+verRes);

						// update CVV2 failed
						if(strF44.length() < 2){
							strF44 = "N"+" ";
						}else{
							strF44 = "N"+strF44.substring(1,2);
						}

						objISO.setStrJcbF44(strF44);

						throw new OnlineException("05", "022822", "Invalid CVV2 in the Message ");
					}
					else
					{

						// update CVV2 success
						// supposed to have 'M', but change this for testing
						//strF44 = strF44.substring(0,1)+"P"+strF44.substring(2,3);
						//strF44 = strF44.substring(0,1)+"M"+strF44.substring(2,3);
						if(strF44.length() < 2){
							strF44 = "M"+" ";
						}else{
							strF44 = "M"+strF44.substring(1,2);
						}

						objISO.setStrJcbF44(strF44);

						System.out.println("CVV2 Validation is Successful");
						if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation success....."+verRes);
					}

				}

			}

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVV2Validator is not available.....");

			return true;

		}
		catch(OnlineException onlineExp)
		{
			throw onlineExp;
		}
		catch(Exception exp)
		{
			throw new OnlineException("05", "022822", "Unable to process CVV2 "+exp.getMessage());
		}
	}
}
