package vn.com.tivn.hsm.phw;

import java.io.IOException;


public abstract class BaseConnection extends Thread  {
  MessageFactory factory = new DefaultMessageFactory();
  private int status = 0; //use this flag to monitor the connection, 0=available, 1=occupied
  byte[] rawdata; //this one is for TESTING ONLY
  private int index = 0;

  public BaseConnection() throws IOException{
  }

  public abstract boolean isConnected();
  //public abstract boolean isClosed();
  //public abstract void close() throws IOException;
  //public abstract void open() throws IOException;
  //public abstract void setKeepAlive(boolean b) throws IOException;

  protected abstract void sendMessageHeader(HSMMsg msg, int len) throws IOException;
  protected abstract void sendMessageLength(int len) throws IOException;
  protected abstract void sendMessage(byte[] b, int offset, int len) throws IOException;
  protected abstract void sendMessageTrailer(HSMMsg msg, int len) throws IOException;

  protected abstract byte[] getMessageHeader() throws IOException, HSMException;
  protected abstract int getMessageLength() throws IOException, HSMException;
  protected abstract void getMessage(byte[] b, int offset, int len) throws IOException, HSMException;
  protected abstract byte[] getMessageTrailer() throws IOException, HSMException;

  public MessageFactory getFactory()
  {
    return factory;
  }
  public void setFactory(MessageFactory fac)
  {
    factory = fac;
  }

  protected abstract Object getSendSynchObject();
  protected abstract Object getReceiveSynchObject();

  public void send(HSMMsg msg) throws IOException{
    byte[] data = msg.pack();
    int datalength = data.length;
    System.out.println("Data length "+datalength);
    synchronized(getSendSynchObject()){
      sendMessageHeader(msg, data.length);
      sendMessageLength(data.length);
      sendMessage(data, 0, data.length);
      sendMessageTrailer(msg, data.length);
    }
    status = 1;

  }

  public void send(HSMMsg msg, byte[] data) throws IOException{
    int datalength = data.length;
    synchronized(getSendSynchObject()){
      sendMessageHeader(msg, data.length);
      sendMessageLength(data.length);
      sendMessage(data, 0, data.length);
      sendMessageTrailer(msg, data.length);
    }
    status = 1;

  }

  public HSMMsg receive() throws IOException, HSMException{
    if (!isConnected()){
      throw new HSMException("unconnected connection");
    }

    synchronized(getReceiveSynchObject()){
      byte[] header = getMessageHeader();
      int len = getMessageLength();
      byte[] data = new byte[len];
      rawdata = new byte[len];
      getMessage(data, 0, data.length);
      System.arraycopy(data,0,rawdata,0,len);
      getMessageTrailer();
      HSMMsg response = factory.createResponseMessage(header, data);
      try
      {
        response.unpack(data);
      }
      catch (Exception e)
      {
        DummyResponse dummy = new DummyResponse(getRawData()[getRawData().length-1]);
        return dummy;
      }
      status = 0;
      return response;
    }
  }

  public boolean isAvailable()
  {
    return status==0;
  }

  public void makeAvailable()
  {
    status=0;
  }

  public byte[] getRawData()
  {
    return rawdata;
  }

  public void block()
  {
    status = 1;
  }

  public void setIndex(int n)
  {
    index = n;
  }

  public int getIndex()
  {
    return index;
  }
}
