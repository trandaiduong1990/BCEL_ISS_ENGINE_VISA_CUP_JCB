package vn.com.tivn.hsm.phw;

public class IT_PPK_GEN_Response extends HSMMsg
{
  public IT_PPK_GEN_Response()
  {
    super.setFunctionCode((byte)0x41);
    super.addField("RC", new FixedLengthField());
    super.addField("eKTM", new FixedLengthField(8));
    super.addField("eKM", new FixedLengthField(8));
  }
}