package vn.com.tivn.hsm.phw;
import java.util.Hashtable;

public abstract class HSMRequestMsg extends HSMMsg {
  public HSMRequestMsg() {
    fields = new TIHashtable(1);
  }
  public byte[] getFunctionCode()
  {
    return null; 
  }

  public byte[] pack()
  {
    // TODO:  Implement this vn.com.tivn.hsm.phw.HSMMsg abstract method
    return null;
  }

  public void unpack(byte[] data)
  {
    // TODO:  Implement this vn.com.tivn.hsm.phw.HSMMsg abstract method
  }
}
