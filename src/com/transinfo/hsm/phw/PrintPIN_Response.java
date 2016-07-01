package com.transinfo.hsm.phw;

public class PrintPIN_Response extends HSMMsg
{
  public PrintPIN_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x0E,(byte)0x05});
    super.addField("RC",new FixedLengthField());
  }
}