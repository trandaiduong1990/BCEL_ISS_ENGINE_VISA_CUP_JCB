// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Trailer.java

package vn.com.tivn.ctf;


// Referenced classes of package vn.com.tivn.ctf:
//            TC

public class Trailer extends TC
{

    public Trailer()
    {
        super.add("TranxCode", 2, true);
        super.add("TranxCodeQualifier", 1, true);
        super.add("TranxComponentSequenceNo", 1, true);
        super.add("BIN", 6, true);
        super.add("ProcessingDate", 5, true);
        super.add("DestinationAmt", 15, true);
        super.add("NoOfMonetaryTranx", 12, true);
        super.add("BatchNo", 6, true);
        super.add("NoOfTCRs", 12, true);
        super.add("Reserved", 6, true);
        super.add("CenterBatchID", 8, false);
        super.add("NoOfTranx", 9, true);
        super.add("Reserved", 18, true);
        super.add("SourceAmt", 15, true);
        super.add("Reserved", 15, true);
        super.add("Reserved", 15, true);
        super.add("Reserved", 15, true);
        super.add("Reserved", 7, false);
    }

    public void setTypeBatchTrailer()
    {
        try
        {
            super.set("TranxCode", "91");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTypeFileTrailer()
    {
        try
        {
            super.set("TranxCode", "92");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
