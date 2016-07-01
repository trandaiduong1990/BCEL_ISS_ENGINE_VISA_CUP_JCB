// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   DraftData.java

package vn.com.tivn.ctf;


// Referenced classes of package vn.com.tivn.ctf:
//            TC

public class DraftData extends TC
{

    public DraftData(int TCR)
    {
        if(TCR == 0)
        {
            initTCR0();
            return;
        }
        else if(TCR == 7)
        {
   		    initTCR7();
   		}
        else
        {
            initTCR1();
            return;
        }
    }

    private void initTCR0()
    {
        add("TranxCode", 2, true);
        add("TranxCodeQualifier", 1, true);
        add("TranxComponentSequenceNo", 1, true);
        add("AccNo", 16, true);
        add("AccNoExtension", 3, true);
        add("FloorLimitIndicator", 1, false);
        add("CRB", 1, false);
        add("PCASIndicator", 1, false);
        add("ARN", 23, true);
        add("AcqBusinessID", 8, true);
        add("PurchaseDate", 4, true);
        add("DestinationAmt", 12, true);
        add("DestinationCurrCode", 3, false);
        add("SourceAmt", 12, true);
        add("SourceCurrCode", 3, false);
        add("MerchantName", 25, false);
        add("MerchantCity", 13, false);
        add("MerchantCountryCode", 3, false);
        add("MCC", 4, true);
        add("MerchantZIPCode", 5, true);
        add("MerchantProvinceCode", 3, false);
        add("RequestedPaymentService", 1, false);
        add("Reserved", 1, false);
        add("UsageCode", 1, false);
        add("ReasonCode", 2, true);
        add("SettlementFlag", 1, true);
        add("AuthCharacteristicsIndicator", 1, false);
        add("AuthCode", 6, false);
        add("POSTerminalCapability", 1, false);
        add("InternationalFeeIndicator", 1, false);
        add("CardHolderIDMethod", 1, false);
        add("CollectionOnlyFlag", 1, false);
        add("POSEntryMode", 2, false);
        add("CentralProcessingDate", 4, true);
        add("ReimbursementAttribute", 1, false);
        try
        {
            set("UsageCode", "1");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initTCR1()
    {
        add("TranxCode", 2, true);
        add("TranxCodeQualifier", 1, true);
        add("TranxComponentSequenceNo", 1, true);
        add("IssuerWorkstationBIN", 6, false);
        add("AcqWorkstationBIN", 6, false);
        add("ChargebackRefNo", 6, true);
        add("DocIndicator", 1, false);
        add("MemberMessageText", 50, false);
        add("SpecialConditionIndicators", 2, false);
        add("FeeProgramIndicator", 3, false);
        add("Reserved", 2, false);
        add("CardAcceptorID", 15, false);
        add("TerminalID", 8, false);
        add("NationalReimbursementFee", 12, true);
        add("MOTOIndicator", 1, false);
        add("SpecialChargebackIndicator", 1, false);
        add("InterfaceTraceNo", 6, false);
        add("CATIndicator", 1, false);
        add("PrepaidCardIndicator", 1, false);
        add("ServiceDevelopmentField", 1, false);
        add("AVSResponseCode", 1, false);
        add("AuthSourceCode", 1, false);
        add("PurchaseIdentifierFormat", 1, false);
        add("ATMAccSelection", 1, false);
        add("InstallmentPaymentCount", 2, false);
        add("PurchaseIdentifier", 25, false);
        add("Cashback", 9, true);
        add("ChipConditionCode", 1, false);
        add("POSEnvironment", 1, false);
        try
        {
            set("TranxComponentSequenceNo", "1");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


public void initTCR7()
{
	  System.out.println("In TCR7");
    this.add("TranxCode",2,true);
    this.add("TranxCodeQualifier",1,true);
    this.add("TranxComponentSequenceNo",1,true);
    this.add("TranxType",2,false);
    this.add("CardSeqNo",3,true);
    this.add("TerminalTranxDate",6,true);
    this.add("TerminalCapabilityProfile",6,false);
    this.add("TerminalCountryCode",3,true);
    this.add("TerminalSerialNo",8,false);
    this.add("UnpredictableNo",8,false);
    this.add("ApplTranxCnt",4,false);
    this.add("ApplInterchangeProfile",4,false);
    this.add("Cryptogram",16,false);
    this.add("IssApplData1",2,false);
    this.add("IssApplData2",2,false);
    this.add("TerminalVerificationResult",10,false);
    this.add("IssApplData3",8,false);
    this.add("CryptogramAmt",12,true);
    this.add("IssApplData4",2,false);
    this.add("IssApplData5",16,true);
    this.add("IssApplData6",2,false);
    this.add("IssApplData7",2,false);
    this.add("IssApplData8",30,false);
    this.add("FormFactorIndicator",8,false);
    this.add("IssuerScript1Result",10,false);

  }



    public void setTranxTypeSale()
    {
        try
        {
            set("TranxCode", "05");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCredit()
    {
        try
        {
            set("TranxCode", "06");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCash()
    {
        try
        {
            set("TranxCode", "07");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeSaleChargeback()
    {
        try
        {
            set("TranxCode", "15");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCreditChargeback()
    {
        try
        {
            set("TranxCode", "16");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCashChargeback()
    {
        try
        {
            set("TranxCode", "17");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeSaleReversal()
    {
        try
        {
            set("TranxCode", "25");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCreditReversal()
    {
        try
        {
            set("TranxCode", "26");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCashReversal()
    {
        try
        {
            set("TranxCode", "27");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeSaleChargebackReversal()
    {
        try
        {
            set("TranxCode", "35");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCreditChargebackReversal()
    {
        try
        {
            set("TranxCode", "36");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setTranxTypeCashChargebackReversal()
    {
        try
        {
            set("TranxCode", "37");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
