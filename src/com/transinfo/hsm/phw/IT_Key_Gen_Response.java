package com.transinfo.hsm.phw;

public class IT_Key_Gen_Response extends HSMMsg
{
  protected FixedLengthField n = new FixedLengthField();

  public IT_Key_Gen_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x04,(byte)0x03});
    super.addField("RC",new FixedLengthField());
    super.addField("n",n);
    super.addField("eKTM", new VariableLengthField());
    super.addField("KS_Spec", new VariableLengthField());
    super.addField("KVC", new FixedLengthField(3));

  }

  /*public void unpack(byte[] b) throws HSMException
  {
    HSMMsg amsg = new HSMMsg();
    amsg.addField("eKTM", new VariableLengthField());
    amsg.addField("KS_Spec", new VariableLengthField());
    amsg.addField("KVC", new FixedLengthField(3));
    super.unpack(b);
    byte[] remainingData = new byte[b.length-5];
    System.arraycopy(b,5,remainingData,0,remainingData.length);
    super.unpack(remainingData,amsg,0);
  }*/
}