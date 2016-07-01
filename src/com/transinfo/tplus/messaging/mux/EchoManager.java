package com.transinfo.tplus.messaging.mux;

import java.util.Date;
import java.util.Map;

import com.transinfo.tplus.TPlusConfig;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.parser.IParser;

public class EchoManager implements Runnable {

	@Override
	public void run() {

		while(true){

			try{

				Thread.sleep(180*1000);

				System.out.println("In Periodic JCBECHO. Time :: " + new Date());
				com.transinfo.tplus.messaging.credit.jcb.EchoRequest objEchoRequest = new com.transinfo.tplus.messaging.credit.jcb.EchoRequest();
				Map connectionMap = TPlusConfig.getConnectionMap();
				IParser parser = (IParser)connectionMap.get("JCB");

				if(parser != null){

					if("Y".equals(parser.getSignOnNeeded())){

						if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager:Already SignOn to JCB");
						System.out.println("EchoManager:Already SignOn to JCB");

						parser = objEchoRequest.execute(parser);

						if(parser != null){

							String strStatus = "";

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager parser is NOT null");
							System.out.println("EchoManager parser is NOT null");

							if(parser.getValue(39).equals("00"))
							{
								strStatus = "JCBECHO Message is Successfull";
							}
							else
							{
								strStatus = "JCBECHO Message is not Successfull";
							}

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("Periodic Echo Status :: " + strStatus);
							System.out.println("Periodic Echo Status :: " + strStatus);

						}else{
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager response parser is null");
							System.out.println("EchoManager response parser is null");					
						}

					}else{
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager: NOT yet SignOn to JCB");
						System.out.println("EchoManager: NOT yet SignOn to JCB");	
					}

				}else{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("connectionMap.get('JCB') parser is null");
					System.out.println("connectionMap.get('JCB') parser is null");
				}

			}catch(Exception e){
				System.out.println("EchoManager :: " + e);
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager :: " + e);
			}

		}

	}

}
