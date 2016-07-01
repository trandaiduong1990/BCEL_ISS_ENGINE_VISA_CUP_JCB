package com.transinfo.hsm.phw;

public class TestHSM
{
  public TestHSM()
  {  }

  /**
   * Create test data for online testing
   */


  public static void createTestData1() throws Exception
   {

     String expDate = "0703";
     String expDate2 = "0706";
     String svcCode = "501";
     byte[] CVV = new byte[4];
     byte[] ePPK = new byte[8];
     byte[] newPinBlock = new byte[8];
     byte[] offset = new byte[12];
     String TerminalSessionKey = "60B7145F65E9276D";


	 PrintDataSet[] ds = new PrintDataSet[2];


       try
       {
         //EracomPHW.GenerateCVV(1,"4565922010129243".trim(),expDate,svcCode,CVV);
         EracomPHW.generateRandomPIN(4,EracomPHW.PIN_FORMAT_ANSI,"4565922010129243","1",ePPK);

         // EracomPHW.CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI,"4565922010129243","1",1,offset,NumberUtil.hexString(ePPK));

        // EracomPHW.TranslatePIN(NumberUtil.hexString(ePPK),"1",EracomPHW.PIN_FORMAT_ANSI,"4565922010129243",EracomPHW.PIN_FORMAT_ANSI, TerminalSessionKey,newPinBlock);

       }catch(Exception npe)
       {
		   System.out.println(npe);
         System.exit(0);
       }


     System.out.println("Test data created");
  }


