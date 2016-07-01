package vn.com.tivn.hsm.phw;

public class MAC_Update_Response extends HSMMsg
{
  public MAC_Update_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x07,(byte)0x00});
    super.addField("RC", new FixedLengthField());
    super.addField("OCD", new FixedLengthField(8));
  }
}