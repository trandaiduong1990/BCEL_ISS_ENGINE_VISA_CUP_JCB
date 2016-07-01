package com.transinfo.hsm.phw;

public class VerifyPIN_PVV_Response extends HSMMsg
{
  public VerifyPIN_PVV_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x05});
    super.addField("RC",new FixedLengthField());
  }
}