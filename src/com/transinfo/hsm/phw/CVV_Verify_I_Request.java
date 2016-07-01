package com.transinfo.hsm.phw;

public class CVV_Verify_I_Request extends HSMMsg
{
  public CVV_Verify_I_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x08,(byte)0x03});
    super.addField("FM", new FixedLengthField());
    super.addField("CVK_Spec",new VariableLengthField());
    super.addField("CVV_Data",new FixedLengthField(16));
    super.addField("CVV",new FixedLengthField(2));
  }
}