package com.transinfo.hsm.phw;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

public class EracomPHW
{
  //
  private static TCPIPConnectionPool pool = new TCPIPConnectionPool();
  private static Logger logger = Logger.getLogger(ObjectPool.class.getName());
  public static boolean INITIALIZED = false;
  //Key flags
  public static final byte[]    KEY_SINGLE_LEN_DPK	= {0x00,0x01};
  public static final byte[]	KEY_SINGLE_LEN_PPK	= {0x00,0x02};
  public static final byte[]	KEY_SINGLE_LEN_MPK	= {0x00,0x04};
  public static final byte[]	KEY_DOUBLE_LEN_DPK	= {0x01,0x00};
  public static final byte[]	KEY_DOUBLE_LEN_PPK	= {0x02,0x00};
  public static final byte[]	KEY_DOUBLE_LEN_MPK	= {0x04,0x00};
  public static final byte[]	KEY_DOUBLE_LEN_KIS	= {0x08,0x00};
  public static final byte[]	KEY_BNV	= {0x0A,0x00};

  //PIN format
  public static final byte[]    PIN_FORMAT_ANSI	    =	{0x01};
  public static final byte[]	PIN_FORMAT_PINPAD 	=	{0x03};
  public static final byte[]	PIN_FORMAT_DOCUTEL  =	{0x08};
  public static final byte[]	PIN_FORMAT_ZKA		  =	{0x09};
  public static final byte[]	PIN_FORMAT_ISO0		  =	{0x10};
  public static final byte[]	PIN_FORMAT_ISO1		  =	{0x11};
  public static final byte[]	PIN_FORMAT_ISO3		  =	{0x13};

  // Cipher mode
  public static final byte[]	CIPHER_MODE_ECB		  =	{0x00};
  public static final byte[]	CIPHER_MODE_CBC		  =	{0x01};

  // PIN printing mode
  public static final boolean PRINT_FROM_HSM = false;

  //Default PPK
  public static final String defaultKey = "1";

  //HSM Type
  public static final boolean PHW_ISSUANCE = false;

  //msg counter
  private static int msg_counter = 0;

  public EracomPHW()
  {
  }

  public static boolean hasHost() {
    if(pool.hosts.size() > 0)
      return true;
    return false;
  }

  private static TCPIPConnection getConnection()
  {
    TCPIPConnection conn = null;
    try{
      conn = pool.checkOutConnection();
      if (++msg_counter%5==0)
        if (echo(conn)!=0)
        {
          conn.reConnect();
        }
    }catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
    return conn;
  }

  public static void addHost(String hostName, int hostPort) {
    // add host w/ default properties
    addHost(hostName,hostPort, 5, 100, 5, 5, 5);
  }

  public static void addHost(String hostName, int hostPort, int maxConn, int weight, int initCount, int minCount, int maxCount) {
    pool.addHostInfo(new TCPIPConnectionPool.HostInfo(hostName,hostPort,maxConn,weight));
    pool.initCount = initCount;
    pool.minCount = minCount;
    pool.maxCount = maxCount;
  }

  public static void init (String host, int port, int maxConn, int weight)
  {
    if(INITIALIZED)
      return;
      System.out.println("Not Intialized");
    pool.addHostInfo(new TCPIPConnectionPool.HostInfo(host,port,maxConn,weight));
    pool.initCount = 2;
    pool.minCount = 2;
    pool.maxCount = maxConn;
  }

  public static void reinit (String host, int port, int maxConn, int weight)
  {
    pool.releaseAll();
    pool.addHostInfo(new TCPIPConnectionPool.HostInfo(host,port,maxConn,weight));
    pool.initCount = 2;
    pool.minCount = 2;
    pool.maxCount = maxConn;
  }

