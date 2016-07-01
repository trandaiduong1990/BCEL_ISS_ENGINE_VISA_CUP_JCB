// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DocumentRequest.java

package vn.com.tivn.ctf;


// Referenced classes of package vn.com.tivn.ctf:
//            TC

public class DocumentRequest extends TC
{

    public DocumentRequest()
    {
        add("TranxCode", 2, true);
        add("TranxCodeQualifier", 1, true);
        add("TranxComponentSequenceNo", 1, true);
        add("AccNo", 16, true);
        add("AccNoExtension", 3, true);
        add("ARN", 23, true);
        add("AcqBusinessID", 8, true);
        add("PurchaseDate", 4, true);
        add("TranxAmt", 12, true);
        add("TranxCurrCode", 3, false);
        add("MerchantName", 25, false);
        add("MerchantCity", 13, false);
        add("MerchantCountryCode", 3, false);
        add("MCC", 4, true);
        add("MerchantZIPCode", 5, true);
        add("MerchantProvinceCode", 3, false);
        add("IssuerControlNo", 9, true);
        add("RequestReasonCode", 2, true);
        add("SettlementFlag", 1, true);
        add("NationalReimbursementFee", 12, true);
        add("ATMAccSelection", 1, false);
        add("RetrievalRequestID", 12, false);
        add("CentralProcessingDate", 4, true);
        add("ReimbursementAttribute", 1, false);
    }

    public void setTypeOriginalRequest()
    {
        try
        {
            set("TranxCode", "51");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTypeCopyRequest()
    {
        try
        {
            set("TranxCode", "52");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTypeConfirmResponse()
    {
        try
        {
            set("TranxCode", "53");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
