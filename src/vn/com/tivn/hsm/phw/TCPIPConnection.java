package vn.com.tivn.hsm.phw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPIPConnection extends BaseConnection {
  private static final byte SOH = 0x01;     // Start Of Header
  private static final byte VERSION = 0x01;

  private String host;
  private int port;

  private Socket socket;
  private InputStream in;
  private OutputStream out;

  private HSMMsg message;

  public TCPIPConnection(String host, int port) throws IOException{
    this.host = host;
    this.port = port;
System.out.println("Host and Port "+host+"   "+port);
    socket = new Socket(host, port);
    socket.setSoTimeout(5000);
    in = new BufferedInputStream(socket.getInputStream());
    out = new BufferedOutputStream(socket.getOutputStream());
  }

  public void close() throws IOException
  {
    socket.close();
  }

  public boolean isConnected() {
    return (socket.isConnected()&&
           (!socket.isInputShutdown())&&(!socket.isOutputShutdown()));
  }

  public void reConnect() throws IOException
  {
    //if ((socket!=null)&&(socket.isConnected()))
      //socket.close();
    if (socket!=null)
      socket.close();

    socket = new Socket(host, port);
    socket.setSoTimeout(5000);
    in = new BufferedInputStream(socket.getInputStream());
    out = new BufferedOutputStream(socket.getOutputStream());
  }

  protected Object getSendSynchObject() {
    return out;
  }

  protected void sendMessage(byte[] b, int offset, int len) throws IOException{
    System.out.println(b[0]);
    out.write(b, offset, len);
  }

  protected void sendMessageHeader(HSMMsg msg, int len) throws IOException{

    out.write(SOH);
    out.write(VERSION);
    int seqNumber = msg.getSequenceNumber();
    out.write(seqNumber >> 8);
    out.write(seqNumber);
  }

  protected void sendMessageLength(int len) throws IOException{
    out.write(len >> 8);
    out.write(len);
  }

  protected void sendMessageTrailer(HSMMsg msg, int len) throws IOException{
    out.flush();

    // No trailer for TCP/IP
  }

  protected Object getReceiveSynchObject() {
    return in;
  }

  protected byte[] getMessageHeader() throws IOException, HSMException{
    byte[] b = new byte[2];

    // SOH
    int soh = in.read();
    if (soh != SOH){
      System.out.println("SOH "+soh);
      throw new HSMException("Not start with SOH");
    }
    if (in.read() != VERSION){
      throw new HSMException("Different version");
    }
    in.read(b);
    return b;
  }

  protected void getMessage(byte[] b, int offset, int len) throws IOException{
    in.read(b, offset, len);
  }

  protected int getMessageLength() throws IOException{
    // TODO:  Implement this vn.com.tivn.hsm.phw.BaseConnection abstract method
    return in.read()*256 + in.read();
  }

  protected byte[] getMessageTrailer() {
    // TODO:  Implement this vn.com.tivn.hsm.phw.BaseConnection abstract method
    return null;
  }

  public void send(HSMMsg msg) throws IOException{
    this.message = msg;
    super.send(msg);
  }

  public HSMMsg receive() throws IOException, HSMException{
    try
    {
      return super.receive();
    }catch(HSMException hsme)
    {
      reConnect();
      super.send(message);
      return super.receive();
    }catch(IOException ioe)
    {
      reConnect();
      super.send(message);
      return super.receive();
    }
  }
}
