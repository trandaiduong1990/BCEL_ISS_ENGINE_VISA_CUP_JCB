package com.transinfo.hsm.phw;

public class EncryptData_Request extends HSMMsg {
  public EncryptData_Request() {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x08,(byte)0x00});
    super.addField("FM", new FixedLengthField());
    super.addField("DPK_Spec", new VariableLengthField());
    super.addField("CM", new FixedLengthField());
    super.addField("ICV", new FixedLengthField(8));
    super.addField("Data", new VariableLengthField());
  }
}
