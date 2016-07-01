package vn.com.tivn.hsm.phw;
import java.io.ByteArrayOutputStream;

public class Gen_ARQC_ARPC_Request extends HSMMsg
{
  public Gen_ARQC_ARPC_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x20,(byte)0x18});
    super.addField("FM",new FixedLengthField());
    super.addField("ACTION",new FixedLengthField(1));
    super.addField("IMK_AC",new VariableLengthField());
    super.addField("MK_METHOD",new FixedLengthField(1));
    super.addField("MK_DATA",new VariableLengthField());
    super.addField("AC_KEY_METHOD",new FixedLengthField(1));
    super.addField("AC_KEY_DATA",new FixedLengthField(1));
    super.addField("AC_METHOD",new FixedLengthField(1));
    super.addField("AC_DATA",new VariableLengthField());
    super.addField("AC",new FixedLengthField(8));
    super.addField("ARPC_KEY_METHOD",new FixedLengthField(1));
    super.addField("ARPC_KEY_DATA",new FixedLengthField(1));
    super.addField("ARPC_METHOD",new FixedLengthField(1));
    super.addField("ARPC_DATA",new VariableLengthField());


  }


}