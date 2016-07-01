package vn.com.tivn.hsm.phw;

public class PIN_Translate_Response extends HSMMsg
{
  public PIN_Translate_Response()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x06,(byte)0x02});
    super.addField("RC",new FixedLengthField());
    super.addField("ePPKo",new FixedLengthField(8));
  }
}