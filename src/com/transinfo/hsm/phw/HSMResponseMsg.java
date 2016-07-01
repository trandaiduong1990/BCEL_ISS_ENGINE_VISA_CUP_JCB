package com.transinfo.hsm.phw;

public abstract class HSMResponseMsg extends HSMMsg {
  public HSMResponseMsg() {
  }
  public byte[] getFunctionCode()
  {
    return null;
  }

  public byte[] pack()
  {
    // TODO:  Implement this com.transinfo.hsm.phw.HSMMsg abstract method
    return null;
  }

  public void unpack(byte[] data)
  {
    // TODO:  Implement this com.transinfo.hsm.phw.HSMMsg abstract method
  }
}
