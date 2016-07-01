package com.transinfo.tplus.messaging.mux;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequest;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.VAPChannel;
import org.jpos.iso.channel.NCCChannel;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.header.BASE1Header;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.parser.IParser;
import com.transinfo.tplus.messaging.TIVNThread;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.HeaderUtil;


public class CUPMux1 implements IMux  {
	public static final String VISA_CHANNEL_REALM = "visa-channel";
	public static final String VISA_MUX_REALM = "visa-mux";
	public static boolean INITIALIZED = false;
	public static boolean FirstTimeConn = true;

	static ISOMUX CUPMux1;
	static BASE1Header header;
	static BaseChannel channel1;
	int visaTimeOut1 = 60;
	int visaTimeOut2 = 60;
	static CUPRequestListener cupList=null;

	public CUPMux1() {
	}



	public ISOMsg process(ISOMsg isomsg){
		ISOMsg requestMsg = isomsg;
		requestMsg.setDirection(requestMsg.OUTGOING);


		ISOMsg response=null;

		try {
			//ISOPackager packager=packager = new NARADAPackager();
			System.out.println("000");
			ISOPackager packager=null;
			System.out.println("00011");
			if(!requestMsg.getMTI().equals("0620") && !requestMsg.getMTI().equals("0100"))
			{
				System.out.println("in 0100,,,,,,,");
				packager = new CUPPackager();
			}
			else
			{
				System.out.println("not in 0100,,,,,,,");
				packager = new CUPPackager1();
			}

			System.out.println("0002");
			requestMsg.setPackager(packager);
			channel1.setPackager(packager);
			System.out.println("0001");
			ISORequest request = new ISORequest(requestMsg);
			requestMsg.dump(System.out,"");

			channel1.setHeader(ISOUtil.hex2byte(HeaderUtil.getReqHeader(requestMsg.pack().length)));
			System.out.println("000111");
			if(!requestMsg.getMTI().equals("0420") && !requestMsg.getMTI().equals("0820") && !requestMsg.getMTI().equals("0220") && (requestMsg.getString(3).equals("000000") || requestMsg.getString(3).equals("030000") || requestMsg.getString(3).equals("200000")))
			{
				requestMsg.set (53,"2600000000000000");
			}
			//ISORequest request = new ISORequest(requestMsg);
			CUPMux1.queue(request);
			response = request.getResponse(visaTimeOut1*1000);

			System.out.println("Waiting for Response");
			if (response!=null && request.isTransmitted())
			{
				DebugWriter.writeMsgDump(response);
				System.out.println("Receiving response from CUP ...");
				System.out.println("Received response from CUP="+ ISOUtil.hexString(response.pack()));
				response.dump(System.out,"");
			}
			else
			{
				if(!request.isTransmitted())
				{
					packager = new VSDCPOSPackager();
					isomsg.setPackager(packager);
					isomsg.set(39, "91");
					return isomsg;
				}

			}

			if (response !=null)
			{
				response.dump(System.out,"");
				if(response.getString(39).equals("98"))
				{
					return null;
				}

			}



		} catch (Exception e) {
			System.out.println(e);
			ISOMsg responseMsg = (ISOMsg)requestMsg;
			try{
				ISOPackager packager = new VSDCPOSPackager();
				isomsg.setPackager(packager);
				isomsg.set(39, "96");
				return isomsg;
			}catch(Exception ignore){}

		}


		return response;

	}


	public void initialize(IParser objISO)
	{
		System.out.println("CUP1="+INITIALIZED+"  "+objISO.getIssuerHost()+" "+objISO.getIssuerPort());

		if (INITIALIZED)
			return;

		FirstTimeConn = true;

		String TICMSIssuerHost = objISO.getIssuerHost();
		int TICMSIssuerPort = -1;
		try
		{
			TICMSIssuerPort = new Integer(objISO.getIssuerPort()).intValue();
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		String visaStationId = objISO.getStationId();

		channel1 = new ASCIIChannel(TICMSIssuerHost, TICMSIssuerPort, new CUPPackager());

		CUPMux1 = new ISOMUX(channel1);
		CUPMux1.setISORequestListener(new CUPRequestListener());
		new Thread(CUPMux1).start();
		
		/*System.out.println("CUPMux1 initialize t.start()");
		if(!INITIALIZED)
		{
			Thread t = new Thread(new EchoManagerCUP());
			t.start();
		}*/

		INITIALIZED = true;

	}
}
