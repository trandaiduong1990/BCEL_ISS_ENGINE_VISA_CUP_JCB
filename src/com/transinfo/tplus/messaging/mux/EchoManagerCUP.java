package com.transinfo.tplus.messaging.mux;

import java.util.Map;

import com.transinfo.tplus.TPlusConfig;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.parser.IParser;

public class EchoManagerCUP implements Runnable {

	@Override
	public void run() {

		while(true){

			try{

				Thread.sleep(300*1000);

				System.out.println("In Periodic CUPECHO");
				com.transinfo.tplus.messaging.credit.cup.EchoRequest objEchoRequest = new com.transinfo.tplus.messaging.credit.cup.EchoRequest();
				Map connectionMap = TPlusConfig.getConnectionMap();
				IParser parser = (IParser)connectionMap.get("CUP");

				if(parser != null){

					if("Y".equals(parser.getSignOnNeeded())){

						if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager:Already SignOn to CUP");
						System.out.println("EchoManager:Already SignOn to CUP");

						parser = objEchoRequest.execute(parser);

						if(parser != null){

							String strStatus = "";

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager parser is NOT null");
							System.out.println("EchoManager parser is NOT null");

							if(parser.getValue(39).equals("00"))
							{
								strStatus = "CUPECHO Message is Successfull";
							}
							else
							{
								strStatus = "CUPECHO Message is not Successfull";
							}

							if (DebugWriter.boolDebugEnabled) DebugWriter.write("Periodic Echo CUP Status :: " + strStatus);
							System.out.println("Periodic Echo CUP Status :: " + strStatus);

						}else{
							if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager response parser is null");
							System.out.println("EchoManager response parser is null");					
						}

					}else{
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManager: NOT yet SignOn to CUP");
						System.out.println("EchoManager: NOT yet SignOn to CUP");	
					}

				}else{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("connectionMap.get('CUP') parser is null");
					System.out.println("connectionMap.get('CUP') parser is null");
				}

			}catch(Exception e){
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("EchoManagerCUP :: " + e);
			}

		}

	}

}
