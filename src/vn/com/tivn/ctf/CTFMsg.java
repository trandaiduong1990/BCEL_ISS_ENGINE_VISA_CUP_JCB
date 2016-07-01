// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CTFMsg.java

package vn.com.tivn.ctf;

import java.io.*;
import java.util.*;

// Referenced classes of package vn.com.tivn.ctf:
//            Trailer, LogicalTranx, DraftData, DocumentRequest,
//            FeeCollectionRequest, MessageFactory, TC

public class CTFMsg
{

    public CTFMsg()
    {
        batchList = new Vector();
        TCRs = new Hashtable();
        Draft0s = new Vector();
        Draft1s = new Vector();
        DocRequests = new Vector();
        FeeCollectionRequests = new Vector();
        Misc = new Vector();
        loaded = false;
        fileTrailer = new Trailer();
        fileTrailer.setTypeFileTrailer();
    }

    public void add(LogicalTranx tranx) throws Exception
    {
        Object obj;
        System.out.println("add1");
        for(Enumeration enmrt = tranx.getTCRs(); enmrt.hasMoreElements(); TCRs.put(obj, obj))
            obj = enmrt.nextElement();
		System.out.println("add2");
        batchList.add(tranx);
    }

    public void write2Stream(OutputStream out)
        throws Exception
    {
        long fileTotal = 0L;
        int fileCount = 0;
        int monetaryTranxCount = 0;
        int TCCount = 0;
        String result = "";
        for(Enumeration enmrt = batchList.elements(); enmrt.hasMoreElements();)
        {
            LogicalTranx tnx = (LogicalTranx)enmrt.nextElement();
            tnx.write2Stream(out);
            //System.out.println(tnx.get("TCR"));

            Trailer tl = tnx.getTrailer();
            fileTotal += Long.parseLong(tl.get("SourceAmt"));
            TCCount += Integer.parseInt(tl.get("NoOfTCRs"));
            fileCount += Integer.parseInt(tl.get("NoOfTranx"));
            monetaryTranxCount += Integer.parseInt(tl.get("NoOfMonetaryTranx"));
        }

        fileTrailer.set("NoOfMonetaryTranx", "" + monetaryTranxCount);
        fileTrailer.set("NoOfTranx", "" + (fileCount + 1));
        fileTrailer.set("NoOfTCRs", "" + (TCCount + 1));
        fileTrailer.set("SourceAmt", "" + fileTotal);

        out.write((fileTrailer.pack2String() + "\r\n").getBytes());
    }

private void getData(InputStream in)
        throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for(String st = br.readLine(); st != null; st = br.readLine())
        {

            TC aTC = MessageFactory.getTranxType(st);

            if(aTC != null)
            {
					aTC.unpackString(st);

					if((aTC instanceof DraftData) && aTC.get("TCR").equals("0"))
					{
						Draft0s.add(Draft0s.size(), aTC);
						continue;
					}
					if((aTC instanceof DraftData) && aTC.get("TCR").equals("1"))
					{
						Draft1s.add(Draft1s.size(), aTC);
						continue;
					}
					if(aTC instanceof DocumentRequest)
					{
						DocRequests.add(DocRequests.size(), aTC);
						continue;
					}
					if(aTC instanceof FeeCollectionRequest)
					{
						FeeCollectionRequests.add(FeeCollectionRequests.size(), aTC);
					}
					else
					{
						Misc.add(Misc.size(), aTC);

					}
			}
			else
			{

				// Misc.add(Misc.size(), aTC);
				System.out.println("Tranx not configured");
		 	}
        }
System.out.println("Loaded");
        loaded = true;
    }


    public Enumeration getDraftData0(InputStream in)
        throws Exception
    {
        if(!loaded)
            getData(in);
        return Draft0s.elements();
    }

    public Enumeration getDraftData1(InputStream in)
        throws Exception
    {
        if(!loaded)
            getData(in);
        return Draft1s.elements();
    }

    public Enumeration getDocumentRequests(InputStream in)
        throws Exception
    {
        if(!loaded)
            getData(in);
        return DocRequests.elements();
    }

    public Enumeration getFeeCollectionRequest(InputStream in)
        throws Exception
    {
        if(!loaded)
            getData(in);
        return FeeCollectionRequests.elements();
    }

    public Enumeration getMisc(InputStream in)
        throws Exception
    {
        if(!loaded)
            getData(in);
        return Misc.elements();
    }

    public int getTranxCount()
    {
        try
        {
            int i = Integer.parseInt(fileTrailer.get("NoOfTranx"));
            return i;
        }
        catch(Exception e)
        {
            int j = 0;
            return j;
        }
    }

    private Vector batchList;
    private Trailer fileTrailer;
    private Hashtable TCRs;
    private Vector Draft0s;
    private Vector Draft1s;
    private Vector DocRequests;
    private Vector FeeCollectionRequests;
    private Vector Misc;
    private boolean loaded;
}
