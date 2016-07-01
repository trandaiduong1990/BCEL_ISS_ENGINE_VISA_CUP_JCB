package vn.com.tivn.hsm.phw;

public class VerifyPIN_PVV_Request extends HSMMsg
{
  protected FixedLengthField FM = new FixedLengthField(1);
  protected FixedLengthField ePPK = new FixedLengthField(8);
  protected VariableLengthField PPK_Spec = new VariableLengthField();
  protected FixedLengthField PF = new FixedLengthField(1);
  protected FixedLengthField ANB = new FixedLengthField(6);
  protected VariableLengthField PVVK_Spec = new VariableLengthField();
  protected FixedLengthField TSP12 = new FixedLengthField(6);
  protected FixedLengthField PVV = new FixedLengthField(2);
  
  public VerifyPIN_PVV_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x05});
    FM.setContent(new byte[]{0x00});
    super.addField("FM",FM);
    super.addField("ePPK",ePPK);
    super.addField("PPK_Spec",PPK_Spec);
    super.addField("PF",PF);
    super.addField("ANB",ANB);
    super.addField("PVVK_Spec",PVVK_Spec);
    super.addField("TSP12",TSP12);
    super.addField("PVV",PVV);
  }
}