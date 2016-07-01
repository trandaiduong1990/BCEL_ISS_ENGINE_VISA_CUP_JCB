package com.transinfo.hsm.phw;

public interface MessageFactory  {
  public HSMMsg createResponseMessage(byte[] header, byte[] data);
  //public static HSMMsg createRequest(String msgtype);
}
