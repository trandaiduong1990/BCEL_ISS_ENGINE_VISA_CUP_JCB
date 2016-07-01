package vn.com.tivn.hsm.phw;

public class CalculatePVVfromOffset_Response extends HSMMsg
{
  protected FixedLengthField rc = new FixedLengthField(1);
  protected FixedLengthField PVV = new FixedLengthField(2);
  public CalculatePVVfromOffset_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x06});
    super.addField("RC",rc);
    super.addField("PVV",PVV);
  }
}