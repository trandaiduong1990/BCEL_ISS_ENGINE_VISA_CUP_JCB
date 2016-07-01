package com.transinfo.hsm.phw;

public class Verify_Random_PIN_Request extends HSMMsg
{
  public Verify_Random_PIN_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,0x06,0x03});
    super.addField("FM", new FixedLengthField(1));
    super.addField("ePPK", new FixedLengthField(8));
    super.addField("PPK_Spec", new VariableLengthField());
    super.addField("PF", new FixedLengthField());
    super.addField("ANB", new FixedLengthField(6));
    super.addField("PVK_Spec", new VariableLengthField());
    super.addField("ValidationData", new FixedLengthField(8));
    super.addField("Offset", new FixedLengthField(6));
    super.addField("Check_Len", new FixedLengthField());
  }
}