package com.transinfo.hsm.phw;

public class CVV_Verify_Request extends HSMMsg
{
  protected FixedLengthField CVK_Index = new FixedLengthField(1);
  protected FixedLengthField CVV_Data = new FixedLengthField(16);
  protected FixedLengthField CVV = new FixedLengthField(2);

  public CVV_Verify_Request()
  {
    //fields = new TIHashtable(4);
    super.setFunctionCode((byte)0x9C);
    super.addField("CVK_Index",CVK_Index);
    super.addField("CVV_Data",CVV_Data);
    super.addField("CVV",CVV);


  }
}