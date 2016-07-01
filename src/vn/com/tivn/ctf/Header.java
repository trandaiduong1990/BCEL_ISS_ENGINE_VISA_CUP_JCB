// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Header.java

package vn.com.tivn.ctf;


// Referenced classes of package vn.com.tivn.ctf:
//            TC

public class Header extends TC
{

    public Header()
    {
        add("TranxCode", 2, true);
        add("ProcessingBIN", 6, false);
        add("ProcessingDate", 5, true);
        add("Reserved", 16, false);
        add("TestOption", 4, false);
        add("Reserved", 29, false);
        add("SecurityCode", 8, false);
        add("Reserved", 6, false);
        add("OutgoingFileID", 3, true);
        add("Reserved", 89, false);
        try
        {
            set("TranxCode", "90");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
