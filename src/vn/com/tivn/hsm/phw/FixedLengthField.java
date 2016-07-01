package vn.com.tivn.hsm.phw;

public class FixedLengthField
{
  private byte[] content;
  private int len;
  public FixedLengthField(int fieldlength)
  {
    content = new byte[fieldlength];
    len = fieldlength;
  }
  public FixedLengthField()
  {
    //default
    len = 1;
    content = new byte[len];
  }
  public byte[] pack()
  {
    return content;
  }
  public void setContent(byte[] c)
  {
    content = new byte[c.length];
    System.arraycopy(c,0,content,0,c.length);  
  }
  public int getLength()
  {
    return len;
  }
  public byte[] getContent()
  {
    return content;
  }
}