package vn.com.tivn.hsm.phw;

public class PIN_Translate_Request extends HSMMsg
{
  protected FixedLengthField FM = new FixedLengthField(1);
  protected FixedLengthField ePPKi = new FixedLengthField(8);
  protected KeySpecField PPKi_Spec = new KeySpecField();
  protected FixedLengthField PFi = new FixedLengthField(1);
  protected FixedLengthField ANB = new FixedLengthField(6);
  protected FixedLengthField PFo = new FixedLengthField(1);
  protected KeySpecField PPKo_Spec = new KeySpecField();

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