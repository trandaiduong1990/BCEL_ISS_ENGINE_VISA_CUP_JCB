package vn.com.tivn.hsm.phw;

public class GenerateRandomPIN_Response extends HSMMsg
{
  public GenerateRandomPIN_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x0E,(byte)0x04});
    super.addField("RC", new FixedLengthField(1));
    super.addField("ePPK", new FixedLengthField(8));
  }
}