package vn.com.tivn.hsm.phw;

public class DecryptData_Response extends HSMMsg {
  public DecryptData_Response() {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x08,(byte)0x01});
    super.addField("RC", new FixedLengthField());
    super.addField("OCV", new FixedLengthField(8));
    super.addField("Data", new VariableLengthField());
  }
}
