package com.transinfo.hsm.phw;

public class IBM3624_PIN_Ver_Request extends HSMMsg
{
  public IBM3624_PIN_Ver_Request()
  {
    super.setFunctionCode((byte)0x61);
    super.addField("PVK_Index",new FixedLengthField());
    super.addField("ePPK",new FixedLengthField(8));
    super.addField("eKM",new FixedLengthField(8));//PPK_Spec
    super.addField("PAN",new FixedLengthField(8));
    super.addField("ANB",new FixedLengthField(6));
    super.addField("Offset",new FixedLengthField(6));
  }
}