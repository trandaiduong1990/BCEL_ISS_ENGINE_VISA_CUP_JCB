package vn.com.tivn.hsm.phw;

public class Gen_ARPC_Response extends HSMMsg
{
  public Gen_ARPC_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x20,(byte)0x18});
    super.addField("RC",new FixedLengthField(1));
    super.addField("ARPC",new VariableLengthField());

  }
}