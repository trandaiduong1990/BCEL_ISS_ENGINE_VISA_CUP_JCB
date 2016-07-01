package vn.com.tivn.hsm.phw;

public class IT_Key_Gen_Request extends HSMMsg
{
  protected FixedLengthField FM =new FixedLengthField();
  protected KeySpecField KTM_Spec =new KeySpecField();
  protected FixedLengthField Key_Flags =new FixedLengthField(2);

  public IT_Key_Gen_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x04,(byte)0x00});
    FM.setContent(new byte[]{0x00});
    super.addField("FM",FM);
    super.addField("KTM_Spec",KTM_Spec);
    super.addField("Key_Flags",Key_Flags);
  }
}