package vn.com.tivn.hsm.phw;
import java.util.Hashtable;

public class CVV_Verify_Response extends HSMMsg
{
  protected FixedLengthField rc = new FixedLengthField(1);
  public CVV_Verify_Response()
  {
    //fields = new TIHashtable(2);
    super.setFunctionCode((byte)0x9C);
    super.addField("RC",rc);
  }
}