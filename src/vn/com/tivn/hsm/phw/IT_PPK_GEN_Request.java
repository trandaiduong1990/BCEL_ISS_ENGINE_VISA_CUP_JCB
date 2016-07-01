package vn.com.tivn.hsm.phw;

public class IT_PPK_GEN_Request extends HSMMsg
{
  public IT_PPK_GEN_Request()
  {
    super.setFunctionCode((byte)0x41);
    super.addField("n", new FixedLengthField());
  }
}