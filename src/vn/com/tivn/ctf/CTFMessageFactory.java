// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CTFMessageFactory.java

package vn.com.tivn.ctf;


// Referenced classes of package vn.com.tivn.ctf:
//            DocumentRequest, FeeCollectionRequest, DraftData, TC

public class CTFMessageFactory
{

    public CTFMessageFactory()
    {
    }

    public static TC getMessage(String tnxCode)
    {
        int tranxcode = Integer.parseInt(tnxCode);
        try
        {
            switch(tranxcode)
            {
            case 510:
            case 520:
            case 530:
                DocumentRequest doc = new DocumentRequest();
                doc.set("TranxCode", "" + tranxcode / 10);
                DocumentRequest documentrequest = doc;
                return documentrequest;

            case 100: // 'd'
                FeeCollectionRequest feecollectionrequest = new FeeCollectionRequest();
                return feecollectionrequest;
            }
            if(tranxcode % 2 == 0)
            {
                DraftData draft0 = new DraftData(0);
                draft0.set("TranxCode", "" + tranxcode / 10);
                DraftData draftdata = draft0;
                return draftdata;
            }
            else
            {
                DraftData draft1 = new DraftData(7);
                draft1.set("TranxCode", "" + tranxcode / 10);
                DraftData draftdata1 = draft1;
                return draftdata1;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
