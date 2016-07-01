package com.transinfo.hsm.phw;

public class EncryptData_Response extends HSMMsg {
  public EncryptData_Response() {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x08,(byte)0x00});
    super.addField("RC", new FixedLengthField());
    super.addField("OCV", new FixedLengthField(8));
    super.addField("eDPK", new VariableLengthField());
  }
}
