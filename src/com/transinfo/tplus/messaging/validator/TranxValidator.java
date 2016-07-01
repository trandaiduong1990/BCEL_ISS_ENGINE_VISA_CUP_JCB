
package com.transinfo.tplus.messaging.validator;


import com.transinfo.tplus.TPlusInterface;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.OnlineException;
import com.transinfo.tplus.messaging.parser.IParser;

public class TranxValidator implements BaseValidator
{

	@Override
	public boolean process(IParser objISO) throws OnlineException {

		boolean res = true;

		try{

			String cardScheme = objISO.getCardProduct();
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: cardScheme :: " + cardScheme);

			// MOTO, ECOM, RECUR, INSTAL,ACCVERI

			// VISA
			if(TPlusInterface.ICardScheme.VISA.equals(cardScheme))
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(3) :: " + objISO.getValue(3));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(4) :: " + objISO.getValue(4));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(25) :: " + objISO.getValue(25));

				// ACCVERI
				if(objISO.hasField(25) && objISO.getValue(25).equals("51") && (!("30".equals(objISO.getValue(3).substring(0,2))) && !("31".equals(objISO.getValue(3).substring(0,2)))) && (new Double(objISO.getValue(4)).doubleValue()==0)) 
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("ACCVERI");
					return true;
				}

				// ECOM
				if(objISO.hasField(25) && "59".equals(objISO.getValue(25)))
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("ECOM");
				}

				// MOTO
				else if(objISO.hasField(25) && "08".equals(objISO.getValue(25)))
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("MOTO");
				}

			}

			// CUP
			else if(TPlusInterface.ICardScheme.CUP.equals(cardScheme))
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(3) :: " + objISO.getValue(3));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(25) :: " + objISO.getValue(25));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(60) :: " + objISO.getValue(60));

				// ACCVERI
				if("33".equals(objISO.getValue(3).substring(0,2)))
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("ACCVERI");
					return true;
				}

				// ECOM
				String f60 = objISO.getValue(60);
				if(f60 != null && !"".equals(f60) && f60.length() >= 14)
				{
					String eci = f60.substring(12,14);
					if("10".equals(eci) || "09".equals(eci))
					{
						objISO.getTransactionDataBean().setTranxCodeSubType("ECOM");
						return true;
					}
				}

				// RECUR
				if(objISO.hasField(25) && "28".equals(objISO.getValue(25)))
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("RECUR");
				}

				// MOTO
				else if(objISO.hasField(25) && "08".equals(objISO.getValue(25)))
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("MOTO");
				}

				// INSTAL
				else if(objISO.hasField(25) && "64".equals(objISO.getValue(25)))
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("INSTAL");
				}

			}

			// JCB
			else if(TPlusInterface.ICardScheme.JCB.equals(cardScheme))
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(22) :: " + objISO.getValue(22));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(25) :: " + objISO.getValue(25));
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("TranxValidator :: objISO.getValue(61) :: " + objISO.getValue(61));

				// ECOM
				String f22eci = objISO.getValue(22).substring(0, 2);
				if("81".equals(f22eci)){
					objISO.getTransactionDataBean().setTranxCodeSubType("ECOM");
					return true;
				}

				// RECUR
				if(objISO.getValue(61) != null)
				{
					String f61jcbRecur = objISO.getValue(61).substring(1, 2);
					if("1".equals(f61jcbRecur))
					{
						objISO.getTransactionDataBean().setTranxCodeSubType("RECUR");
						return true;
					}
				}

				// MOTO
				if(objISO.hasField(25) && "08".equals(objISO.getValue(25)))
				{
					objISO.getTransactionDataBean().setTranxCodeSubType("MOTO");
				}

			}

		}
		catch (OnlineException e)
		{
			res = false;
			throw new OnlineException(e);
		}
		catch (Exception e)
		{
			res = false;
			throw new OnlineException("96","G0001","System Error");
		}
		finally
		{
			if(res)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> TranxValidator is Successful.....");
			}else
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write(">>> TranxValidator is NOT Successful.....");
			}
		}

		return res;
	}

}