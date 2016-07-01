package com.transinfo.tplus.messaging.validator;

import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.hsm.HSMIF;
import com.transinfo.tplus.javabean.CardInfo;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.parser.IParser;

@SuppressWarnings("unused")
public class PINValidator implements BaseValidator {

	boolean isMandatory = false;
	public PINValidator(boolean isMandatory)
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
		System.out.println("PIN Validation Processing...");

		try
		{

			System.out.println("PINValidator - objISO.isEComTranx()..." + objISO.isEComTranx());
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINValidator - objISO.isEComTranx()..." + objISO.isEComTranx());

			if(objISO.isEComTranx())
			{

				System.out.println("PINValidator - Return here");
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINValidator - Return here");

				return true;
			}

			String cardScheme = objISO.getCardProduct();

			if(!objISO.hasField(52) && !isMandatory)
			{

				if("CU".equals(cardScheme)){

					String f22 = objISO.getValue(22);
					System.out.println("f22 :: "+f22);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("f61 :: "+f22);

					String f25 = objISO.getValue(25);
					System.out.println("f25 :: "+f25);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("f25 :: "+f25);

					String f6035 = "";
					String f60 = objISO.getValue(60);
					if(f60 != null && !"".equals(f60) && f60.length() >= 23){
						f6035 = f60.substring(22,23);
						System.out.println("f6035 :: "+f6035);
					}
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("f6035 :: "+f6035);

					if(("021".equals(f22) || "021".equals(f22)) && "00".equals(f25) && "2".equals(f6035)){
						throw new OnlineException("55", "022822", "No PIN Data Available for CAT");
					}

				}

				return true;
			}

			if(!objISO.hasField(52) && isMandatory)
			{
				throw new OnlineException("05", "022822", "No PIN Data Available");
			}

			HSMIF hsmAdaptor = new HSMAdaptor();
			TransactionDB objTranxDB = new TransactionDB();
			CardInfo objCardInfo = objISO.getCardDataBean();

			String oc = objCardInfo.getOc();

			// CUP validation
			String strF61 = objISO.getStrF61();

			if("CU".equals(cardScheme)){
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
			}

			//if(!"0".equals(oc)){

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PIN Validation: Values....." + objCardInfo.getCardProductId() + ", " + objCardInfo.getIssuerId() + ", " + objCardInfo.getCardnumber() + ", " + objCardInfo.getPVVOFFSET() + ", " + objISO.getValue(52) + ", " + objISO.getTransactionDataBean().getAcqBinType());

			String strPinChangeStatus = "Y";
			int verRes = hsmAdaptor.verifyPIN_PVV(objCardInfo.getCardProductId(),objCardInfo.getIssuerId(),objCardInfo.getCardnumber(),objCardInfo.getPVVOFFSET(),objISO.getValue(52),objISO.getTransactionDataBean().getAcqBinType());
			if(verRes!=0)
			{

				if(Integer.parseInt(objCardInfo.getWrongPINCount())+1>2)
				{
					strPinChangeStatus = "N";
				}

				//objTranxDB.UpdatePinCnt(objISO.getCardNumber(),strPinChangeStatus,Integer.parseInt(objCardInfo.getWrongPINCount())+1);
				objTranxDB.UpdatePinCntAndDate(objISO.getCardNumber(),strPinChangeStatus,Integer.parseInt(objCardInfo.getWrongPINCount())+1);

				if (DebugWriter.boolDebugEnabled) DebugWriter.write("PIN Validation: PIN Validation Failed....."+verRes);
				System.out.println("****WithDrawalRequest: PIN Validation Failed *****"+verRes);

				if("CU".equals(cardScheme)){
					if(!"".equals(strF61)){
						String secValue = "";
						if(strF61.length()>32){
							secValue = strF61.substring(32, strF61.length());
						}
						strF61 = strF61.substring(0, 23)+"2"+strF61.substring(24,27)+strF61.substring(27, 31)+strF61.substring(31, 32)+secValue;
						objISO.setStrF61(strF61);
					}
				}

				throw new OnlineException("55","A05374","Incorrect PIN ");

			}
			else
			{

				if("CU".equals(cardScheme)){
					if(!"".equals(strF61)){
						String secValue = "";
						if(strF61.length()>32){
							secValue = strF61.substring(32, strF61.length());
						}
						strF61 = strF61.substring(0, 23)+"1"+strF61.substring(24,27)+strF61.substring(27, 31)+strF61.substring(31, 32)+secValue;
						objISO.setStrF61(strF61);
					}
				}

				//objTranxDB.UpdatePinCnt(objISO.getCardNumber(),"Y",0);
				objTranxDB.UpdatePinCntAndDate(objISO.getCardNumber(),"Y",0);

			}

			//}

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
