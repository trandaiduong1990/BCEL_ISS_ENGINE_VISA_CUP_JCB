package com.transinfo.hsm.phw;

public class CalculateOffsetfromPIN_Response extends HSMMsg
{
  public CalculateOffsetfromPIN_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x04});
    super.addField("RC",new FixedLengthField());
    super.addField("Offset",new FixedLengthField(6));
    super.addField("PIN_Len",new FixedLengthField());
  }
}