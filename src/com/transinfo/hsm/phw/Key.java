package com.transinfo.hsm.phw;

public class Key
{
  byte [] content = new byte[3];//default is hsmstored key
  boolean isHSMStored = true;

  public Key()
  {
  }

  public Key(int index)
  {
    try
    {
      setIndex(index);
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public Key(String st)
  {
    try
    {
      setKey(st);
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }


  public void setIndex(int index) throws Exception
  {
    if (index>9999)
    {
      throw new Exception("Invalid Index");
    }
    String st = ""+index;
    while (st.length()<4)
      st = "0"+st;
    setKeyTypeHSMStored();
    System.arraycopy(NumberUtil.hex2byte(st),0,content,1,2);
  }

  public void setKey(String st) throws Exception
  {
    if (st.length()>32)
    {
      throw new Exception("Invalid key length");
    }
    if (st.length()==32)
      setKeyTypeHostStored();
    else if (st.length()==16)
      setKeyTypeSingleLengthHostStored();
    else if (st.length()<=4)
    {
      setKeyTypeHSMStored();
      setIndex(Integer.parseInt(st));
    }
    byte[] source = NumberUtil.hex2byte(st);
    System.arraycopy(source,0,content,content.length-source.length,source.length);
  }

  public void setKey(byte [] b) throws Exception
  {
    System.arraycopy(b,0,content,content.length-b.length,b.length);
  }

  public void setKeyTypeHSMStored()
  {
    content = new byte[3];
    content[0]=(byte)0x02;
    isHSMStored = true;
  }

  public void setKeyTypeHostStored()
  {
    content = new byte[17];
    content[0]=(byte)0x11;
    isHSMStored = false;
  }

  public void setKeyTypeHostStoredCBC()
  {
    //content = new byte[17];
    content[0]=(byte)0x13;
    isHSMStored = false;
  }

  public void setKeyTypeSingleLengthHostStored()
  {
    content = new byte[9];
    content[0]=(byte)0x10;
    isHSMStored = false;
  }

  public boolean isHostStored()
  {
    return !isHSMStored;
  }

  public byte[] getBytes()
  {
    return content;
  }
}