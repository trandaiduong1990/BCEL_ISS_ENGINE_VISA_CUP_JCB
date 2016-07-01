package com.transinfo.tplus.messaging.validator;


import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.parser.IParser;

@SuppressWarnings("unused")
public class CVVOnTrack2Validator implements BaseValidator {
	public CVVOnTrack2Validator() {
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
		System.out.println(" In CVVOnTrack2Validator process...");

		System.out.println("CVVOnTrack2Validator - objISO.isEComTranx()..." + objISO.isEComTranx());
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("CVVOnTrack2Validator - objISO.isEComTranx()..." + objISO.isEComTranx());

		if(objISO.isEComTranx())
		{

			System.out.println("CVVOnTrack2Validator - Return here");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("CVVOnTrack2Validator - Return here");

			return true;
		}

		String track2 = objISO.getValue(35);
		if (track2==null ||(track2.equals("")))
			return true;
		System.out.println("track2="+track2);
		if(track2.split("=")[1].length()<10)
		{
			throw new OnlineException("05", "022822", "Invalid Track2 Data with missing fields to calculate CVV");
		}
		String expDate = track2.split("=")[1].substring(0,4);
		String svcCode = track2.split("=")[1].substring(4,7);
		//String CVV = track2.split("=")[1].substring(7,10);
		String CVV = track2.split("=")[1].substring(17,20);

		String cardScheme = objISO.getCardProduct();
		if("JC".equals(cardScheme)){
			CVV = track2.split("=")[1].substring(12, 15);
		}

		String cardNumber = track2.split("=")[0];

		CardInfo objCardInfo = objISO.getCardDataBean();
		System.out.println("Track2 =="+expDate+"  "+svcCode+"   "+cardNumber+"  "+CVV);
		if(expDate == null || expDate.equals("") || svcCode == null || svcCode.equals("") || CVV == null || CVV.equals(""))
		{
			throw new OnlineException("05", "022822", "Invalid Track2 Data with missing fields to calculate CVV");
		}

		String strCVV = ("000"+objCardInfo.getCVV()).substring(objCardInfo.getCVV().length());
		System.out.println("CVV"+strCVV);

		try
		{
			HSMAdaptor hsmAdaptor = new HSMAdaptor();

			String strF44 = objISO.getF44();

			// CUP validation
			String strF61 = objISO.getStrF61();

			//String cardScheme = objISO.getCardProduct();

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

			String responseCode = "05";

			int verRes = -1;

			String f22 = "";
			String f22Emv = "";

			if("JC".equals(cardScheme)){

				f22 = objISO.getValue(22);

				if(f22 != null && !"".equals(f22)){
					f22Emv = f22.substring(0, 2);
				}

			}

			if(objISO.hasField(55) || "AUTHCOMPLETE".equals(objISO.getTranxType()) || "REFUND".equals(objISO.getTranxType()) || (!"".equals(f22Emv) && "05".equals(f22Emv))){

				verRes = hsmAdaptor.verifyCVV_I(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),cardNumber,expDate,"999",CVV,objISO.getTransactionDataBean().getAcqBinType());
				if(verRes!=0)
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation Failed..... "+verRes);
					System.out.println("**** CVV Validation Failed *****"+verRes);

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

						responseCode = "59";

					}

					throw new OnlineException(responseCode,"A05374","CVV Validation Failed ");

				}
				else
				{
					System.out.println("CVV Validation Sucessful...."+strF44+"   "+strF44.length());
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

			}else{

				verRes = hsmAdaptor.verifyCVV(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),cardNumber,expDate,svcCode,CVV,objISO.getTransactionDataBean().getAcqBinType());
				if(verRes!=0)
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVN Validation Failed....."+verRes);
					System.out.println("**** CVV Validation Failed *****"+verRes);

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

						responseCode = "59";

					}

					throw new OnlineException(responseCode,"A05374","CVV Validation Failed ");

				}
				else
				{

					strF44 = strF44.substring(0,4)+"2"+strF44.substring(5,8);
					objISO.setF44(strF44);
					System.out.println(strF44.length());

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

			}

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> CVVOnTrack2Validation is Successful.....");

		}catch (OnlineException oe)
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("Exp..."+oe.getMessage());
			oe.printStackTrace();
			throw new OnlineException(oe);
		}catch (Exception e)
		{
			e.printStackTrace();
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("Exp..."+e);
			throw new OnlineException("14", "022822", "Unable to process CVV on track 2 for card number:  "+cardNumber);
		}
		return true;
	}

	public static void main(String s[])
	{

		String track1="B4665311450000186^CARD HOLDER NAME^2112123393000";
		int firstSeparator = track1.indexOf("^");
		int secondSeparator = track1.indexOf("^", firstSeparator+1);
		String expDate = track1.substring(secondSeparator+1,secondSeparator+5);
		String svcCode = track1.substring(secondSeparator+5,secondSeparator+8);
		String CVV = track1.substring(secondSeparator+8,secondSeparator+11);
		String cardNumber = track1.substring(0,firstSeparator);

		System.out.println(firstSeparator+"  "+secondSeparator+"  "+expDate+"  "+svcCode+"  "+CVV+"  "+cardNumber);


	}

}
