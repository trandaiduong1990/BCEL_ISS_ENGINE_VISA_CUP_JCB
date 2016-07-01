package vn.com.tivn.hsm.phw;

public abstract class HSMResponseMsg extends HSMMsg {
  public HSMResponseMsg() {
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
