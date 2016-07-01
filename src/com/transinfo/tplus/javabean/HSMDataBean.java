package com.transinfo.tplus.javabean;

import org.jpos.iso.ISOUtil;

import vn.com.tivn.hsm.phw.EracomPHW;

import com.transinfo.tplus.util.DBManager;
import com.transinfo.tplus.util.TPlusResultSet;

public class HSMDataBean
{
    private String ip;
    private String port;
    private String maxConn;
    private String weight;
    private String oldIp;
    private DBManager objDBManager = null;

    public HSMDataBean(){

          StringBuffer strSql = new StringBuffer();
          try{
              objDBManager = new DBManager();
              strSql = new StringBuffer();
              strSql.append("SELECT HSM_IP, HSM_PORT, MAX_CONN, WEIGHT FROM HSM");
              System.out.println(strSql.toString());
              objDBManager.executeSQL(strSql.toString());
              TPlusResultSet rs = objDBManager.getResultSet();
              if(rs.next()){
                  System.out.println("Getting HSM Info ...... ");
                  this.setIp(rs.getString("HSM_IP"));
                  this.setPort(rs.getString("HSM_PORT"));
                  this.setMaxConn(rs.getString("MAX_CONN"));
                  this.setWeight(rs.getString("WEIGHT"));
              }else{
                  throw new Exception("<<< NO VALID HSM >>>");
              }
          }catch(Exception vep) {
              System.out.println("Exception while getting HSM Information : "+vep.toString());
          }
    }

    public void initHSM() throws Exception{
      EracomPHW.init(this.getIp(), new Integer(this.getPort()).intValue(), new Integer(this.getMaxConn()).intValue(), new Integer(this.getWeight()).intValue());
      EracomPHW.INITIALIZED=true;
    }

    public void setIp(String ip)
    {
      this.ip = ip;
    }


    public String getIp()
    {
      return ip;
    }


    public void setPort(String port)
    {
      this.port = port;
    }


    public String getPort()
    {
      return port;
    }


    public void setMaxConn(String maxConn)
    {
      this.maxConn = maxConn;
    }


    public String getMaxConn()
    {
      return maxConn;
    }


    public void setWeight(String weight)
    {
      this.weight = weight;
    }


    public String getWeight()
    {
      return weight;
    }


    public void setOldIp(String oldIp)
    {
      this.oldIp = oldIp;
    }


    public String getOldIp()
    {
      return oldIp;
    }

    public static void main(String[] args)
    {
      HSMDataBean hsm = new HSMDataBean();
      try{
          hsm.initHSM();

          String data = "04203021CC2680" + "9704142222222222";
          byte[] endata = new byte[32];
          byte[] icv = "00000000".getBytes();
          byte[] ocv = "00000000".getBytes();

          EracomPHW.encryptData("1", true, icv, data.getBytes(), ocv, endata);

          System.out.println(ISOUtil.hexString(endata).substring(32));

          System.exit(0);

      }catch(Exception e){

      }

    }

}