  public static int echo()
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("HSMStatus");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  public static int echo(TCPIPConnection conn) throws IOException, SocketException, HSMException
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("HSMStatus");
    //TCPIPConnection conn = null;
    //conn = getConnection();
    conn.send(req);
    logger.info("Message sent "+NumberUtil.hexString(req.pack()));
    msg = conn.receive();
    if (msg instanceof DummyResponse)
      return msg.getResponseCode();
    logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
    return msg.getResponseCode();
  }

  public static byte[] buildCVVValidationData(String cardNumber, String expDate, String serviceCode)
	{
    int i;

    byte[] verificationData = new byte[16];

    for (i=0; i<8; i++){
      verificationData[i] = (byte)(((cardNumber.charAt(2*i)-'0') * 16) + (cardNumber.charAt(2*i+1)-'0'));
    }
    verificationData[8] = (byte)(((expDate.charAt(0)-'0') * 16) + (expDate.charAt(1)-'0'));
    verificationData[9] = (byte)(((expDate.charAt(2)-'0') * 16) + (expDate.charAt(3)-'0'));
    verificationData[10] = (byte)(((serviceCode.charAt(0)-'0') * 16) + (serviceCode.charAt(1)-'0'));
    verificationData[11] = (byte)(((serviceCode.charAt(2)-'0') * 16));
    verificationData[12] = 0x00;
    verificationData[13] = 0x00;
    verificationData[14] = 0x00;
    verificationData[15] = 0x00;
    return verificationData;
  }

  //41
  public static int GenerateInitialPPK(int KTM_Index, byte[] eKTM, byte[] eKM)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("GenerateInitialPPK");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      System.out.println("???????????????????SetContent="+NumberUtil.hex2byte(""+KTM_Index));
      req.setContent("n",NumberUtil.hex2byte(""+KTM_Index));
      System.out.println(NumberUtil.hexString(req.pack()));
      conn.send(req);
      logger.info("Message sent");
      msg = conn.receive();

      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(msg.getFieldContent("eKTM"),0,eKTM,0,eKTM.length);
      System.arraycopy(NumberUtil.hexString(msg.getFieldContent("eKM")).getBytes(),0,eKM,0,eKM.length);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //EE0400, EE0402
  public static int GenerateTerminalKey(String KTM_Spec, byte[] eKTM, byte[] KS_Spec, byte[] KVC)
  {
	  System.out.println("In GenerateTerminalKey");
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("GenerateKey");
    TCPIPConnection conn = null;
    try{
      System.out.println("Connecting to HSM ...");
      conn = getConnection();
      System.out.println("HSM found");
      //Modify whatever attribute(s) of the request here before sending
      if (KTM_Spec.length()<16)
        req.setContent("KTM_Spec",new Key(Integer.parseInt(KTM_Spec)));
      else
        req.setContent("KTM_Spec",new Key(KTM_Spec));
      if (KTM_Spec.length()==32)//doublelength
        req.setContent("Key_Flags",new byte[]{0x00,0x09});
      else
        req.setContent("Key_Flags",KEY_SINGLE_LEN_PPK);
        //req.setContent("Key_Flags",new byte[]{0x00,0x02});
      System.out.println("Request "+NumberUtil.hexString(req.pack()));
      conn.send(req);
      logger.info("Message sent");
      msg = conn.receive();
	System.out.println("Received response from host for GenerateTerminalKey "+NumberUtil.hexString(conn.getRawData()));
      logger.info("Received response from host for GenerateTerminalKey "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
        System.out.println("No DUMP Response");
      System.arraycopy(msg.getFieldContent("eKTM"),0,eKTM,0,eKTM.length);
      System.arraycopy(msg.getFieldContent("KS_Spec"),0,KS_Spec,0,KS_Spec.length);
      System.arraycopy(msg.getFieldContent("KVC"),0,KVC,0,3);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //EE0400, EE0402
  public static int GenerateMACKey(String KTM_Spec, byte[] eKTM, byte[] KS_Spec, byte[] KVC)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("GenerateKey");
    TCPIPConnection conn = null;
    try{
      System.out.println("Connecting to HSM ...");
      conn = getConnection();
      System.out.println("HSM found");
      //Modify whatever attribute(s) of the request here before sending
      if (KTM_Spec.length()<16)
        req.setContent("KTM_Spec",new Key(Integer.parseInt(KTM_Spec)));
      else
        req.setContent("KTM_Spec",new Key(KTM_Spec));
      if (KTM_Spec.length()==32)//doublelength
        req.setContent("Key_Flags",new byte[]{0x00,0x09});
      else
        req.setContent("Key_Flags",KEY_SINGLE_LEN_MPK);
        //req.setContent("Key_Flags",new byte[]{0x00,0x02});
      System.out.println(NumberUtil.hexString(req.pack()));
      conn.send(req);
      logger.info("Message sent");
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(msg.getFieldContent("eKTM"),0,eKTM,0,eKTM.length);
      System.arraycopy(msg.getFieldContent("KS_Spec"),0,KS_Spec,0,KS_Spec.length);
      System.arraycopy(msg.getFieldContent("KVC"),0,KVC,0,3);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //EE0602
  public static int TranslatePIN(String pinBlock, String PPKi_Spec, byte[] PFi, String Cardnumber, byte[] PFo, String PPKo_Spec, byte[] newPinBlock)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("TranslatePIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      //req.setContent("ePPKi",pinBlock.getBytes());
      req.setContent("ePPKi",NumberUtil.hex2byte(pinBlock));
      req.setContent("PPKi_Spec",new Key(PPKi_Spec));
      req.setContent("PFi",PFi);
      req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length()-13,Cardnumber.length()-1)));
      req.setContent("PFo",PFo);
      if (PPKo_Spec.length()<16)
        req.setContent("PPKo_Spec",new Key(Integer.parseInt(PPKo_Spec)));
      else
        req.setContent("PPKo_Spec",new Key(PPKo_Spec));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      //newPinBlock = new byte[msg.getFieldContent("ePPKo").length];
      System.arraycopy(msg.getFieldContent("ePPKo"),0,newPinBlock,0,newPinBlock.length);
      System.out.println("??/???>>>>Translate PIN="+NumberUtil.hexString(newPinBlock));
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    //return NumberUtil.hexString(msg.getFieldContent("ePPKo"));
    return msg.getResponseCode();
  }

  //EE0403
  public static int receiveInterchangeSessionKey(String KIR_Spec, byte[] Key_Flag , String eKIRvx, byte[] KS_Spec)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("SessionKey");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute of request here before sending
      Key KIR = new Key(KIR_Spec);
      if (KIR_Spec.length()==32)
        KIR.setKeyTypeHostStoredCBC();
      req.setContent("KIR_Spec", KIR);
      req.setContent("Key_Flags", Key_Flag);
      //eKIRvx is not a key spec !!!!
      req.setContent("eKIRvx", NumberUtil.hex2byte(eKIRvx));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();

      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(msg.getFieldContent("KS_Spec"),0,KS_Spec,0,KS_Spec.length);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  /*
  //EE0403
  public static int receiveInterchangeSessionKey(int index, byte[] KS_Spec, byte[] KVC, boolean doublelength)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("SessionKey");
    TCPIPConnection conn = null;
    Key kir =new Key();
    kir.setKeyTypeHostStored();
    try{
      conn = getConnection();
      //Modify whatever attribute of request here before sending
      req.setContent("KIR_Spec", new Key(index));
      if (doublelength)
        req.setContent("Key_Flags",KEY_DOUBLE_LEN_PPK);
      else
        req.setContent("Key_Flags",KEY_SINGLE_LEN_PPK);
      req.setContent("eKIRvx", kir.content);
      System.out.println(NumberUtil.hexString(req.getFieldContent("KIR_Spec")));
      System.out.println(NumberUtil.hexString(req.getFieldContent("eKIRvx")));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      System.out.println(NumberUtil.hexString(req.pack()));
      msg = conn.receive(); if (msg instanceof DummyResponse) 	return msg.getResponseCode();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      KS_Spec = new byte[msg.getFieldContent("KS_Spec").length];
      System.arraycopy(msg.getFieldContent("KS_Spec"),0,KS_Spec,0,KS_Spec.length);
      //KVC = new byte[3];
      System.arraycopy(NumberUtil.hexString(msg.getFieldContent("KVC")).getBytes(),0,KVC,0,6);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }
  */
  //9B
  public static int GenerateCVV(int index, String cardNumber, String expDate, String serviceCode, byte[] CVV)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("GenerateCVV");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("CVK_Index", NumberUtil.hex2byte(""+index));
      req.setContent("CVV_Data", buildCVVValidationData(cardNumber, expDate, serviceCode));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      //CVV = new byte[2];
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(msg.getFieldContent("CVV"),0,CVV,0,2);
      CVV = msg.getFieldContent("CVV");
      System.out.println("CVV="+NumberUtil.hexString(CVV));

    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
    //return NumberUtil.hexString(msg.getFieldContent("CVV")).substring(0,3);
  }

  //9C
  public static int verifyCVV(int index, String cardNumber, String expDate, String serviceCode, String CVV)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("VerifyCVV");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("CVK_Index", NumberUtil.hex2byte(""+index));
      req.setContent("CVV_Data", buildCVVValidationData(cardNumber, expDate, serviceCode));
      req.setContent("CVV", NumberUtil.hex2byte(CVV+"F"));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  public static int verifyCVV_I(String CVK_Spec, String cardNumber, String expDate, String serviceCode, String CVV)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("VerifyCVV_I");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("CVK_Spec", new Key(CVK_Spec));
      req.setContent("CVV_Data", buildCVVValidationData(cardNumber, expDate, serviceCode));
      req.setContent("CVV", NumberUtil.hex2byte(CVV+"F"));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //EE0606
  public static int CalculatePVVfromOffset(int KeyIndex, String Cardnumber, int pinKeyIndex, String offset, byte[] PVV)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("CalculatePVVfromOffset");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("PVK_Spec", new Key(pinKeyIndex));
      req.setContent("ValidationData",NumberUtil.hex2byte(Cardnumber));
      req.setContent("Offset4",NumberUtil.hex2byte(offset));
      req.setContent("PVVK_Spec", new Key(KeyIndex));
      //build TSP12
      String TSP12 = Cardnumber.substring(4,15);
      TSP12+=KeyIndex;
      req.setContent("TSP12", NumberUtil.hex2byte(TSP12.substring(0,12)));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      //PVV = new byte[msg.getFieldContent("PVV").length];
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(NumberUtil.hexString(msg.getFieldContent("PVV")).getBytes(),0,PVV,0,4);

    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //EE0607
  public static int generatePVVfromPIN(int KeyIndex, String Cardnumber, String PPK, String pinBlock, byte[] PINformat, byte[] PVV)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("GeneratePVVfromPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("ePPK",NumberUtil.hex2byte(pinBlock));
      req.setContent("PPK_Spec", new Key(PPK));
      req.setContent("PF", PINformat);
      req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(3,15)));
      req.setContent("PVVK_Spec", new Key(KeyIndex));
      //build TSP12
      String TSP12 = Cardnumber.substring(4,15);
      TSP12+=KeyIndex;
      req.setContent("TSP12", NumberUtil.hex2byte(TSP12.substring(0,12)));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      //PVV = new byte[msg.getFieldContent("PVV").length];
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(msg.getFieldContent("PVV"),0,PVV,0,2);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }

    return msg.getResponseCode();
  }

  //Clear PIN is encrypted under default key
  public static int generatePVVFromEncryptedPIN(int KeyIndex, String Cardnumber, String PPK, byte[] PINformat, String encryptedPIN, int PINlength, byte[] PVV)
  {
    byte [] PINBlock = new byte[8];
    int ret = buildPINBlockFromEncryptedPIN(Cardnumber, encryptedPIN, PINBlock, PINlength);
    if (ret!=0)
      return ret;
    return generatePVVfromPIN(KeyIndex, Cardnumber, PPK, NumberUtil.hexString(PINBlock), PINformat, PVV);
  }

  //EE0604
  public static int CalculateOffsetfromPIN(byte[] PF, String Cardnumber, String PPK, int PVKI, byte[] Offset, String ePPK)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("CalculateOffsetfromPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("ePPK", NumberUtil.hex2byte(ePPK));
      req.setContent("PPK_Spec", new Key(PPK));
      req.setContent("PF", PF);
      req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(3,15)));
      req.setContent("PVK_Spec", new Key(PVKI));
      req.setContent("ValidationData", NumberUtil.hex2byte(Cardnumber));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      //Offset = new byte[6];
      System.arraycopy(msg.getFieldContent("Offset"),0,Offset,0,6);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }


  //EE0E04
  public static int generateRandomPIN(int PIN_Len, byte[] PFo, String Cardnumber, String PPK, byte[] ePPK)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("GenerateRandomPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("PIN_Len", new byte[]{(byte)PIN_Len});
      req.setContent("PFo", PFo);
      req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length()-13,Cardnumber.length()-1)));
      req.setContent("PPK_Spec", new Key(PPK));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      //ePPK = new byte[8];
      System.arraycopy(msg.getFieldContent("ePPK"),0,ePPK,0,8);
      System.out.println("############ Random PIN="+ePPK.length+"  "+NumberUtil.hexString(ePPK));
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //EE0605
  public static int verifyPIN_PVV(int PVKI, String Cardnumber, String PPK, String pinBlock, String PVV, byte[] PINformat)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("VerifyPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("ePPK",NumberUtil.hex2byte(pinBlock));
      req.setContent("PPK_Spec", new Key(PPK));
      req.setContent("PF",PINformat);
      req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length()-13,Cardnumber.length()-1)));
      req.setContent("PVVK_Spec", new Key(PVKI));
      //build TSP12
      String TSP12 = Cardnumber.substring(Cardnumber.length()-12,Cardnumber.length()-1);
      TSP12+=PVKI;
      req.setContent("TSP12", NumberUtil.hex2byte(TSP12.substring(0,12)));
      req.setContent("PVV",NumberUtil.hex2byte(PVV));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //EE0603
  public static int verifyRandomPIN(String PPK_Spec, String Cardnumber, int PVKIndex, String pinBlock,String offset, int PINLen)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("VerifyRandomPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      //req.setContent("ePPK",pinBlock.getBytes());
      req.setContent("ePPK",NumberUtil.hex2byte(pinBlock));
      /*if (PPK_Spec.length()<16)
        req.setContent("PPK_Spec",new Key(Integer.parseInt(PPK_Spec)));
      else
        req.setContent("PPK_Spec",new Key(PPK_Spec));*/
      req.setContent("PPK_Spec",new Key(PPK_Spec));
      //req.setContent("eKM",NumberUtil.hex2byte(PPK_Spec));
      req.setContent("PF",PIN_FORMAT_ANSI);
      //req.setContent("PAN",NumberUtil.hex2byte(Cardnumber));
      req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length()-13,Cardnumber.length()-1)));
      req.setContent("PVK_Spec", new Key(PVKIndex));
      req.setContent("ValidationData", NumberUtil.hex2byte(Cardnumber));
      //int len = offset.length();
      while (offset.length()<12)
        offset+="0";
      req.setContent("Offset", NumberUtil.hex2byte(offset));
      req.setContent("Check_Len", new byte[]{(byte)PINLen});
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  //E2
  public static int printRandomPIN(int PVK_Index, String PAN, int PIN_Len, byte[] offset)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("PrintRandomPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("PVK_Index",NumberUtil.hex2byte(""+PVK_Index));
      req.setContent("PAN",NumberUtil.hex2byte(PAN));
      req.setContent("PIN_Len",new byte[]{(byte)PIN_Len});
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      //offset = new byte[6];
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(NumberUtil.hexString(msg.getFieldContent("Offset")).getBytes(),0,offset,0,6);

    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  public static int printPINfromPINBlock(String PPK, String ePPK, byte[] PFi, String Cardnumber, PrintDataSet[] things2print)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("PrintPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("ePPK",NumberUtil.hex2byte(ePPK));
      req.setContent("PPK_Spec",new Key(PPK));
      req.setContent("PFi",PFi);
      req.setContent("ANB",NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length()-13,Cardnumber.length()-1)));
      req.setContent("PAN",NumberUtil.hex2byte(Cardnumber));
      ((PrintPIN_Request)req).addPrintData(things2print);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      String abc = NumberUtil.hexString(req.pack());
      System.out.println(abc);
      conn.send(req);
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    //return NumberUtil.hexString(msg.getFieldContent("Offset"));
    return msg.getResponseCode();
  }

  public static int GenerateMAC(byte[] ICD, byte[] MAC, String data, String MPK_Spec)
  {
    byte[] OCD = new byte[8];
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("MACUpdate");
    TCPIPConnection conn = null;
    while((data.length() % 8) != 0)
    {
      data = data + '\u0000';
    }
    try{
      conn = getConnection();
      /*
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("ICD",ICD);
      if (MPK_Spec.length()<16)
        req.setContent("MPK_Spec",new Key(Integer.parseInt(MPK_Spec)));
      else
        req.setContent("MPK_Spec",new Key(MPK_Spec));
      req.setContent("Data",data.getBytes());
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      msg = conn.receive(); if (msg instanceof DummyResponse) 	return msg.getResponseCode();
      OCD = msg.getFieldContent("OCD");
      */
      //finalize the MAC generation
      req = DefaultMessageFactory.createRequest("MACGenFinal");
      req.setContent("MACLength", new byte[] {(byte)MAC.length});
      //req.setContent("ICD",OCD);
      req.setContent("ICD",ICD);
      if (MPK_Spec.length()<16)
        req.setContent("MPK_Spec",new Key(Integer.parseInt(MPK_Spec)));
      else
        req.setContent("MPK_Spec",new Key(MPK_Spec));
      req.setContent("Data",data.getBytes());
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      conn.send(req);
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(msg.getFieldContent("MAC"),0,MAC,0,MAC.length);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  public static int VerifyMAC(byte[] ICD, byte[] MAC, String data, String MPK_Spec)
  {
    byte[] OCD = new byte[8];
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("MACUpdate");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("ICD",ICD);
      if (MPK_Spec.length()<16)
        req.setContent("MPK_Spec",new Key(Integer.parseInt(MPK_Spec)));
      else
        req.setContent("MPK_Spec",new Key(MPK_Spec));
      req.setContent("Data",data.getBytes());
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive(); if (msg instanceof DummyResponse) 	return msg.getResponseCode();
      OCD = msg.getFieldContent("OCD");

      //finalize the MAC generation
      req = DefaultMessageFactory.createRequest("MACGenFinal");
      req.setContent("MAClength", new byte[] {(byte)MAC.length});
      req.setContent("ICD",OCD);
      if (MPK_Spec.length()<16)
        req.setContent("MPK_Spec",new Key(Integer.parseInt(MPK_Spec)));
      else
        req.setContent("MPK_Spec",new Key(MPK_Spec));
      req.setContent("Data",data.getBytes());
      conn.send(req);

      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      msg = conn.receive();
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      System.arraycopy(msg.getFieldContent("MAC"),0,MAC,0,MAC.length);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  /**
   * Encrypt data
   * @return reason code: 0: successful, other: error
   * @param DPK_Spec: key spec for data protection key (DPK)
   * @param cbcMode: true CBC mode
   * @param icv: Input Chaining Value for CBC encipherment, ignored for ECB mode
   * @param data: data to be encrypted
   * @param ocv: Output Chaining Value for CBC encipherment, caller should allocate enough memory for it
   * @param encryptedData: encrypted data buffer, caller should allocate enough memory for it
   */
  public static int encryptData(String DPK_Spec, boolean cbcMode, byte[] icv, byte[] data, byte[] ocv, byte[] encryptedData)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("EncryptData");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      if (cbcMode){
        req.setContent("CM", CIPHER_MODE_CBC);
        req.setContent("ICV", icv);
      }
      if (DPK_Spec.length()<16)
        req.setContent("DPK_Spec",new Key(Integer.parseInt(DPK_Spec)));
      else
        req.setContent("DPK_Spec",new Key(DPK_Spec));
        // make sure data length is multiple by 8
      byte[] data8;
      if (data.length % 8 == 0){
        data8 = data;
      }else{
        data8 = new byte[(data.length/8 + 1) * 8];
        System.arraycopy(data, 0, data8, 0, data.length);
      }
      logger.info("DATA8???????????? "+NumberUtil.hexString(data8));
      req.setContent("Data", data8);
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      if (cbcMode){
        System.arraycopy(msg.getFieldContent("OCV"), 0, ocv, 0, ocv.length);
      }
      byte[] tmp = msg.getFieldContent("eDPK");
      System.arraycopy(tmp, 0, encryptedData, 0, Math.min(tmp.length, encryptedData.length));
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  /**
   * Decrypt data
   * @return reason code: 0: successful, other: error
   * @param DPK_Spec: key spec for data protection key (DPK)
   * @param cbcMode: true CBC mode
   * @param icv: Input Chaining Value for CBC encipherment, ignored for ECB mode
   * @param data: data to be decrypted
   * @param ocv: Output Chaining Value for CBC encipherment, caller should allocate enough memory for it
   * @param clearData: data buffer, caller should allocate enough memory for it
   */

  public static int decryptData(String DPK_Spec, boolean cbcMode, byte[] icv, byte[] data, byte[] ocv, byte[] clearData)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("DecryptData");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      if (cbcMode){
        req.setContent("CM", CIPHER_MODE_CBC);
        req.setContent("ICV", icv);
      }
      if (DPK_Spec.length()<16)
        req.setContent("DPK_Spec",new Key(Integer.parseInt(DPK_Spec)));
      else
        req.setContent("DPK_Spec",new Key(DPK_Spec));
      // make sure data length is multiple by 8
      byte[] data8;
      if (data.length % 8 == 0){
        data8 = data;
      }else{
        data8 = new byte[(data.length/8 + 1) * 8];
        System.arraycopy(data, 0, data8, 0, data.length);
      }
      req.setContent("Data", data8);
      conn.send(req);
      logger.info("Decrypt Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      if (cbcMode){
        System.arraycopy(msg.getFieldContent("OCV"), 0, ocv, 0, ocv.length);
      }
      byte[] tmp = msg.getFieldContent("Data");
      System.arraycopy(tmp, 0, clearData, 0, Math.min(tmp.length, clearData.length));
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }

  public static int encryptClearPIN(String ppkSpec, String cardNumber, String PIN, byte[] encryptedPIN)
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("EncryptClearPIN");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      req.setContent("Pin_Len", new byte[]{(byte)PIN.length()});
      if (PIN.length() % 2 != 0){
        PIN = "0" + PIN;
      }
      req.setContent("PIN", NumberUtil.hex2byte(PIN));
      String TSP12 = cardNumber.substring(3,15);
      byte[] ANB = NumberUtil.hex2byte(TSP12);
      req.setContent("ANB", ANB);
      if (ppkSpec.length()<16)
        req.setContent("PPK_Spec",new Key(Integer.parseInt(ppkSpec)));
      else
        req.setContent("PPK_Spec",new Key(ppkSpec));
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
      byte[] tmp = msg.getFieldContent("ePIN");
      System.out.println("Encrypted PIN Block.."+NumberUtil.hexString(tmp));
      System.arraycopy(tmp, 0, encryptedPIN, 0, encryptedPIN.length);
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }


  public static int generateClearRandomPIN(int PINlength, byte[] encryptedPIN)
  {
    if ((PINlength < 4)||(PINlength > 8)||(encryptedPIN.length!=8))
      return 11;
     String data = ""+(int)(Math.random()*Math.pow(10,PINlength));

    while(data.length()<PINlength)
      data="0"+data;

    return encryptData(defaultKey, false, null,data.getBytes(), null, encryptedPIN);
  }

  public static int buildPINBlockFromEncryptedPIN(String cardNumber, String encryptedPIN, byte[] PINBlock, int PINlength)
  {
    byte [] clearPIN = new byte[PINlength];
    decryptData(defaultKey, false, null, NumberUtil.hex2byte(encryptedPIN), null, clearPIN);
    System.out.println("##########################"+NumberUtil.hexString(clearPIN)+"  "+new String(clearPIN));
    return encryptClearPIN(defaultKey, cardNumber, "9999", PINBlock);
    //return encryptClearPIN("1", cardNumber, new String(clearPIN), PINBlock);
  }

  //standard format of all functions in this class
  public static int template()
  {
    HSMMsg msg = new HSMMsg();
    HSMMsg req = DefaultMessageFactory.createRequest("");
    TCPIPConnection conn = null;
    try{
      conn = getConnection();
      //Modify whatever attribute(s) of the request here before sending
      conn.send(req);
      logger.info("Message sent "+NumberUtil.hexString(req.pack()));
      msg = conn.receive();
      logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
      if (msg instanceof DummyResponse)
        return msg.getResponseCode();
    }catch(Exception e)
    {
      e.printStackTrace();
      return -1;
    }finally
    {
      pool.checkIn(conn);
    }
    return msg.getResponseCode();
  }
/*
  public static void main(String args[])
  {
    EracomPHW.init("192.168.1.25",1000,1,1000);
    EracomPHW.GenerateInitialPPK(1,new byte[8],new byte[8]);
    EracomPHW.GenerateInitialPPK(1,new byte[8],new byte[8]);
    EracomPHW.GenerateInitialPPK(1,new byte[8],new byte[8]);
  }
*/
}
