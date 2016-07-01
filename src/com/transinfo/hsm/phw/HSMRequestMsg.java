package com.transinfo.hsm.phw;

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
    // TODO:  Implement this com.transinfo.hsm.phw.HSMMsg abstract method
    return null;
  }

  public void unpack(byte[] data)
  {
    // TODO:  Implement this com.transinfo.hsm.phw.HSMMsg abstract method
  }
}
