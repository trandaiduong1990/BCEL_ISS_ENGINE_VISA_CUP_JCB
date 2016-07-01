package com.transinfo.tplus.messaging.validator;

import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.parser.IParser;

@SuppressWarnings("unused")
public class CVVOnTrack1Validator implements BaseValidator {
	public CVVOnTrack1Validator() {
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
		System.out.println(" In CVVOnTrack1Validator process...");

		System.out.println("CVVOnTrack1Validator - objISO.isEComTranx()..." + objISO.isEComTranx());
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("CVVOnTrack1Validator - objISO.isEComTranx()..." + objISO.isEComTranx());

		if(objISO.isEComTranx())
		{

			System.out.println("CVVOnTrack1Validator - Return here");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("CVVOnTrack1Validator - Return here");

			return true;
		}

		String track1 = objISO.getValue(45);
		if (track1==null ||(track1.equals("")))
			return true;

		int firstSeparator = track1.indexOf("^");
		int secondSeparator = track1.indexOf("^", firstSeparator+1);
		String expDate = track1.substring(secondSeparator+1,secondSeparator+5);
		String svcCode = track1.substring(secondSeparator+5,secondSeparator+8);
		//String CVV = track1.substring(secondSeparator+10,secondSeparator+13);
		String CVV = track1.substring(secondSeparator+23,secondSeparator+26);
		String cardNumber = objISO.getCardNumber();

		CardInfo objCardInfo = objISO.getCardDataBean();
		System.out.println("Track2 =="+expDate+"  "+svcCode+"   "+cardNumber+"  "+CVV);

		if(expDate == null || expDate.equals("") || svcCode == null || svcCode.equals("") || CVV == null || CVV.equals(""))
		{
			throw new OnlineException("05", "022822", "Invalid Track1 Data with missing fields to calculate CVV");
		}

		//expDate = expDate.substring(2,4)+expDate.substring(0,2);
		String strCVV = ("000"+objCardInfo.getCVV()).substring(objCardInfo.getCVV().length());
		System.out.println("CVV"+strCVV+"expdate="+expDate);

		/*if(!CVV.equals(strCVV))
	{
		throw new OnlineException("05", "022822", "Invalid CVV in Track2 Data ");
	}*/

		try
		{

			HSMAdaptor hsmAdaptor = new HSMAdaptor();

			String strF44 = objISO.getF44();

			// CUP validation
			String strF61 = objISO.getStrF61();

			String cardScheme = objISO.getCardProduct();

			if("CU".equals(cardScheme)){
				String reqF61 = objISO.getValue(61);
				if(reqF61 != null && !"".equals(reqF61)){
					String secValue = "";
					if(reqF61.length()>32){
						secValue = reqF61.substring(32, reqF61.length());
					}
					strF61 = strF61.substring(0, 24)+reqF61.substring(24,27)+strF61.substring(27, 31)+strF61.substring(31, 32)+secValue;
				}else{
					//strF61 = "";
				}
			}

			int verRes = hsmAdaptor.verifyCVV(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),cardNumber,expDate,svcCode,CVV,objISO.getTransactionDataBean().getAcqBinType());
			if(verRes!=0)
			{

				if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation Failed on Track1....."+verRes);
				System.out.println("**** CVV Validation Failed Track1*****"+verRes);

				strF44 = strF44.substring(0,4)+"1"+strF44.substring(5,8);
				objISO.setF44(strF44);

				if("CU".equals(cardScheme)){
					if(!"".equals(strF61)){
						String secValue = "";
						if(strF61.length()>32){
							secValue = strF61.substring(32, strF61.length());
						}
						strF61 = strF61.substring(0, 22)+"2"+strF61.substring(23, 24)+strF61.substring(24,27)+strF61.substring(27, 31)+strF61.substring(31, 32)+secValue;
						objISO.setStrF61(strF61);
					}
				}

				throw new OnlineException("05","A05374","CVV Validation Failed on Track1");
			}
			else
			{

				strF44 = strF44.substring(0,4)+"2"+strF44.substring(5,8);
				objISO.setF44(strF44);

				if("CU".equals(cardScheme)){
					if(!"".equals(strF61)){
						String secValue = "";
						if(strF61.length()>32){
							secValue = strF61.substring(32, strF61.length());
						}
						strF61 = strF61.substring(0, 22)+"1"+strF61.substring(23, 24)+strF61.substring(24,27)+strF61.substring(27, 31)+strF61.substring(31, 32)+secValue;
						objISO.setStrF61(strF61);
					}
				}

				System.out.println("CVV Validation is Successful");
			}

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVVOnTrack2Validation is Successful.....");

		}catch (OnlineException oe)
		{
			oe.printStackTrace();
			throw new OnlineException(oe);
		}catch (Exception e)
		{
			e.printStackTrace();
			throw new OnlineException("14", "022822", "Unable to process CVV on track 1 for card number:  "+cardNumber);
		}
		return true;
	}

	public static void main(String s[])
	{
		String str="          M";
		System.out.println(str.substring(0,9));
		System.out.println(str.substring(0,9)+"M");
	}


}
