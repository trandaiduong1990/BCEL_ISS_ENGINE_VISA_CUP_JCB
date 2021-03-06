package com.transinfo.hsm.phw;

public class VariableLengthField
{
  protected byte [] content;
  protected byte [] lengthPrefix;
  public VariableLengthField()
  {
  }
  public void  setContent(byte[] c)
  {
    int prefixLength = (int)Math.round((Math.log(c.length)/Math.log(128))+0.5);
    lengthPrefix = new byte[prefixLength];
    content = new byte[c.length];
    int len = c.length;
    System.out.println(">>>>>>>>"+c.length+"  "+NumberUtil.hexString(c));
    //Generate the length prefix value
    for (int i=prefixLength-1;i>=0;i--)
    {
      //lengthPrefix[i]=(byte)prefixLength;
      lengthPrefix[i]=(byte)len;
      //prefixLength = prefixLength >>>8;
      len=len>>>8;
    }
    //Generate the length indicator of length prefix
    for (int j = 0; j<prefixLength-1;j++)
    {
      lengthPrefix[0] = (byte)(lengthPrefix[0]|(int)Math.pow(2,8-j));
      //lengthPrefix[0] = (byte)(c.length|(int)Math.pow(2,8-j));
    }

    System.arraycopy(c,0,content,0,c.length);
  }

  public byte[] pack()
  {
    byte [] temp = new byte[content.length+lengthPrefix.length];
    System.arraycopy(lengthPrefix,0,temp,0,lengthPrefix.length);
    System.arraycopy(content,0,temp,lengthPrefix.length,content.length);
    return temp;
  }

  public void unpack(byte[] b) throws Exception
  {
    int check = 1;
    int datalength = 0;
    double power = 8;

    //Support maximum 8 byte length prefix (actually 4 because of integer limit)
    while (check !=0)
    {
      --power;
      check = b[0] & (int)Math.pow(2,power);
    }

    if (power<0)
    {
      throw new Exception("Unsupported length");
    }
    int prefixLength = (int)(8-power);
    lengthPrefix = new byte[prefixLength];
    System.arraycopy(b,0,lengthPrefix,0,prefixLength);

    //Truncate all the preceding 1s to get the actual length
    datalength = lengthPrefix[0];
    datalength = datalength<<(prefixLength-1);
    datalength = datalength>>>(prefixLength-1);//datalength byte[0]

    //Get the actual length of the message by concatenating all the bytes of the length prefix
    for (int j=1;j<prefixLength;j++)
      datalength = datalength<<8|lengthPrefix[j];//datalength byte[1] to byte[prefixlength-1]
    try
    {
      content = new byte[datalength];
      System.arraycopy(b,prefixLength,content,0,datalength);
    }
    catch (ArrayIndexOutOfBoundsException ae)
    {
      ae.printStackTrace();
    }
  }

  public int getTotalLength()
  {
    if (content!=null)
      return lengthPrefix.length+content.length;
    return 0;
  }

  public byte[] getContent()
  {
    return content;
  }

  public byte[] getLengthPrefix()
  {
    return lengthPrefix;
  }

  public int getLength()
  {
    // TODO:  Implement this com.transinfo.hsm.phw.HSMField abstract method
    return getTotalLength();
  }
}