  public static void createTestData() throws Exception
  {
/*
    String expDate = "0703";
    String expDate2 = "0706";
    String svcCode = "501";
    byte[] CVV = new byte[4];
    byte[] ePPK = new byte[8];
    byte[] newPinBlock = new byte[8];
    byte[] offset = new byte[12];
    String TerminalSessionKey = "60B7145F65E9276D";
    Class.forName("oracle.jdbc.driver.OracleDriver");
    Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.5.1:1521:tivn", "ticms", "ticms");
    Statement stmt = conn.createStatement();
    String query = "Select card_number from test_card_info where card_number <> '4123450100003855'";
    ResultSet rs = stmt.executeQuery(query);
    PreparedStatement pstmt = conn.prepareStatement("Update test_card_info set CVV = ?, pin_block = ?, track2 = ?, pvv_offset=? where card_number = ?");
    PreparedStatement pstmt_pcard = conn.prepareStatement("Update plastic_card set pvv_offset = ? where card_number = ?");
    while (rs.next())
    {
      try
      {
        EracomPHW.GenerateCVV(1,rs.getString("Card_Number").trim(),expDate,svcCode,CVV);
        EracomPHW.generateRandomPIN(4,EracomPHW.PIN_FORMAT_ANSI,rs.getString("Card_Number").trim(),"1",ePPK);
        EracomPHW.CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI,rs.getString("Card_Number").trim(),"1",1,offset,NumberUtil.hexString(ePPK));

        EracomPHW.TranslatePIN(NumberUtil.hexString(ePPK),"1",EracomPHW.PIN_FORMAT_ANSI,rs.getString("Card_Number").trim(),EracomPHW.PIN_FORMAT_ANSI, TerminalSessionKey,newPinBlock);

      }catch(NullPointerException npe)
      {
        System.exit(0);
      }

      pstmt.setString(1,(new String(CVV)).substring(0,3));
      pstmt.setString(2, NumberUtil.hexString(newPinBlock));
      pstmt.setString(3,rs.getString("Card_Number").trim()+"=101050117714"+(new String(CVV)).substring(0,3));
      pstmt.setString(4,new String(offset));
      pstmt.setString(5,rs.getString("Card_Number").trim());
      pstmt_pcard.setString(1,new String(offset));
      pstmt_pcard.setString(2,rs.getString("Card_Number").trim());
      pstmt.executeUpdate();
      pstmt_pcard.executeUpdate();
    }
    conn.commit();
    System.out.println("Test data created");
  }

  public static void main(String args[]) throws Exception
  {
    EracomPHW phw = new EracomPHW();
    String track2 = "2112000000512039 D 110550114626919";
    String cardnumber = "2112000000512039";
    String expDate = "1105";
    //String expDate2 = "0706";
    String svcCode = "501";
    phw.echo();
    phw.verifyCVV(20,cardnumber, expDate, svcCode, "919");

    byte[] CVV = new byte[4];
    byte[] ePPK = new byte[16];
    byte[] newPinBlock = new byte[8];
    byte[] offset = new byte[12];
     PrintDataSet[] ds = new PrintDataSet[2];

    ds[0] = new PrintDataSet();
    ds[0].setLineNo(new byte[]{20});
    ds[0].setColumnNo(new byte[]{1});
    ds[0].setData(cardnumber.getBytes());

    ds[1] = new PrintDataSet();
    ds[1].setLineNo(new byte[]{25});
    ds[1].setColumnNo(new byte[]{1});
    ds[1].setData(ePPK);
    //createTestData();



    /*
    Class.forName("oracle.jdbc.driver.OracleDriver");
    Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.5.1:1521:tivn", "ticisscb", "ticisscb");
    CallableStatement stmt = conn.prepareCall("BEGIN GetReport(?, ?, ?); END;");
    stmt.setString(1,"01-08-06");
    stmt.setString(2,"dd-mm-yy"); // DEPTNO
    stmt.registerOutParameter(3, OracleTypes.CURSOR); //REF CURSOR
    stmt.execute();
    ResultSet rs = ((OracleCallableStatement)stmt).getCursor(3);
    while (rs.next())
    {
      System.out.println("Name: "+rs.getString(1)+"   Dau ky pri: "+rs.getString(2));
    }
    conn.close();
    */
    //EracomPHW.generateRandomPIN(4,EracomPHW.PIN_FORMAT_ANSI,"4123450100003855",1,ePPK);
    //System.out.println("ePPK "+new String(ePPK));
    //EracomPHW.CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI,cardnumber,"1",1,offset,new String(ePPK));
   // System.out.println("Offset "+new String(offset));
    //EracomPHW.printPINfromPINBlock(1,"C4B8CACEF378A998",EracomPHW.PIN_FORMAT_ANSI,cardnumber,ds);
    //EracomPHW.verifyRandomPIN("1","4123450100003855",1,"C4B8CACEF378A998","718700000000",4);
   // System.out.println(new String(ePPK));

    //EracomPHW.GenerateTerminalKey("1",new byte[1],new byte[1],new byte[6]);
    //0200011110353230303636303030303030303037330101050430363037

    //EracomPHW.printPINfromPINBlock(1,new String(ePPK),EracomPHW.PIN_FORMAT_ANSI,cardnumber,ds);

    //EracomPHW.CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI,cardnumber,1,1,offset,new String(ePPK));
    //System.out.println("Offset "+new String(offset));
    System.out.println("Real length: "+"EE0E0500ABCDEF129839287503020001010106600000000752006600000000730200011110353230303636303030303030303037330101050430363037".length()/2);
    /*
    BufferedWriter writer = new BufferedWriter(new FileWriter("/applications/MCTestSuite/cardData.txt"));

    EracomPHW.GenerateCVV(1,"5506750000006377",expDate,svcCode,CVV);
    writer.write("CVV 5506750000006377 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004315",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004315 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004422",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004422 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004430",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004430 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004448",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004448 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004455",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004455 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004463",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004463 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004471",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004471 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000006385",expDate,svcCode,CVV);
    writer.write("CVV 5506750000006385 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004489",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004489 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004497",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004497 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000004505",expDate,svcCode,CVV);
    writer.write("CVV 5506750000004505 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000006005",expDate,svcCode,CVV);
    writer.write("CVV2 5506750000006005 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000006005",expDate2,svcCode,CVV);
    writer.write("CVV2 5506750000006005 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000000297",expDate2,svcCode,CVV);
    writer.write("CVV2 5506750000000297 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000000305",expDate2,svcCode,CVV);
    writer.write("CVV2 5506750000000305 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000000321",expDate2,svcCode,CVV);
    writer.write("CVV2 5506750000000321 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000000347",expDate2,svcCode,CVV);
    writer.write("CVV2 5506750000000347 "+new String(CVV));
    writer.newLine();
    EracomPHW.GenerateCVV(1,"5506750000000354",expDate2,svcCode,CVV);
    writer.write("CVV2 5506750000000354 "+new String(CVV));
    writer.newLine();
    writer.close();

    int ret = EracomPHW.verifyCVV(1,"5506750000000354",expDate2,svcCode,"447");
    if (ret==0)
      System.out.println("CVV matched");
    else
      System.out.println("CVV not matched ");
    //CVV 186
    System.out.println("rc "+EracomPHW.verifyCVV(1,cardnumber,expDate,svcCode,"186"));
    //EracomPHW.verifyCVV(1,cardnumber,expDate,svcCode,"298");
    //EracomPHW.receiveInterchangeSessionKey();
    //EracomPHW.GenerateTerminalKey(1,new byte[1],new byte[1],new byte[3],true);
    //EracomPHW.generateRandomPIN(1,cardnumber,12);
    //EracomPHW.echo();
    */

  }

