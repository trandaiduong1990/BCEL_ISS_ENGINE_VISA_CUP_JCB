package vn.com.tivn.hsm.phw;

public class CalculateOffsetfromPIN_Request extends HSMMsg
{
  public CalculateOffsetfromPIN_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x04});
    super.addField("FM",new FixedLengthField(1));
    super.addField("ePPK",new FixedLengthField(8));
    super.addField("PPK_Spec",new VariableLengthField());
    super.addField("PF",new FixedLengthField());
    super.addField("ANB",new FixedLengthField(6));
    super.addField("PVK_Spec",new VariableLengthField());
    super.addField("ValidationData",new FixedLengthField(8));
  }
}