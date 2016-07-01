package vn.com.tivn.hsm.phw;

public class II_Key_Rcv_Request extends HSMMsg
{
  protected FixedLengthField FM =new FixedLengthField();
  protected VariableLengthField KIR_Spec =new VariableLengthField();
  protected FixedLengthField Key_Flags =new FixedLengthField(2);
  protected VariableLengthField eKIRvx =new VariableLengthField();
  
  public II_Key_Rcv_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x04,(byte)0x03});
    FM.setContent(new byte[]{0x00});
    super.addField("FM",FM);
    super.addField("KIR_Spec",KIR_Spec);
    super.addField("Key_Flags",Key_Flags);
    super.addField("eKIRvx",eKIRvx);
  }
}