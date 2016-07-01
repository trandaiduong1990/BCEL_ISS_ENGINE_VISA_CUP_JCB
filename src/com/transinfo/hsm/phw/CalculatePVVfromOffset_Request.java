package com.transinfo.hsm.phw;

public class CalculatePVVfromOffset_Request extends HSMMsg
{
  public CalculatePVVfromOffset_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x06});
    super.addField("FM",new FixedLengthField(1));
    super.addField("PVK_Spec",new VariableLengthField());
    super.addField("ValidationData",new FixedLengthField(8));
    super.addField("Offset4",new FixedLengthField(2));
    super.addField("PVVK_Spec",new VariableLengthField());
    super.addField("TSP12",new FixedLengthField(6));
  }
}