package com.transinfo.tplus.messaging.mux;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusCodes;
//import com.transinfo.tplus.messaging.cup.*;
import com.transinfo.tplus.TPlusConfig;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.TPlusOffUsRequest;
import com.transinfo.tplus.db.TransactionDB;
//import vn.com.tivn.online.BaseProcessor;
import com.transinfo.tplus.debug.DebugMsgWriter;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.hsm.HSMAdaptor;
import com.transinfo.tplus.hsm.HSMIF;
import com.transinfo.tplus.messaging.HeaderUtil;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.ParserFactory;

public class CUPRequestListener implements ISORequestListener
{
	public CUPRequestListener()
	{
	}

	protected void initialize() {
		// super.initialize();
	}

	@SuppressWarnings("static-access")
	public boolean process(ISOSource source, ISOMsg m)
	{

		try
		{
			
			System.out.println("Request received from CUP........");
			
			System.out.println("m.getPackager().toString() :: " + m.getPackager().toString());
			
			System.out.println("CUP Listener Received CUP request: "+ m.getMTI());
			System.out.println(ISOUtil.hexString(m.pack()));

			DebugMsgWriter.write("Received CUP Request:"+m.getMTI());
			m.dump(System.out,"");
			DebugWriter.writeMsgDump(m);
			//Echo
			ISOMsg res = (ISOMsg)m.clone();
			if ("0800".equals(m.getMTI()) && m.getString(70).equals("101"))
			{


				if (DebugWriter.boolDebugEnabled) DebugWriter.writeMsgDump(m);

				String F48 = m.getString(48);

				if(F48 != null && !F48.equals(""))
				{
					String PPK = F48.substring(4,36);


					System.out.println(PPK);

					byte [] PKPEncMode = new byte[]{0x00};
					byte [] PKPKeyType = new byte[]{0x01};
					byte [] PKPKS = new byte[16];
					byte [] PKPKVC = new byte[3];
					System.out.println("ImportKey");

					HSMIF hsmif = new HSMAdaptor();

					System.out.println("import key values :: " + TPlusConfig.getCUPKeyIndex()+", "+PKPKeyType+", "+PKPEncMode+", "+PPK+", "+PKPKS+", "+PKPKVC);

					hsmif.importKey(TPlusConfig.getCUPKeyIndex(),PKPKeyType,PKPEncMode,PPK,PKPKS,PKPKVC);

					String PEK_HS = ISOUtil.hexString(PKPKS);
					String PEK_KVC = ISOUtil.hexString(PKPKVC);

					TransactionDB objTranx = new TransactionDB();
					objTranx.UpdateIssuerKeys("1",PEK_HS,PEK_HS,"CUP");

				}

				res.setResponseMTI();
				res.setDirection(res.OUTGOING);
				res.unset(96);
				res.unset(48);
				res.unset(128);

				res.set(39,"00");
				System.out.println("Sending KeyExchange response "+res.pack().length);
				res.setHeader(ISOUtil.hex2byte(HeaderUtil.getResHeader(ISOUtil.hexString(res.getHeader()),res.pack().length)));
				res.dump(System.out,"");
				source.send(res);

			}
			else if ("0820".equals(m.getMTI()))
			{

				res.setResponseMTI();
				System.out.println(" Res Header.."+ISOUtil.hexString(res.getHeader()));

				if(m.getString(70).equals("001"))
				{

					TransactionDB tranx = new TransactionDB();

					tranx.UpdateSignOn("CUP","Y");

					//after sign off get the sign on lists
					TPlusConfig.connectionMap = tranx.getSignOnList();
				}else if(m.getString(70).equals("002"))
				{

					TransactionDB tranx = new TransactionDB();

					tranx.UpdateSignOn("CUP","N");

					//after sign off get the sign on lists
					TPlusConfig.connectionMap = tranx.getSignOnList();
				}

				//swap source and destination

				res.setDirection(res.OUTGOING);
				res.set(39,"00");
				res.unset(128);

				System.out.println("Sending Echo response");
				res.setHeader(ISOUtil.hex2byte(HeaderUtil.getResHeader(ISOUtil.hexString(res.getHeader()),res.pack().length)));
				res.dump(System.out,"");
				source.send(res);
			}
			else if ("0620".equals(m.getMTI()))
			{

				res.setResponseMTI();

				//swap source and destination
				res.setDirection(res.OUTGOING);
				res.set(39,"00");
				//res.set(70,"801");
				res.unset(128);
				res.unset(48);

				System.out.println("Sending Echo response");
				res.setHeader(ISOUtil.hex2byte(HeaderUtil.getResHeader(ISOUtil.hexString(res.getHeader()),res.pack().length)));
				res.dump(System.out,"");
				source.send(res);

			}
			else if("0430".equals(m.getMTI()))
			{
				DebugMsgWriter.write("Error in Response Msg");
				System.out.println("Error in Response Msg");
				return true;
			}
			else
			{

				IParser objISOParser = (IParser)ParserFactory.createPraserObject("com.transinfo.tplus.messaging.parser.TPlus8583Parser");
				objISOParser.setMsgObject(m);
				objISOParser.setParse(true);
				objISOParser.setMsgType("ISO");

				TPlusOffUsRequest tplusOffUsRequest = new TPlusOffUsRequest();
				objISOParser = tplusOffUsRequest.processRequest(objISOParser);
				ISOMsg objResMsg = objISOParser.getMsgObject();

				if(objResMsg !=null)
				{
					objResMsg.setResponseMTI();
					objResMsg.setDirection(res.OUTGOING);

					objResMsg.setPackager(new CUPPackager());

					System.out.println(HeaderUtil.getResHeader(ISOUtil.hexString(objResMsg.getHeader()),objResMsg.pack().length));

					objResMsg.setHeader(ISOUtil.hex2byte(HeaderUtil.getResHeader(ISOUtil.hexString(objResMsg.getHeader()),objResMsg.pack().length)));
					DebugWriter.write("       ");
					System.out.println("--------------------- Response To Visa -------------------");
					DebugWriter.writeMsgDump(objResMsg);
					DebugWriter.write("       ");
					objResMsg.dump(System.out,"");
					DebugWriter.write("--------------------- Response To Visa -------------------");

					source.send(objResMsg);
				}


				return true;
			}

		}catch (Exception e)
		{
			System.out.println("Message rejected");
			e.printStackTrace();
		}

		return true;
	}


	public byte[] getBinaryValue(String strValue)throws TPlusException
	{

		try
		{
			return ISOUtil.hex2byte(strValue);
		}
		catch(Exception isoExp)
		{
			throw new TPlusException(TPlusCodes.PARAM_SET_ERR,TPlusCodes.getErrorDesc(TPlusCodes.PARAM_SET_ERR)+". Error:"+isoExp.getMessage(),isoExp.getMessage());
		}

	}


}