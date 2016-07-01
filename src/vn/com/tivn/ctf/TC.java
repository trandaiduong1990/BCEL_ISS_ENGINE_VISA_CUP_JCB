// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TC.java

package vn.com.tivn.ctf;

import java.util.*;

// Referenced classes of package vn.com.tivn.ctf:
//            field, CTFUtils

public class TC
{

    public TC()
    {
        fields = new Vector();
        indexTable = new Hashtable();
        index = 0;
    }

    public void add(String fieldName, int len, boolean isNumeric)
    {
        fields.add(index, new field(fieldName, len, isNumeric));
        indexTable.put(fieldName.toUpperCase(), "" + index);
        index++;
    }

    public void unpackString(String st)
        throws Exception
    {
        Vector tmp = new Vector();
        field afield;
        for(Enumeration enmrt = fields.elements(); enmrt.hasMoreElements(); tmp.add(afield))
        {
            afield = (field)enmrt.nextElement();
           // System.out.println("Name="+afield.getName());
            afield.setContent(st.substring(0, afield.getLength()));
            st = st.substring(afield.getLength());
        }

        fields = tmp;
    }

    public String pack2String()
    {
        String result = "";
        for(Enumeration enmrt = fields.elements(); enmrt.hasMoreElements();)
            result = result + ((field)enmrt.nextElement()).getContent();

        return result;
    }

    public void set(String fieldname, String content)
        throws Exception
    {
        if(fieldname.equals("TCR"))
            fieldname = "TranxComponentSequenceNo";
        int idx = Integer.parseInt((String)indexTable.get(fieldname.toUpperCase()));
        if(idx >= 0)
            ((field)fields.elementAt(idx)).setContent(content);
        else
            throw new Exception("Invalid field name: " + fieldname);
    }

    public String get(String fieldname)
        throws Exception
    {
        if(fieldname.equals("TCR"))
            fieldname = "TranxComponentSequenceNo";
        try
        {
            int idx = Integer.parseInt((String)indexTable.get(fieldname.toUpperCase()));
            if(idx >= 0)
            {
                String s = ((field)fields.elementAt(idx)).getContent();
                return s;
            }
        }
        catch(Exception e)
        {
            throw new Exception("Invalid field name: " + fieldname);
        }
        return null;
    }

    public String getTranxStatus()
    {
        String tnxcode = "";
        try
        {
            tnxcode = get("TranxCode") + get("TranxComponentSequenceNo");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return tnxcode;
    }

    public String getTranxType()
    {
        int tnxcode = Integer.parseInt(getTranxStatus());
        switch(tnxcode)
        {
        case 50: // '2'
            return "Sale";

        case 60: // '<'
            return "Credit";

        case 70: // 'F'
            return "Cash";

        case 150:
            return "SaleChargeback";

        case 160:
            return "CreditChargeback";

        case 170:
            return "CashChargeback";

        case 250:
            return "SaleReversal";

        case 260:
            return "CreditReversal";

        case 270:
            return "CashReversal";

        case 350:
            return "SaleChargebackReversal";

        case 360:
            return "CreditChargebackReversal";

        case 370:
            return "CashChargebackReversal";

        case 51: // '3'
            return "SaleAdditional";

        case 61: // '='
            return "CreditAdditional";

        case 71: // 'G'
            return "CashAdditional";

        case 151:
            return "SaleChargebackAdditional";

        case 161:
            return "CreditChargebackAdditional";

        case 171:
            return "CashChargebackAdditional";

        case 251:
            return "SaleReversalAdditional";

        case 261:
            return "CreditReversalAdditional";

        case 271:
            return "CashReversalAdditional";

        case 351:
            return "SaleChargebackReversalAdditional";

        case 361:
            return "CreditChargebackReversalAdditional";

        case 371:
            return "CashChargebackReversalAdditional";

        case 510:
            return "OriginalRequest";

        case 520:
            return "CopyRequest";

        case 530:
            return "ConfirmResponse";

        case 100: // 'd'
            return "FeeCollection";
        }
        return "Invalid TC";
    }

    public void setARN(String FormatCode, String BIN, String TranxDate, String FilmLocator)
    {
        String ARN = FormatCode + BIN + TranxDate + FilmLocator;
        ARN = ARN + CTFUtils.getCheckDigit(ARN);
        try
        {
            set("ARN", ARN);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    Vector fields;
    Hashtable indexTable;
    int index;
}
