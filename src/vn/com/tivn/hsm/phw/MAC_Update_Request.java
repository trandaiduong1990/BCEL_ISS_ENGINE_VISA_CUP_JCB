package vn.com.tivn.hsm.phw;

public class MAC_Update_Request extends HSMMsg
{
  public MAC_Update_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x07,(byte)0x00});
    super.addField("FM", new FixedLengthField());
    super.addField("Alg", new FixedLengthField(0x01));
    super.addField("ICD", new FixedLengthField(8));
    super.addField("MPK_Spec", new VariableLengthField());
    super.addField("Data", new VariableLengthField());
  }
}