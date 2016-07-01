package com.transinfo.hsm.phw;

public class HSM_Status_Request extends HSMMsg
{
  //byte FunctionCode = 0x01;
  public HSM_Status_Request()
  {
    this.setFunctionCode(new byte[]{0x01});
  }

  public byte[] pack()
  {
    // TODO:  Implement this com.transinfo.hsm.phw.HSMMsg abstract method
    //byte[] temp = {FunctionCode};
    return FunctionCode;
  }

  public void unpack(byte[] data)
  {
    // TODO:  Implement this com.transinfo.hsm.phw.HSMMsg abstract method
  }

}