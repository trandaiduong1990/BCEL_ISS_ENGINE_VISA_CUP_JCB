package com.transinfo.hsm.phw;

public class PIN_Translate_Request extends HSMMsg
{
  protected FixedLengthField FM = new FixedLengthField(1);
  protected FixedLengthField ePPKi = new FixedLengthField(8);
  protected VariableLengthField PPKi_Spec = new VariableLengthField();
  protected FixedLengthField PFi = new FixedLengthField(1);
  protected FixedLengthField ANB = new FixedLengthField(6);
  protected FixedLengthField PFo = new FixedLengthField(1);
  protected VariableLengthField PPKo_Spec = new VariableLengthField();

  public PIN_Translate_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x02});
    FM.setContent(new byte[]{0x00});
    super.addField("FM",FM);
    super.addField("ePPKi",ePPKi);
    super.addField("PPKi_Spec",PPKi_Spec);
    super.addField("PFi",PFi);
    super.addField("ANB",ANB);
    super.addField("PFo",PFo);
    super.addField("PPKo_Spec",PPKo_Spec);
  }
}