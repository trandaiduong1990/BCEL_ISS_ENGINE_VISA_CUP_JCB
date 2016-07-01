package vn.com.tivn.hsm.phw;

public class CVV_Verify_I_Response extends HSMMsg
{
  public CVV_Verify_I_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x08,(byte)0x03});
    super.addField("RC", new FixedLengthField());
  }
}