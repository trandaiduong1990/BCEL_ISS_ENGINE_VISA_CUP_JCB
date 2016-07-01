package vn.com.tivn.hsm.phw;

public class Clr_PIN_Encrypt_Response extends HSMMsg
{
  protected FixedLengthField rc = new FixedLengthField(1);
  protected FixedLengthField ePPK = new FixedLengthField(8);
  public Clr_PIN_Encrypt_Response()
  {
    super.setFunctionCode(new byte[]{(byte)0xEE,(byte)0x06,(byte)0x00});
    super.addField("RC",rc);
    super.addField("ePIN",ePPK);
  }
}