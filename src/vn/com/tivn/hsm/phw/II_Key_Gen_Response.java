package vn.com.tivn.hsm.phw;

public class II_Key_Gen_Response extends HSMMsg
{
  public II_Key_Gen_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x04,(byte)0x02});
    super.addField("RC", new FixedLengthField());
    super.addField("n",new FixedLengthField());
    //super.addField("eKISvx", new VariableLengthField());
    //use eKTM as label for compatibility reason. The key is actually encrypted
    //under KIS
    super.addField("eKTM", new VariableLengthField());
    super.addField("KS_Spec", new VariableLengthField());
    super.addField("KVC",new FixedLengthField(3));
  }
}