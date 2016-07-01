package com.transinfo.hsm.phw;

public class MAC_Ver_Final_Response extends HSMMsg
{
  public MAC_Ver_Final_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x07,(byte)0x02});
    super.addField("RC", new FixedLengthField());
  }
}