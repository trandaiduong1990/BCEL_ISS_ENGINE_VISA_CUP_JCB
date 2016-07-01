package vn.com.tivn.hsm.phw;

public class BNV_Key_Rcv_Response extends HSMMsg
{
  protected FixedLengthField n = new FixedLengthField();
  public BNV_Key_Rcv_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x04,(byte)0x03});
    super.addField("RC",new FixedLengthField());
    super.addField("n",n);
    super.addField("KS_Spec",new VariableLengthField());
    super.addField("KVC",new FixedLengthField(3));
  }
}