package com.transinfo.tplus.messaging.mux;

import java.io.EOFException;
import java.io.IOException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

public class CUPChannel extends ASCIIChannel {
	
	public CUPChannel (String host, int port, ISOPackager p) {
		super(host, port, p);
	}
	
    /**
     * Waits and receive an ISOMsg over the TCP/IP session
     * @return the Message received
     * @throws IOException
     * @throws ISOException
     */
   /* public ISOMsg receive() throws IOException, ISOException {
    	
        LogEvent evt = new LogEvent (this, "receive");
        ISOMsg m = createMsg ();  // call createMsg instead of createISOMsg for 
                                  // backward compatibility
        m.setSource (this);
        try {
        	
        	super.receive();
        	
        } catch (EOFException e) {
            reconnect();
            evt.addMessage ("<peer-disconnect/>");
            throw e;
        } catch (Exception e) { 
            evt.addMessage (m);
            evt.addMessage (e);
            throw new ISOException ("unexpected exception", e);
        } finally {
            Logger.log (evt);
        }
        return m;
    }*/

}
