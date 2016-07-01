package com.transinfo.hsm.phw;

public class PIN_Mail_Response extends HSMMsg
{
  public PIN_Mail_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xE2});
    super.addField("RC", new FixedLengthField());
    super.addField("Offset", new FixedLengthField(6));
  }
}