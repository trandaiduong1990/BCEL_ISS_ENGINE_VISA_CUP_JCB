package vn.com.tivn.hsm.phw;
import java.util.Hashtable;

public class HSM_Status_Response extends HSMMsg
{
  protected FixedLengthField rc = new FixedLengthField(1);
  protected FixedLengthField RAMStatus = new FixedLengthField(1);
  protected FixedLengthField ROMStatus = new FixedLengthField(1);
  protected FixedLengthField DESStatus = new FixedLengthField(1);
  protected FixedLengthField HostPortStatus = new FixedLengthField(1);
  protected FixedLengthField BatteryStatus = new FixedLengthField(1);
  protected FixedLengthField HDDStatus = new FixedLengthField(1);
  protected FixedLengthField RSAAccelerator = new FixedLengthField(1);
  protected FixedLengthField PerformanceLevel = new FixedLengthField(1);
  protected FixedLengthField ResetCount = new FixedLengthField(2);
  protected FixedLengthField Calls_in_last_minute = new FixedLengthField(4);
  protected FixedLengthField Calls_in_last_10_minutes = new FixedLengthField(4);
  protected FixedLengthField SoftwareIDLength = new FixedLengthField(1);
  
  
  public HSM_Status_Response()
  {
    //fields = new TIHashtable(14);
    super.setFunctionCode(new byte[]{0x01});
    super.addField("RC",rc); 
    super.addField("RAMStatus",RAMStatus);
    super.addField("ROMStatus",ROMStatus);
    super.addField("DESStatus",DESStatus);
    super.addField("HostPortStatus",HostPortStatus);
    super.addField("BatteryStatus",BatteryStatus);
    super.addField("HDDStatus",HDDStatus);
    super.addField("RSAAccelerator",RSAAccelerator);
    super.addField("PerformanceLevel",PerformanceLevel);
    super.addField("ResetCount",ResetCount);
    super.addField("Calls_in_last_minute",Calls_in_last_minute);
    super.addField("Calls_in_last_10_minutes",Calls_in_last_10_minutes);
    super.addField("SoftwareIDLength",SoftwareIDLength);
   }

  //public byte[] pack()
  //{
    // TODO:  Implement this vn.com.tivn.hsm.phw.HSMMsg abstract method
    //return null;
  //}

  public void unpack(byte[] data) throws HSMException
  {
    super.unpack(data);
    byte[] temp = SoftwareIDLength.getContent();
    int IDlength = (int)SoftwareIDLength.getContent()[0];
    FixedLengthField SoftwareID = new FixedLengthField(IDlength);
    byte[] content = new byte[IDlength];
    System.arraycopy(data,data.length-IDlength-1,content,0,IDlength);
    SoftwareID.setContent(content);
    super.addField("SoftwareID",SoftwareID);
  }
   
}