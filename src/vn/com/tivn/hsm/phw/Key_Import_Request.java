package vn.com.tivn.hsm.phw;

public class Key_Import_Request extends HSMMsg
{
  protected FixedLengthField FM =new FixedLengthField();
  protected VariableLengthField KIR_Spec =new VariableLengthField();
  protected FixedLengthField Key_Type =new FixedLengthField(1);
  protected FixedLengthField ENC_Mode =new FixedLengthField(1);
  protected VariableLengthField Key_Spec =new VariableLengthField();

  public Key_Import_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x02,(byte)0x00});
    FM.setContent(new byte[]{0x00});
    super.addField("FM",FM);
    super.addField("KIR_Spec",KIR_Spec);
    super.addField("Key_Type",Key_Type);
    super.addField("ENC_Mode",ENC_Mode);
    super.addField("Key_Spec",Key_Spec);
  }
}