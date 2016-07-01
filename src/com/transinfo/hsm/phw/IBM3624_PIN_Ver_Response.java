package com.transinfo.hsm.phw;

public class IBM3624_PIN_Ver_Response extends HSMMsg
{
  public IBM3624_PIN_Ver_Response()
  {
    super.setFunctionCode((byte)0x61);
    super.addField("RC", new FixedLengthField());
  }
}