package com.transinfo.tplus.messaging.validator;


import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.parser.IParser;




public class DataValidator implements BaseValidator
{
	public DataValidator()
	{
	}

	public boolean process(IParser objISO) throws OnlineException
	{
		System.out.println(" In DataValidator process...");

		try
		{

			System.out.println("DataValidator - objISO.isEComTranx()..." + objISO.isEComTranx());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - objISO.isEComTranx()..." + objISO.isEComTranx());

			if(objISO.isEComTranx())
			{

				System.out.println("DataValidator - Return here");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - Return here");

				return true;
			}

			String cardScheme = objISO.getCardProduct();
			String tranxType = objISO.getTransactionDataBean().getAcqBinType();

			System.out.println("DataValidator - tranxType..." + tranxType);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - tranxType..." + tranxType);

			if("OFFUS".equals(tranxType))
				//if(objISO.getMTI().equals("0100") || objISO.getMTI().equals("0101"))
			{

				//int[] dataFields  = {3,4,7,11,18,19,22,25,32,37,42,43,49,60};
				int[] dataFields  = {3,4,7,11,18,19,22,25,32,37,42,43,49};
				int[] dataFieldsForBal  = {3,7,11,18,19,22,25,32,37,42,43,49};
				//int[] dataFieldsJCB  = {3,4,7,11,18,22,25,32,33,37,42,43,49};
				int[] dataFieldsJCB  = {3,4,7,11,18,22,25,32,33,37,42,49};

				//int[] dataMotoFields  = {3,4,7,11,18,19,22,25,32,37,43,49,60};
				int[] dataMotoFields  = {3,4,7,11,18,19,22,25,32,37,43,49};
				//int[] dataMotoFieldsCUP  = {3,4,7,11,18,19,22,25,32,37,43,49,61};
				int[] dataMotoFieldsCUP  = {3,4,7,11,18,19,22,25,32,37,43,49};
				int[] dataMotoFieldsJCB  = {3,4,7,11,18,22,25,32,37,43,49};

				System.out.println("DataValidator - objISO.getValue(25)..." + objISO.getValue(25));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - objISO.getValue(25)..." + objISO.getValue(25));

				System.out.println("DataValidator - objISO.getValue(3)..." + objISO.getValue(3));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - objISO.getValue(3)..." + objISO.getValue(3));

				if(!objISO.getValue(25).equals("08"))
				{
					if("JC".equals(cardScheme)){
						if(!objISO.hasFields(dataFieldsJCB))
						{
							throw new OnlineException("05", "022734","Some of the Mandatory fields not Available in Normal JCB Request.. ");
						}
					}else{

						if(objISO.getValue(3).substring(0,2).equals("30") || objISO.getValue(3).substring(0,2).equals("31")){
							if(!objISO.hasFields(dataFieldsForBal))
							{
								throw new OnlineException("05", "022734","Some of the Mandatory fields not Available in Normal Bal Request.. ");
							}
						}else{
							if(!objISO.hasFields(dataFields))
							{
								throw new OnlineException("05", "022734","Some of the Mandatory fields not Available in Normal Request.. ");
							}
						}

					}

				}else{

					// assign transaction code sub type
					objISO.getTransactionDataBean().setTranxCodeSubType("MOTO");

					if("CU".equals(cardScheme)){

						if(!objISO.hasFields(dataMotoFieldsCUP)){
							throw new OnlineException("05", "022734","Some of the Mandatory fields not Available in CUP Moto Request.. ");
						}

					}else if("JC".equals(cardScheme)){

						if(!objISO.hasFields(dataMotoFieldsJCB)){
							throw new OnlineException("05", "022734","Some of the Mandatory fields not Available in CUP Moto Request.. ");
						}

					}else{

						if(!objISO.hasFields(dataMotoFields)){
							throw new OnlineException("05", "022734","Some of the Mandatory fields not Available in Moto Request.. ");
						}

					}

				}

				if(!("30".equals(objISO.getValue(3).substring(0,2))) && !("31".equals(objISO.getValue(3).substring(0,2)))){

					if(!(new Double(objISO.getValue(4)).doubleValue()>0) && !objISO.isAccountVerification())
					{
						throw new OnlineException("05", "022734","Invalid Amount Value.. "+objISO.getValue(4));
					}

				}

				System.out.println("2");

				String f61jcbMoto = "";
				String f61jcbRecur = "";
				String f61jcbPreAuth = "";
				// get JCB values for validation
				if("JC".equals(cardScheme)){

					if(objISO.getValue(61) != null){

						f61jcbMoto = objISO.getValue(61).substring(0, 1);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - JCB f61Moto..." + f61jcbMoto);

						f61jcbRecur = objISO.getValue(61).substring(1, 2);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - JCB f61Recur..." + f61jcbRecur);

						f61jcbPreAuth = objISO.getValue(61).substring(2, 3);
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("DataValidator - JCB f61PreAuth..." + f61jcbPreAuth);

					}

					//recurring transaction also must F14
					if("1".equals(f61jcbRecur)){

						// assign transaction code sub type
						objISO.getTransactionDataBean().setTranxCodeSubType("RECUR");

						String f14ExpDate = objISO.getValue(14);
						System.out.println("f14ExpDate :: " + f14ExpDate);

						if(f14ExpDate == null || "".equals(f14ExpDate)){
							throw new OnlineException("54", "022734","F14 must to JCB RECUR");
						}

						String f48AD = objISO.getValue(48);
						System.out.println("f48AD :: " + f48AD);

					}

				}

				if(objISO.getValue(25).equals("08")) // MOTO Indicator Checking
				{
					if("VI".equals(cardScheme)){

						if(objISO.getValue(60).length()>=10)
						{
							System.out.println("objISO.getValue(60)"+objISO.getValue(60)+"  "+objISO.getValue(60).substring(8,10));
							String strMotoInd = objISO.getValue(60).substring(8,10);
							if(!strMotoInd.equals("01") && !strMotoInd.equals("02") && !strMotoInd.equals("05") && !strMotoInd.equals("06") && !strMotoInd.equals("07") && !strMotoInd.equals("08"))
							{
								throw new OnlineException("05", "022734","Moto Indicator not Correct in F60");
							}
						}

					}else if("JC".equals(cardScheme)){

						if((!"1".equals(f61jcbMoto)) || (("1".equals(f61jcbMoto)) && ("1".equals(f61jcbRecur) || "1".equals(f61jcbPreAuth)) )){
							throw new OnlineException("05", "022734","Moto Indicator not Correct in F61 for JCB");
						}

						String f14ExpDate = objISO.getValue(14);
						System.out.println("f14ExpDate :: " + f14ExpDate);

						if(f14ExpDate == null || "".equals(f14ExpDate)){
							throw new OnlineException("54", "022734","F14 must to JCB MOTO");
						}

						/*String f48AD = objISO.getValue(48);
						System.out.println("f48AD :: " + f48AD);

						if(f48AD == null || "".equals(f48AD)){
							throw new OnlineException("05", "022734","F48 must to JCB MOTO");
						}*/

					}else{
						
						// CUP Recurring

						if(objISO.getValue(60).length()>=23)
						{
							String strMotoIndCUP = objISO.getValue(60).substring(22,23);
							if(!strMotoIndCUP.equals("4") && !strMotoIndCUP.equals("3"))
							{
								throw new OnlineException("05", "022734","Moto Indicator not Correct in F60");
							}
						}

					}
				}

				if("CU".equals(cardScheme)){
					
					if(objISO.getValue(25).equals("28")) // Recurring
					{
						
						// assign transaction code sub type
						objISO.getTransactionDataBean().setTranxCodeSubType("RECUR");
						
						/*if(objISO.getValue(60).length()>=23)
						{
							String strRecurIndCUP = objISO.getValue(60).substring(22,23);
							String f6025 = objISO.getValue(60).substring(8,10);
							if(!strRecurIndCUP.equals("4") && !"".equals(f6025)){
								throw new OnlineException("05", "022734","Recurring Indicator not Correct in F60");
							}
						}*/
						
					}else if(objISO.getValue(25).equals("64")) // Installment
					{
						
						// assign transaction code sub type
						objISO.getTransactionDataBean().setTranxCodeSubType("INSTAL");
						
						if(objISO.getValue(48) == null || "".equals(objISO.getValue(48))){
							throw new OnlineException("05", "022734","Installment Indicator not Correct in F48");
						}else{

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("objISO.getValue(48) :: " + objISO.getValue(48));
							String f48 = new String(ISOUtil.hex2byte(objISO.getValue(48)));
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("f48 :: "+f48);
							
							if(f48.length()>=2 && !"IP".equals(f48))
							{
								throw new OnlineException("05", "022734","Installment Indicator not Correct in F48");
							}
						}
					}

				}

				System.out.println("3");
				if(objISO.hasField(18))
				{
					System.out.println("3 "+objISO.getValue(2).substring(0,2)+"  "+objISO.getValue(18));
					// Check for the MCC value is valid.
					if(objISO.getValue(3).substring(0,2).equals("01"))
					{
						if(!objISO.getValue(18).equals("4829") && !objISO.getValue(18).equals("6051") && !objISO.getValue(18).equals("6010") && !objISO.getValue(18).equals("6011") && !objISO.getValue(18).equals("7995"))
						{
							throw new OnlineException("05", "022734","Invalid MCC.. "+objISO.getValue(18));
						}
					}
				}
				
				System.out.println("5");
				if(objISO.hasField(52) && !objISO.hasField(53))
				{
					throw new OnlineException("05", "022734","F53 is not avaliable when F52 exists. " +objISO.getValue(22));

				}

			}
			System.out.println("6");

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