// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   LogicalTranx.java

package vn.com.tivn.ctf;

import java.io.OutputStream;
import java.util.*;

// Referenced classes of package vn.com.tivn.ctf:
//            Trailer, TC

public class LogicalTranx
{

    public LogicalTranx()
    {
        tranxList = new Vector();
        TCRs = new Hashtable();
        batchTrailer = new Trailer();
        batchTrailer.setTypeBatchTrailer();
    }

    public boolean add(TC tranx)
        throws Exception
    {
        int TCR = Integer.parseInt(tranx.get("TranxComponentSequenceNo"));
        TCRs.put(new Integer(TCR), new Integer(TCR));
        /*if(!tranxList.isEmpty())
        {
            for(int i = 0; i < tranxList.size(); i++)
            {
                int currentTCR = Integer.parseInt(((TC)tranxList.elementAt(i)).get("TranxComponentSequenceNo"));
                if(currentTCR >= TCR)
                {
                    tranxList.add(i, tranx);
                    return true;
                }
            }

        }*/

        tranxList.add(tranx);
        System.out.println(tranxList.size());
        return true;
    }

    public String pack2String() throws Exception
    {
        String result = "";
        long batchTotal = 0L;
        int batchCount = 0;
        int monetaryTranxCount = 0;
        for(Enumeration enmrt = tranxList.elements(); enmrt.hasMoreElements();)
        {
            TC aTC = (TC)enmrt.nextElement();
            long amt = 0L;
            try
            {
                amt = Long.parseLong(aTC.get("SourceAmt"));
            }
            catch(Exception exception) { }

            if(amt > (long)0)
                monetaryTranxCount++;
            batchTotal = (int)((long)batchTotal + amt);
            batchCount++;
            result = result + aTC.pack2String() + "\r\n";

        }

		TotBatchTotal = batchTotal;
		TotBatchCount = monetaryTranxCount;

        batchTrailer.set("NoOfMonetaryTranx", "" + monetaryTranxCount);
        batchTrailer.set("NoOfTranx", "" + (monetaryTranxCount + 1));
        batchTrailer.set("NoOfTCRs", "" + (batchCount + 1));
        batchTrailer.set("SourceAmt", "" + batchTotal);
        return result + batchTrailer.pack2String() + "\r\n";
    }

    public void write2Stream(OutputStream out)throws Exception
    {
        long batchTotal = 0L;
        int batchCount = 0;
        int monetaryTranxCount = 0;
        TC aTC;
        for(Enumeration enmrt = tranxList.elements(); enmrt.hasMoreElements(); out.write((aTC.pack2String() + "\r\n").getBytes()))
        {
            aTC = (TC)enmrt.nextElement();
            long amt = 0L;
            try
            {
                amt = Long.parseLong(aTC.get("SourceAmt"));
            }
            catch(Exception e)
            {
               // e.printStackTrace();
            }
            if(amt > (long)0)
            	monetaryTranxCount++;

            batchTotal += amt;
            batchCount++;
        }

		TotBatchTotal = batchTotal;
		TotBatchCount = monetaryTranxCount;

        batchTrailer.set("NoOfMonetaryTranx", "" + monetaryTranxCount);
        System.out.println("CenterBatchID="+centerBatchId);
        if(!centerBatchId.equals("") && centerBatchId.length() == 8)
        batchTrailer.set("CenterBatchID",centerBatchId);
        batchTrailer.set("NoOfTranx", "" + (monetaryTranxCount + 1));
        batchTrailer.set("NoOfTCRs", "" + (batchCount + 1));
        batchTrailer.set("SourceAmt", "" + batchTotal);
        out.write((batchTrailer.pack2String() + "\r\n").getBytes());

        System.out.println("$$$$$$$$$$$$"+TotBatchTotal+"   "+TotBatchCount);
    }

    public Enumeration getTCRs()
    {
        return TCRs.elements();
    }

    public Trailer getTrailer()
    {
        return batchTrailer;
    }

    public boolean isEmpty()
    {
		System.out.println(tranxList.size());
        return tranxList.isEmpty();
    }


   public long getBatchTotal()
   {
     return TotBatchTotal;
   }


   public int getBatchCount()
   {
     return TotBatchCount;
   }

   public int getMonetaryTranxCount()
   {
     return monetaryTranxCount;
   }


   public String getCenterBatchId()
   {
     return centerBatchId;
   }


   public void setCenterBatchId(String centerBatchId)
   {
	   System.out.println("************* centerBatchId="+centerBatchId);
     this.centerBatchId = centerBatchId;
   }

    private Vector tranxList;
    Hashtable TCRs;
    private Trailer batchTrailer;
    private long TotBatchTotal = 0;
	private int TotBatchCount = 0;
  	private int monetaryTranxCount = 0;
  	private String centerBatchId = "";
}
