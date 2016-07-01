package com.transinfo.hsm.phw;

public class EncryptClearPIN_Request extends HSMMsg {
  public EncryptClearPIN_Request() {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x00});
    super.addField("FM", new FixedLengthField());
    super.addField("PIN-Len", new FixedLengthField());
    super.addField("PIN", new VariableLengthField());
    super.addField("ANB", new FixedLengthField(6));
    super.addField("PPK-Spec", new VariableLengthField());
  }
}
