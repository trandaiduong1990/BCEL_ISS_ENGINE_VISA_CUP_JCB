package com.transinfo.hsm.phw;

public class II_Key_Rcv_Response extends HSMMsg
{
  protected FixedLengthField n = new FixedLengthField();
  public II_Key_Rcv_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x04,(byte)0x03});
    super.addField("RC",new FixedLengthField());
    super.addField("n",n);
  }

  public void unpack(byte[] b) throws HSMException
  {
    super.unpack(b);
    int index = 5;
    byte[] fdata = new byte[3];
    int i = (int)n.getContent()[0];  //number of sets of K_Spec and KVC in the message
    int remainingLength = b.length-index;
    //byte[] data = new byte[remainingLength];

    for (int j = 0;j<i;j++)
    {
      byte[] vfdata = new byte[remainingLength];
      System.arraycopy(b,index,vfdata,0,remainingLength);
      VariableLengthField vf = new VariableLengthField();
      try
      {
        vf.unpack(vfdata);
      }catch(Exception e)
      {
        e.printStackTrace();
      }
      index+=vf.getTotalLength();
      remainingLength-=vf.getTotalLength();
      System.arraycopy(b,index,fdata,0,3);
      FixedLengthField f = new FixedLengthField(3);
      f.setContent(fdata);
      index+=3;
      remainingLength-=3;
      super.addField("KS_Spec",vf);
      super.addField("KVC",f);
    }
  }
}