package vn.com.tivn.hsm.phw;

public class NumberUtil 
{
  public NumberUtil()
  {
  }
  /**
   * converts a byte array to hex string 
   * (suitable for dumps and ASCII packaging of Binary fields
   * @param b - byte array
   * @return String representation
   * @author apr@cs.com.uy
   * @author Hani S. Kirollos
   * @author Alwyn Schoeman 
   * Copyright (c) 2000 jPOS.org.  All rights reserved.
  */
  public static String hexString(byte[] b) {
    StringBuffer d = new StringBuffer(b.length * 2);
    for (int i=0; i<b.length; i++) {
        char hi = Character.forDigit ((b[i] >> 4) & 0x0F, 16);
        char lo = Character.forDigit (b[i] & 0x0F, 16);
        d.append(Character.toUpperCase(hi));
        d.append(Character.toUpperCase(lo));
    }
    return d.toString();
  }
  
  /**
   * @param   b       source byte array
   * @param   offset  starting offset
   * @param   len     number of bytes in destination (processes len*2)
   * @return  byte[len]
   */
  public static byte[] hex2byte (byte[] b, int offset, int len) {
    byte[] d = new byte[len];
    for (int i=0; i<len*2; i++) {
      int shift = i%2 == 1 ? 0 : 4;
      d[i>>1] |= Character.digit((char) b[offset+i], 16) << shift;
    }
    return d;
  }
  /**
   * @param s source string (with Hex representation)
   * @return byte array
   */
  public static byte[] hex2byte (String s) {
    //left pad the string with 0 if its length is odd
    if (s.length()%2==1)
      s="0"+s;
    return hex2byte (s.getBytes(), 0, s.length() >> 1);
  }
}