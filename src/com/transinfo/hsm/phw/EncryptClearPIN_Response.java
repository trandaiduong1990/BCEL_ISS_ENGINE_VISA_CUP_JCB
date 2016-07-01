package com.transinfo.hsm.phw;

public class EncryptClearPIN_Response extends HSMMsg {
  public EncryptClearPIN_Response() {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x00});
    super.addField("RC", new FixedLengthField());
    super.addField("ePIN", new FixedLengthField(8));
  }
}