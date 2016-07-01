package com.transinfo.hsm.phw;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TCPIPConnectionPool extends ObjectPool {
  public static class HostInfo{
    String host;
    int port;
    int maxConn;
    int weight;

    int usedConn = 0;

    HostInfo(String host, int port, int maxConn, int weight){
      this.host = host;
      this.port = port;
      this.maxConn = maxConn;
      this.weight = weight;
    }
  }

  private static int requests = 0;
  List hosts = new Vector();
  Map openedConnection = new Hashtable();

  public TCPIPConnectionPool() {
  }

  private HostInfo getDesiredHostInfo(){
    int totalWeight = 0;
    for (int i=0; i<hosts.size(); i++){
      HostInfo hostInfo = (HostInfo)hosts.get(i);
      totalWeight += hostInfo.weight;
    }
    int random = (int)(Math.random() * totalWeight);

    totalWeight = 0;
    for (int i=0; i<hosts.size(); i++){
      HostInfo hostInfo = (HostInfo)hosts.get(i);
      totalWeight += hostInfo.weight;
      if (random < totalWeight){
        return hostInfo;
      }
    }
    return null;
  }

  protected synchronized Object createObject() throws Exception{
    HostInfo hostInfo = getDesiredHostInfo();
    //if (hostInfo.usedConn < hostInfo.maxConn){
      TCPIPConnection conn = new TCPIPConnection(hostInfo.host, hostInfo.port);
      openedConnection.put(conn, hostInfo);
      hostInfo.usedConn++;
      return conn;
    /*}else{
      throw new RuntimeException("Maximum connections for host " + hostInfo.host + " is reached.");
    }*/
  }
  protected boolean validateObject(Object obj){
    try{
      TCPIPConnection conn = (TCPIPConnection)obj;
      return conn.isConnected();
    }catch(Exception e){
      e.printStackTrace();
      return false;
    }
  }

  //x void expire(Object obj){
  protected synchronized void releaseObject(Object obj){
    HostInfo hostInfo;
    if ((hostInfo = (HostInfo)openedConnection.get(obj)) != null){
      hostInfo.usedConn--;
    }
    try{
//?      ((TCPIPConnection)obj).close();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  protected synchronized void releaseAll()
  {
    while (!availableList.isEmpty())
    {
      try
      {
        ((TCPIPConnection)availableList.removeFirst()).close();
      }catch(IOException ignore)
      {
      }
    }
    HostInfo hostInfo = getDesiredHostInfo();
    super.removeAll();
    hostInfo.usedConn = 0;
  }

  public void addHostInfo(HostInfo hostInfo){
    hosts.add(hostInfo);
/*
    try
    {
      connectAll(hostInfo.maxConn);
    }catch(Exception e)
    {
      e.printStackTrace();
    }
*/
  }

  private void connectAll(int maxConn) throws Exception
  {
    int size = maxConn;
    TCPIPConnection[] conns = new TCPIPConnection[size];
    while (size>0)
    {
      conns[--size] = checkOutConnection();
    }
    for (int i=0;i<conns.length;i++)
      checkIn(conns[i]);
  }

  public TCPIPConnection checkOutConnection() throws Exception
  {
    TCPIPConnection conn = (TCPIPConnection)super.checkOut();
    if (!conn.isConnected())
    {
      System.out.println("Reconnecting ...");
      conn.reConnect();
      System.out.println("Connected");
    }
    //else
      System.out.println("1 connection found");
    int ret = 0;
    try
    {
      ret = EracomPHW.echo(conn);
    }catch (IOException ioe)
    {
      conn.reConnect();
    }catch (HSMException e)
    {
      //not start with SOH
      conn.reConnect();
    }
    if (ret !=0)
      conn.reConnect();
    requests = 0;
    return conn;
  }

  private static class TestTCPIPThread extends Thread{
    ObjectPool pool;
    TestTCPIPThread(ObjectPool pool){
      this.pool = pool;
    }
    public void run(){
      for (int i=0; i<10; i++){
        TCPIPConnection conn = null;
        try{
          // check out
          conn = (TCPIPConnection)pool.checkOut();

          // do some thing
          HSM_Status_Request request = new HSM_Status_Request();
          conn.send(request);
          HSMMsg response = conn.receive();
          System.out.println(response.getSequenceNumber());
          System.out.println("RC = " + NumberUtil.hexString(response.getFieldContent("RC")));

          Thread.sleep(500);
        }catch(Exception e){
          e.printStackTrace();
          if (conn != null){
            System.out.println("Error on " + conn.hashCode());
          }
        }finally{
          if (conn != null){
            pool.checkIn(conn);
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception{

    TCPIPConnectionPool pool = new TCPIPConnectionPool();
    pool.addHostInfo(new HostInfo("192.168.1.25", 1000, 2, 1000));

    pool.initCount = 2;
    pool.minCount = 2;
    pool.maxCount = 4;

   TCPIPConnection conns = (TCPIPConnection)pool.checkOut();
   HSM_Status_Request request = new HSM_Status_Request();
   conns.send(request);
   HSMMsg response = conns.receive();
   System.out.println("RC = " + NumberUtil.hexString(response.getFieldContent("RC")));

/*
    //
    int N = 5;
    TCPIPConnection[] conns = new TCPIPConnection[N];
    for (int i=0; i<N; i++){
      // check out
      conns[i] = (TCPIPConnection)pool.checkOut();
    }

    for (int i=0; i<N; i++){
      // do some thing
      HSM_Status_Request request = new HSM_Status_Request();
      conns[i].send(request);
      HSMMsg response = conns[i].receive();
      System.out.println("RC = " + NumberUtil.hexString(response.getFieldContent("RC")));
    }
    for (int i=0; i<N; i++){
      // check in
      pool.checkIn(conns[i]);
    }
    TCPIPConnection conn = (TCPIPConnection)pool.checkOut();
    HSM_Status_Request request = new HSM_Status_Request();
    conn.send(request);
    HSMMsg response = conn.receive();
    System.out.println(response.getSequenceNumber());
    System.out.println("RC = " + NumberUtil.hexString(response.getFieldContent("RC")));
    // Thread test
    int MAX_THREAD = 20;
    for (int i=0; i<MAX_THREAD; i++){
      new TestTCPIPThread(pool).start();
    }
    */
  }
}
