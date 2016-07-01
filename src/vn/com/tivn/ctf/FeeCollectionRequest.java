// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FeeCollectionRequest.java

package vn.com.tivn.ctf;


// Referenced classes of package vn.com.tivn.ctf:
//            TC

public class FeeCollectionRequest extends TC
{

    public FeeCollectionRequest()
    {
        add("TranxCode", 2, true);
        add("TranxCodeQualifier", 1, true);
        add("TranxComponentSequenceNo", 1, true);
        add("DestinationBIN", 6, true);
        add("SourceBIN", 6, true);
        add("ReasonCode", 4, true);
        add("CountryCode", 3, true);
        add("EventDate", 4, true);
        add("AccNo", 16, false);
        add("AccNoExtension", 3, false);
        add("DestinationAmt", 12, true);
        add("DestinationCurrCode", 3, false);
        add("SourceAmt", 12, true);
        add("SourceCurrCode", 3, false);
        add("MessageText", 70, false);
        add("SettlementFlag", 1, true);
        add("TranxIdentifier", 15, true);
        add("Reserved", 1, false);
        add("CentralProcessingDate", 4, true);
        add("ReimbursementAttribute", 1, false);
        try
        {
            set("TranxCode", "10");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
