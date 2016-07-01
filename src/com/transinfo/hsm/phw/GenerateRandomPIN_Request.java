package com.transinfo.hsm.phw;

public class GenerateRandomPIN_Request extends HSMMsg
{
  public GenerateRandomPIN_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x0E,(byte)0x04});
    super.addField("FM",new FixedLengthField(1));
    super.addField("PIN_Len",new FixedLengthField(1));
    super.addField("PFo",new FixedLengthField());
    super.addField("ANB",new FixedLengthField(6));
    super.addField("PPK_Spec",new VariableLengthField());
  }
}