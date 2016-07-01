package vn.com.tivn.hsm.phw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Test1  {
  public static void main(String[] args) throws Exception{
    Socket s = new Socket("192.168.5.123", 9090);
    OutputStream out = new BufferedOutputStream(s.getOutputStream());
    InputStream in = new BufferedInputStream(s.getInputStream());
    
    byte[] b = new byte[4];
    b[0] = 0x01;
    b[1] = 0x01;
    b[2] = 0x00;
    b[3] = 0x00;
    out.write(b);
    
    b = new byte[2];
    b[0] = 0x00;
    b[1] = 0x01;
    out.write(b);

    b = new byte[1];
    b[0] = 0x01;
    out.write(b);

    out.flush();
    
    b = new byte[20];
    in.read(b);
    System.out.println(b);
  }
}
