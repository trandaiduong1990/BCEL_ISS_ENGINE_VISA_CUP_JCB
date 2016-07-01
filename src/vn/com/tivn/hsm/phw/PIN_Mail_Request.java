package vn.com.tivn.hsm.phw;

public class PIN_Mail_Request extends HSMMsg
{
  public PIN_Mail_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xE2});
    super.addField("PVK_Index", new FixedLengthField());
    super.addField("PAN", new FixedLengthField(8));
    super.addField("PIN_Len", new FixedLengthField());
    FixedLengthField PIN_Type = new FixedLengthField();
    PIN_Type.setContent(new byte[]{0x01});
    super.addField("PIN_Type", PIN_Type);
  }
}