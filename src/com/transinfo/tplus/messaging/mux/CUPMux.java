package com.transinfo.tplus.messaging.mux;



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequest;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.header.BASE1Header;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.HeaderUtil;
import com.transinfo.tplus.messaging.parser.IParser;


public class CUPMux implements IMux  {
	public static final String VISA_CHANNEL_REALM = "visa-channel";
	public static final String VISA_MUX_REALM = "visa-mux";
	public static boolean INITIALIZED = false;
	public static boolean FirstTimeConn = true;

	static ISOMUX CUPMux;
	static BASE1Header header;
	static BaseChannel channel;
	int visaTimeOut1 = 30;
	int visaTimeOut2 = 30;
	static CUPRequestListener cupList=null;

	public CUPMux() {
	}



	public ISOMsg process(ISOMsg isomsg){
		ISOMsg requestMsg = isomsg;
		requestMsg.setDirection(requestMsg.OUTGOING);
		ISOMsg response=null;

		try
		{

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("CUPMux:CUP Connected.."+CUPMux.isConnected());

			ISOPackager packager=null;

			if(!requestMsg.getMTI().equals("0620") && !requestMsg.getMTI().equals("0100"))
			{
				System.out.println("in CUPPackager");
				packager = new CUPPackager();
			}
			else
			{
				System.out.println("in CUPPackager1");
				packager = new CUPPackager1();
			}


			requestMsg.setPackager(packager);
			//channel.setPackager(packager);

			if(requestMsg.hasField(52) && !requestMsg.getMTI().equals("0420") && !requestMsg.getMTI().equals("0820") && !requestMsg.getMTI().equals("0220") && (requestMsg.getString(3).equals("000000") || requestMsg.getString(3).equals("030000") || requestMsg.getString(3).equals("200000")))
			{
				requestMsg.set (53,"2600000000000000");
			}


			ISORequest request = new ISORequest(requestMsg);
			requestMsg.dump(System.out,"");

			System.out.println(ISOUtil.hexString(requestMsg.pack())+"  "+requestMsg.pack().length);

			System.out.println("Header="+HeaderUtil.getReqHeader(requestMsg.pack().length));

			channel.setHeader(ISOUtil.hex2byte(HeaderUtil.getReqHeader(requestMsg.pack().length)));

			//ISORequest request = new ISORequest(requestMsg);

			DebugWriter.writeMsgDump(requestMsg);
			CUPMux.queue(request);
			response = request.getResponse(visaTimeOut1*1000);

			System.out.println("Waiting for Response");
			if (response!=null && request.isTransmitted())
			{
				DebugWriter.writeMsgDump(response);
				System.out.println("Receiving response from CUP ...");
				System.out.println("Received response from CUP="+ ISOUtil.hexString(response.pack()));

			}
			else
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("CUPMux:Request Not Transmitted");
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
		System.out.println("CUPMux initialize ="+INITIALIZED+"  "+objISO.getIssuerHost()+" "+objISO.getIssuerPort());

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

		channel = new ASCIIChannel(TICMSIssuerHost, TICMSIssuerPort, new CUPPackager());
		//channel = new CUPChannel(TICMSIssuerHost, TICMSIssuerPort, new CUPPackager());

		CUPMux = new ISOMUX(channel);
		CUPMux.setISORequestListener(new CUPRequestListener());
		new Thread(CUPMux).start();

		/*if(!INITIALIZED)
		{
			Thread t = new Thread(new EchoManagerCUP());
			t.start();
		}*/

		System.out.println("before INITIALIZED");

		INITIALIZED = true;

		Logger logger = new Logger();
		logger.addListener(new SimpleLogListener(System.out));
		channel.setLogger(logger,"CACIS-CUP");

		Logger fileLogger = new Logger();

		try
		{
			System.out.println("*********************************************File Logger");
			FileOutputStream fwDebug = new FileOutputStream("/opt/cacis_be/issuer_engine/debug/ISOLog.txt",true);
			//FileOutputStream fwDebug = new FileOutputStream("E:\\Projects\\BCEL1\\Issusing\\IssusingEngine\\DEV\\ISOLog.txt",true);
			PrintStream   pwDebug = new PrintStream(fwDebug);
			fileLogger.addListener(new SimpleLogListener(pwDebug));
			channel.setLogger(fileLogger,"ISOCACIS");
		}
		catch(Exception exp)
		{
			System.out.println(exp);
		}


	}
}
