package vn.com.tivn.hsm.phw;

public class Key_Import_Response extends HSMMsg
{
  protected FixedLengthField n = new FixedLengthField();
  public Key_Import_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x02,(byte)0x00});
    super.addField("RC",new FixedLengthField());
    super.addField("KIR_Spec", new KeySpecField());
    super.addField("KVC", new FixedLengthField(3));
  }

 /* public void unpack(byte[] b) throws HSMException
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
  }*/
}