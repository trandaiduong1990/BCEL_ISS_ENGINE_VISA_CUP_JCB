// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   field.java

package vn.com.tivn.ctf;


public class field
{

    public field(String name, int len, boolean numeric)
    {
        fieldName = name;
        length = len;
        isNumeric = numeric;
        if(isNumeric)
            content = createDefaultString(len, '0');
        else
            content = createDefaultString(len, ' ');
    }

    public int getLength()
    {
        return length;
    }

    public String getName()
    {
        return fieldName;
    }

    public boolean isNumeric()
    {
        return isNumeric;
    }

    public void setContent(String content)
        throws Exception
    {
        if(content.length() > length)
            throw new Exception("Value too large for field " + fieldName);
        String padder = " ";
        if(isNumeric())
        {
            padder = "0";
            for(; content.length() < length; content = padder + content);
        } else
        {
            for(; content.length() < length; content = content + padder);
        }
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    private String createDefaultString(int len, char ch)
    {
        String st = "";
        for(int i = 0; i < len; i++)
            st = st + ch;

        return st;
    }

    protected int length;
    protected boolean isNumeric;
    protected String content;
    protected String fieldName;
}
