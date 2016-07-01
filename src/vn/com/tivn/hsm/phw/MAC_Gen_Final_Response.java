package vn.com.tivn.hsm.phw;

public class MAC_Gen_Final_Response extends HSMMsg
{
  public MAC_Gen_Final_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x07,(byte)0x01});
    super.addField("RC", new FixedLengthField());
    super.addField("MAC", new VariableLengthField());
  }
}