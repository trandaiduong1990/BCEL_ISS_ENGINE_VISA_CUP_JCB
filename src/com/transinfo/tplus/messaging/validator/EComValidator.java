package com.transinfo.tplus.messaging.validator;


import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.util.StringUtil;

@SuppressWarnings("unused")
public class EComValidator implements BaseValidator {

	public boolean process(IParser objISO) throws OnlineException
	{
		try
		{

			System.out.println("EComValidator - objISO.isEComTranx()..." + objISO.isEComTranx());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("EComValidator - objISO.isEComTranx()..." + objISO.isEComTranx());

			if(!objISO.isEComTranx())
			{

				System.out.println("EComValidator - Return here");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("EComValidator - Return here");

				return true;
			}

			String cardScheme = objISO.getCardProduct();

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> Card Scheme..... "+cardScheme);
			System.out.println(">>> Card Scheme..... "+cardScheme);

			if ("CU".equals(cardScheme)){

				String f60 = objISO.getValue(60);
				System.out.println("f60 :: " + f60);
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("f60 :: "+f60);

				if(f60 != null && !"".equals(f60) && f60.length() >= 14){

					String eci = f60.substring(12,14);
					System.out.println("eci :: " + eci);

					// get the key from F61 AM tag
					String f61 = objISO.getValue(61);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("f61 :: "+f61);

					if(f61 != null && !"".equals(f61)){

						// AM tag
						String strAM = f61.split("AM")[1];
						System.out.println("strAM :: " + strAM);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("strAM :: " + strAM);
						
						if("09".equals(eci) && objISO.isAccountVerification()){
							
							if(strAM.length() < 39){
								
								System.out.println("NO Key on F61 for ACCVERIFY");
								if (DebugWriter.boolDebugEnabled) DebugWriter.write("NO Key on F61 for ACCVERIFY");
								throw new TPlusException("05","G0001","NO Key on F61 for ACCVERIFY");
								
							}
							
						}

						// get the indicator
						String verificationMode = strAM.substring(0, 16);

						// NON AUTH MODE NAME Validation
						if("10".equals(eci)){

							// NAME validation for NON-AUTH mode - 8th Position
							String embossName = objISO.getCardDataBean().getEmbossName();

							// get name indicator
							String nameIndi = verificationMode.substring(7, 8);
							if("1".equals(nameIndi)){

								// length of name
								String strNameLen = strAM.substring(16, 19);
								strNameLen = strNameLen.trim();

								if("".equals(strNameLen)){

									if (DebugWriter.boolDebugEnabled) DebugWriter.write("ECom Name validation : No Name");
									System.out.println("ECom Name validation : No Name");
									throw new OnlineException("05","G0001","ECom Name validation : No Name");

								}

								int nameLen = Integer.valueOf(strNameLen).intValue();

								String nameOnReq = strAM.substring(verificationMode.length()+strNameLen.length(), verificationMode.length()+strNameLen.length()+nameLen);
								nameOnReq = nameOnReq.trim();

								if(!embossName.equalsIgnoreCase(nameOnReq)){

									if (DebugWriter.boolDebugEnabled) DebugWriter.write("ECom Name validation : Wrong Name");
									System.out.println("ECom Name validation : Wrong Name");
									throw new OnlineException("05","G0001","ECom Name validation : Wrong Name");

								}

							}

						}

					}else{

						if("09".equals(eci)){

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("ECom validation : No F61");
							System.out.println("ECom validation : No F61");
							throw new OnlineException("05","G0001","ECom validation : No F61");

						}
					}

				}

			}

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> EComValidator is success....");

			return true;

		}
		catch(OnlineException onlineExp)
		{
			throw onlineExp;
		}
		catch(Exception exp)
		{
			throw new OnlineException("05", "022822", "Unable to process EcomValidator "+exp.getMessage());
		}
	}
}
