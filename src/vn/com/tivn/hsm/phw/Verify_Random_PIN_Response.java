package vn.com.tivn.hsm.phw;

public class Verify_Random_PIN_Response extends HSMMsg
{
  public Verify_Random_PIN_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,0x06,0x03});
    super.addField("RC", new FixedLengthField());
  }
}