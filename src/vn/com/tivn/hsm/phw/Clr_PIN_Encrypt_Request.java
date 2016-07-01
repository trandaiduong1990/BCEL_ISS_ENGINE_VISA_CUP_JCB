package vn.com.tivn.hsm.phw;

public class Clr_PIN_Encrypt_Request extends HSMMsg
{
  protected FixedLengthField FM = new FixedLengthField(1);
  protected FixedLengthField Pin_Len = new FixedLengthField(1);
  protected VariableLengthField PIN = new VariableLengthField();
  protected FixedLengthField ANB = new FixedLengthField(6);
  protected VariableLengthField PPK_Spec = new VariableLengthField();
  
  public Clr_PIN_Encrypt_Request()
  {
    super.setFunctionCode(new byte[]{(byte)0xEE,(byte)0x06,(byte)0x00});
    super.addField("FM",FM);
    super.addField("Pin_Len",Pin_Len);
    super.addField("PIN",PIN);
    super.addField("ANB",ANB);
    super.addField("PPK_Spec",PPK_Spec);
  }
}