  public static void main(String args[]) throws Exception
  {

   /* byte[] data = new byte[8];
    EracomPHW.encryptData(EracomPHW.defaultKey,false,null,"1111".getBytes(),null, data);
    System.out.println(NumberUtil.hexString(data));
    byte[] clearData = new byte[8];
    EracomPHW.decryptData(EracomPHW.defaultKey,false,null,data,null, clearData);
    System.out.println(NumberUtil.hexString(clearData));
    byte[] pvv = new byte[2];
    EracomPHW.generatePVVFromEncryptedPIN(1,"1897019000050001",EracomPHW.defaultKey,EracomPHW.PIN_FORMAT_ANSI,NumberUtil.hexString(data),4,pvv);
    System.out.println(NumberUtil.hexString(pvv));
    */

    /* System.out.println("Connecting to HSM");
     EracomPHW.init("192.168.1.25",1000,1,1000);
     createTestData1();
     */

    /*EracomPHW.init("192.168.1.25",1000,1,1000);
    byte[] data = new byte[16];
    EracomPHW.encryptData("1",false,null,"1111111111111".getBytes(),null, data);
    System.out.println("Encrypted Data="+NumberUtil.hexString(data));
    byte[] clearData = new byte[16];
    EracomPHW.decryptData("1",false,null,data,null, clearData);
    */

   /*
    byte [] eKTM = new byte[8];
    byte [] KS = new byte[8];
    byte [] KVC = new byte[8];
	System.out.println("GenerateTerminalKey");
    EracomPHW.GenerateTerminalKey("1",eKTM,KS,KVC);
    System.out.println(NumberUtil.hexString(eKTM));
    System.out.println("Key to store: "+NumberUtil.hexString(KS));
    System.out.println("KVC : "+NumberUtil.hexString(KVC));
    */

   /*
    SecretKey key = new SecretKeySpec(NumberUtil.hex2byte("1111111111111111"), "DES");
    Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] cipherText = cipher.doFinal(eKTM);

    System.out.println(NumberUtil.hexString(cipherText));

    SecretKey key1 = new SecretKeySpec(cipherText, "DES");
    cipher.init(Cipher.ENCRYPT_MODE, key1);
    System.out.println("Check KVC "+NumberUtil.hexString(cipher.doFinal(NumberUtil.hex2byte("0000000000000000"))));
    System.out.println("Terminal clear key "+NumberUtil.hexString(cipherText));
    */

   /* EracomPHW.init("192.168.1.25",1000,1,1000);
    byte [] clearRandomPIN = new byte[8];
    int i = EracomPHW.generateClearRandomPIN(4,clearRandomPIN);
    System.out.println("RC "+i+" "+NumberUtil.hexString(clearRandomPIN));
	*/

	/*EracomPHW.init("192.168.1.25",1000,1,1000);

    byte[] ePINBlock = new byte[8];
    EracomPHW.generateClearRandomPIN(4, ePINBlock);
    byte[] PVV = new byte [2];
    EracomPHW.generatePVVFromEncryptedPIN(1,"1897019000050033",EracomPHW.defaultKey, EracomPHW.PIN_FORMAT_ANSI,NumberUtil.hexString(ePINBlock), 4, PVV);
     System.out.println("***********************************************************");
    byte[] clearPIN = new byte[8];
    EracomPHW.decryptData(EracomPHW.defaultKey,false,null,ePINBlock,null, clearPIN);
    System.out.println("Clear PIN "+new String (clearPIN));
    System.out.println("PVV "+NumberUtil.hexString(PVV));*/

	EracomPHW.init("192.168.1.25",1000,1,1000);
	byte[] newPinBlock = new byte[8];
	String TerminalSessionKey = "60B7145F65E9276D";
    EracomPHW.TranslatePIN("9B0AD99E39830A1D","2",EracomPHW.PIN_FORMAT_ANSI,"8888880100024447",EracomPHW.PIN_FORMAT_ANSI, "1",newPinBlock);

    //byte[] CVV = new byte[2];
    //EracomPHW.GenerateCVV(1, "1897019000050033", "1204", "501", CVV);
    System.out.println(EracomPHW.verifyCVV(1,"1897019000050033","1204","501","289"));
   /* EracomPHW.init("192.168.1.25",1000,1,1000);
    byte [] clearPIN = new byte[8];
    byte [] PINBlock = new byte[8];
    //EracomPHW.decryptData("1",false,null,NumberUtil.hex2byte("D188229A9E880CBD"),null, clearPIN);
    EracomPHW.buildPINBlockFromEncryptedPIN("8888880100024447", "D188229A9E880CBD", PINBlock, 6);
    byte[] b = new byte[]{(byte)0x98,(byte)0xC0,(byte)0x82,(byte)0x74};
    System.out.println(NumberUtil.hexString(b));*/
    }
}