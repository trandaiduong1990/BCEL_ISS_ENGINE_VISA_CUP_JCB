package com.transinfo.hsm.phw;

public class CVV_Generate_Response extends HSMMsg
{
  protected FixedLengthField rc = new FixedLengthField(1);
  protected FixedLengthField CVV = new FixedLengthField(2);
  public CVV_Generate_Response()
  {
    //fields = new TIHashtable(3);
    super.setFunctionCode((byte)0x9B);
    super.addField("RC",rc);
    super.addField("CVV",CVV);

  }
}