// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   EracomPHW.java

package vn.com.tivn.hsm.phw;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Logger;

// Referenced classes of package vn.com.tivn.hsm.phw:
//            HSMMsg, DummyResponse, Key, PrintPIN_Request,
//            TCPIPConnectionPool, HSMException, TCPIPConnection, DefaultMessageFactory,
//            NumberUtil, PrintDataSet

public class EracomPHW
{

	static Class _mthclass$(String a)
	{
		try
		{
			Class class1 = Class.forName(a);
			return class1;
		}
		catch(ClassNotFoundException e)
		{
			throw new NoClassDefFoundError(e.getMessage());
		}
	}

	public EracomPHW()
	{
	}

	public static boolean hasHost()
	{
		return pool.hosts.size() > 0;
	}

	private static TCPIPConnection getConnection()
	{
		TCPIPConnection conn = null;
		try
		{
			conn = pool.checkOutConnection();
			if(++msg_counter % 5 == 0 && echo(conn) != 0)
				conn.reConnect();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			TCPIPConnection tcpipconnection = null;
			return tcpipconnection;
		}
		return conn;
	}

	public static void addHost(String hostName, int hostPort)
	{
		addHost(hostName, hostPort, 5, 100, 5, 5, 5);
	}

	public static void addHost(String hostName, int hostPort, int maxConn, int weight, int initCount, int minCount, int maxCount)
	{
		pool.addHostInfo(new TCPIPConnectionPool.HostInfo(hostName, hostPort, maxConn, weight));
		pool.initCount = initCount;
		pool.minCount = minCount;
		pool.maxCount = maxCount;
	}

	public static void init(String host, int port, int maxConn, int weight)
	{
		if(INITIALIZED)
		{
			return;
		} else
		{
			pool.addHostInfo(new TCPIPConnectionPool.HostInfo(host, port, maxConn, weight));
			pool.initCount = 2;
			pool.minCount = 2;
			pool.maxCount = maxConn;
			return;
		}
	}

	public static void reinit(String host, int port, int maxConn, int weight)
	{
		pool.releaseAll();
		pool.addHostInfo(new TCPIPConnectionPool.HostInfo(host, port, maxConn, weight));
		pool.initCount = 2;
		pool.minCount = 2;
		pool.maxCount = maxConn;
	}

	public static void main(String args[])
	{
		init("192.168.6.95",1000,5,5000);
		
		String Cardnumber = "3569700000000077";
		String PPK = "2";
		int PVKI = 1;
		byte[] Offset = new byte [6];
		String ePPK = "6AFF93B9784A962F";
		
		int res = CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI, Cardnumber, PPK, PVKI, Offset, ePPK);
		System.out.println(res);
		
		System.out.println("PIN OFFSET :: " +NumberUtil.hexString(Offset));

		System.exit(0);
	}

	public static int echo()
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("HSMStatus");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int echo(TCPIPConnection conn)
			throws HSMException, SocketException, IOException
			{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("HSMStatus");
		conn.send(req);
		logger.info("Message sent " + NumberUtil.hexString(req.pack()));
		msg = conn.receive();
		if(msg instanceof DummyResponse)
		{
			return msg.getResponseCode();
		} else
		{
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			return msg.getResponseCode();
		}
			}

	public static byte[] buildCVVValidationData(String cardNumber, String expDate, String serviceCode)
	{
		byte verificationData[] = new byte[16];
		for(int i = 0; i < 8; i++)
			verificationData[i] = (byte)((cardNumber.charAt(2 * i) - 48) * 16 + (cardNumber.charAt(2 * i + 1) - 48));

		verificationData[8] = (byte)((expDate.charAt(0) - 48) * 16 + (expDate.charAt(1) - 48));
		verificationData[9] = (byte)((expDate.charAt(2) - 48) * 16 + (expDate.charAt(3) - 48));
		verificationData[10] = (byte)((serviceCode.charAt(0) - 48) * 16 + (serviceCode.charAt(1) - 48));
		verificationData[11] = (byte)((serviceCode.charAt(2) - 48) * 16);
		verificationData[12] = 0;
		verificationData[13] = 0;
		verificationData[14] = 0;
		verificationData[15] = 0;
		return verificationData;
	}

	public static int GenerateInitialPPK(int KTM_Index, byte eKTM[], byte eKM[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("GenerateInitialPPK");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("n", NumberUtil.hex2byte("" + KTM_Index));
			System.out.println(NumberUtil.hexString(req.pack()));
			conn.send(req);
			logger.info("Message sent");
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("eKTM"), 0, eKTM, 0, eKTM.length);
			System.arraycopy(NumberUtil.hexString(msg.getFieldContent("eKM")).getBytes(), 0, eKM, 0, eKM.length);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int GenerateTerminalKey(String KTM_Spec, byte eKTM[], byte KS_Spec[], byte KVC[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("GenerateKey");
		TCPIPConnection conn = null;
		try
		{
			System.out.println("Connecting to HSM ...");
			conn = getConnection();
			System.out.println("HSM found");
			if(KTM_Spec.length() < 16)
				req.setContent("KTM_Spec", new Key(Integer.parseInt(KTM_Spec)));
			else
				req.setContent("KTM_Spec", new Key(KTM_Spec));
			if(KTM_Spec.length() == 32)
				req.setContent("Key_Flags", new byte[] {
						0, 9
				});
			else
				req.setContent("Key_Flags", KEY_SINGLE_LEN_PPK);
			System.out.println("Request " + NumberUtil.hexString(req.pack()));
			conn.send(req);
			logger.info("Message sent");
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("eKTM"), 0, eKTM, 0, eKTM.length);
			System.arraycopy(msg.getFieldContent("KS_Spec"), 0, KS_Spec, 0, KS_Spec.length);
			System.arraycopy(msg.getFieldContent("KVC"), 0, KVC, 0, 3);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int GenerateMACKey(String KTM_Spec, byte eKTM[], byte KS_Spec[], byte KVC[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("GenerateKey");
		TCPIPConnection conn = null;
		try
		{
			System.out.println("Connecting to HSM ...");
			conn = getConnection();
			System.out.println("HSM found");
			if(KTM_Spec.length() < 16)
				req.setContent("KTM_Spec", new Key(Integer.parseInt(KTM_Spec)));
			else
				req.setContent("KTM_Spec", new Key(KTM_Spec));
			if(KTM_Spec.length() == 32)
				req.setContent("Key_Flags", new byte[] {
						0, 9
				});
			else
				req.setContent("Key_Flags", KEY_SINGLE_LEN_MPK);
			System.out.println(NumberUtil.hexString(req.pack()));
			conn.send(req);
			logger.info("Message sent");
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("eKTM"), 0, eKTM, 0, eKTM.length);
			System.arraycopy(msg.getFieldContent("KS_Spec"), 0, KS_Spec, 0, KS_Spec.length);
			System.arraycopy(msg.getFieldContent("KVC"), 0, KVC, 0, 3);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int TranslatePIN(String pinBlock, String PPKi_Spec, byte PFi[], String Cardnumber, byte PFo[], String PPKo_Spec, byte newPinBlock[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("TranslatePIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("ePPKi", NumberUtil.hex2byte(pinBlock));
			req.setContent("PPKi_Spec", new Key(PPKi_Spec));
			req.setContent("PFi", PFi);
			req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length() - 13, Cardnumber.length() - 1)));
			req.setContent("PFo", PFo);
			if(PPKo_Spec.length() < 16)
				req.setContent("PPKo_Spec", new Key(Integer.parseInt(PPKo_Spec)));
			else
				req.setContent("PPKo_Spec", new Key(PPKo_Spec));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("ePPKo"), 0, newPinBlock, 0, newPinBlock.length);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int receiveInterchangeSessionKey(String KIR_Spec, byte Key_Flag[], String eKIRvx, byte KS_Spec[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("SessionKey");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			Key KIR = new Key(KIR_Spec);
			if(KIR_Spec.length() == 32)
				KIR.setKeyTypeHostStoredCBC();
			req.setContent("KIR_Spec", KIR);
			req.setContent("Key_Flags", Key_Flag);
			req.setContent("eKIRvx", NumberUtil.hex2byte(eKIRvx));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("KS_Spec"), 0, KS_Spec, 0, KS_Spec.length);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int GenerateCVV(int index, String cardNumber, String expDate, String serviceCode, byte CVV[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("GenerateCVV");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("CVK_Index", NumberUtil.hex2byte("" + index));
			req.setContent("CVV_Data", buildCVVValidationData(cardNumber, expDate, serviceCode));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("CVV"), 0, CVV, 0, 2);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int verifyCVV(int index, String cardNumber, String expDate, String serviceCode, String CVV)
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("VerifyCVV");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("CVK_Index", NumberUtil.hex2byte("" + index));
			req.setContent("CVV_Data", buildCVVValidationData(cardNumber, expDate, serviceCode));
			req.setContent("CVV", NumberUtil.hex2byte(CVV + "F"));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		System.out.println("VN CVV Res Send");
		return msg.getResponseCode();
	}

	public static int verifyCVV_I(String CVK_Spec, String cardNumber, String expDate, String serviceCode, String CVV)
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("VerifyCVV_I");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("CVK_Spec", new Key(CVK_Spec));
			req.setContent("CVV_Data", buildCVVValidationData(cardNumber, expDate, serviceCode));
			req.setContent("CVV", NumberUtil.hex2byte(CVV + "F"));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int CalculatePVVfromOffset(int KeyIndex, String Cardnumber, int pinKeyIndex, String offset, byte PVV[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("CalculatePVVfromOffset");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("PVK_Spec", new Key(pinKeyIndex));
			req.setContent("ValidationData", NumberUtil.hex2byte(Cardnumber));
			req.setContent("Offset4", NumberUtil.hex2byte(offset));
			req.setContent("PVVK_Spec", new Key(KeyIndex));
			String TSP12 = Cardnumber.substring(Cardnumber.length() - 12, Cardnumber.length() - 1);
			TSP12 = TSP12 + KeyIndex;
			req.setContent("TSP12", NumberUtil.hex2byte(TSP12.substring(0, 12)));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(NumberUtil.hexString(msg.getFieldContent("PVV")).getBytes(), 0, PVV, 0, 4);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int generatePVVfromPIN(int KeyIndex, String Cardnumber, String PPK, String pinBlock, byte PINformat[], byte PVV[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("GeneratePVVfromPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("ePPK", NumberUtil.hex2byte(pinBlock));
			req.setContent("PPK_Spec", new Key(PPK));
			req.setContent("PF", PINformat);
			req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length() - 13, Cardnumber.length() - 1)));
			req.setContent("PVVK_Spec", new Key(KeyIndex));
			String TSP12 = Cardnumber.substring(Cardnumber.length() - 12, Cardnumber.length() - 1);
			TSP12 = TSP12 + KeyIndex;
			req.setContent("TSP12", NumberUtil.hex2byte(TSP12.substring(0, 12)));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("PVV"), 0, PVV, 0, 2);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int generatePVVFromEncryptedPIN(int KeyIndex, String Cardnumber, String PPK, byte PINformat[], String encryptedPIN, int PINlength, byte PVV[])
	{
		/*byte PINBlock[] = new byte[8];
        int ret = buildPINBlockFromEncryptedPIN(Cardnumber, encryptedPIN, PINBlock, PINlength,PPK);
        if(ret != 0)
            return ret;
        else
            //return generatePVVfromPIN(KeyIndex, Cardnumber, PPK, NumberUtil.hexString(PINBlock), PINformat, PVV);
            return CalculateOffsetfromPIN(PIN_FORMAT_ANSI,Cardnumber,PPK,KeyIndex,PVV,NumberUtil.hexString(PINBlock));
		 */
		return CalculateOffsetfromPIN(PIN_FORMAT_ANSI,Cardnumber,PPK,KeyIndex,PVV,encryptedPIN);
	}

	public static int CalculateOffsetfromPIN(byte PF[], String Cardnumber, String PPK, int PVKI, byte Offset[], String ePPK)
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("CalculateOffsetfromPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("ePPK", NumberUtil.hex2byte(ePPK));
			req.setContent("PPK_Spec", new Key(PPK));
			req.setContent("PF", PF);
			req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length() - 13, Cardnumber.length() - 1)));
			req.setContent("PVK_Spec", new Key(PVKI));
			req.setContent("ValidationData", NumberUtil.hex2byte(Cardnumber));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("Offset"), 0, Offset, 0, 6);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int generateRandomPIN(int PIN_Len, byte PFo[], String Cardnumber, String PPK, byte ePPK[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("GenerateRandomPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("PIN_Len", new byte[] {
					(byte)PIN_Len
			});
			req.setContent("PFo", PFo);
			req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length() - 13, Cardnumber.length() - 1)));
			req.setContent("PPK_Spec", new Key(PPK));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("ePPK"), 0, ePPK, 0, 8);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int verifyPIN_PVV(int PVKI, String Cardnumber, String PPK, String pinBlock, String PVV, byte PINformat[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("VerifyPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("ePPK", NumberUtil.hex2byte(pinBlock));
			req.setContent("PPK_Spec", new Key(PPK));
			req.setContent("PF", PINformat);
			req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length() - 13, Cardnumber.length() - 1)));
			req.setContent("PVVK_Spec", new Key(PVKI));
			String TSP12 = Cardnumber.substring(Cardnumber.length() - 12, Cardnumber.length() - 1);
			TSP12 = TSP12 + PVKI;
			req.setContent("TSP12", NumberUtil.hex2byte(TSP12.substring(0, 12)));
			req.setContent("PVV", NumberUtil.hex2byte(PVV));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int verifyRandomPIN(String PPK_Spec, String Cardnumber, int PVKIndex, String pinBlock, String offset, int PINLen)
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("VerifyRandomPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("ePPK", NumberUtil.hex2byte(pinBlock));
			req.setContent("PPK_Spec", new Key(PPK_Spec));
			req.setContent("PF", PIN_FORMAT_ANSI);
			req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length() - 13, Cardnumber.length() - 1)));
			req.setContent("PVK_Spec", new Key(PVKIndex));
			req.setContent("ValidationData", NumberUtil.hex2byte(Cardnumber));
			for(; offset.length() < 12; offset = offset + "0");
			req.setContent("Offset", NumberUtil.hex2byte(offset));
			req.setContent("Check_Len", new byte[] {
					(byte)PINLen
			});
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int printRandomPIN(int PVK_Index, String PAN, int PIN_Len, byte offset[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("PrintRandomPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("PVK_Index", NumberUtil.hex2byte("" + PVK_Index));
			req.setContent("PAN", NumberUtil.hex2byte(PAN));
			req.setContent("PIN_Len", new byte[] {
					(byte)PIN_Len
			});
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(NumberUtil.hexString(msg.getFieldContent("Offset")).getBytes(), 0, offset, 0, 6);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int printPINfromPINBlock(String PPK, String ePPK, byte PFi[], String Cardnumber, PrintDataSet things2print[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("PrintPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("ePPK", NumberUtil.hex2byte(ePPK));
			req.setContent("PPK_Spec", new Key(PPK));
			req.setContent("PFi", PFi);
			req.setContent("ANB", NumberUtil.hex2byte(Cardnumber.substring(Cardnumber.length() - 13, Cardnumber.length() - 1)));
			req.setContent("PAN", NumberUtil.hex2byte(Cardnumber));
			System.out.println("*****PINDATA***");
			((PrintPIN_Request)req).addPrintData(things2print);
			System.out.println("*****PINDATA1***");
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			String abc = NumberUtil.hexString(req.pack());
			System.out.println(abc);
			conn.send(req);
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				System.out.println("DummyRes="+i);
				return i;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		System.out.println("ActualRes="+msg.getResponseCode());
		return msg.getResponseCode();
	}

	public static int GenerateMAC(byte ICD[], byte MAC[], String data, String MPK_Spec)
	{
		byte OCD[] = new byte[8];
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("MACUpdate");
		TCPIPConnection conn = null;
		for(; data.length() % 8 != 0; data = data + '\0');
		try
		{
			conn = getConnection();
			req = DefaultMessageFactory.createRequest("MACGenFinal");
			req.setContent("MACLength", new byte[] {
					(byte)MAC.length
			});
			req.setContent("ICD", ICD);
			if(MPK_Spec.length() < 16)
				req.setContent("MPK_Spec", new Key(Integer.parseInt(MPK_Spec)));
			else
				req.setContent("MPK_Spec", new Key(MPK_Spec));
			req.setContent("Data", data.getBytes());
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			conn.send(req);
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			System.arraycopy(msg.getFieldContent("MAC"), 0, MAC, 0, MAC.length);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int VerifyMAC(byte ICD[], byte MAC[], String data, String MPK_Spec)
	{
		byte OCD[] = new byte[8];
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("MACUpdate");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("ICD", ICD);
			if(MPK_Spec.length() < 16)
				req.setContent("MPK_Spec", new Key(Integer.parseInt(MPK_Spec)));
			else
				req.setContent("MPK_Spec", new Key(MPK_Spec));
			req.setContent("Data", data.getBytes());
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			OCD = msg.getFieldContent("OCD");
			req = DefaultMessageFactory.createRequest("MACGenFinal");
			req.setContent("MAClength", new byte[] {
					(byte)MAC.length
			});
			req.setContent("ICD", OCD);
			if(MPK_Spec.length() < 16)
				req.setContent("MPK_Spec", new Key(Integer.parseInt(MPK_Spec)));
			else
				req.setContent("MPK_Spec", new Key(MPK_Spec));
			req.setContent("Data", data.getBytes());
			conn.send(req);
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			msg = conn.receive();
			if(msg instanceof DummyResponse)
			{
				int j = msg.getResponseCode();
				return j;
			}
			System.arraycopy(msg.getFieldContent("MAC"), 0, MAC, 0, MAC.length);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int encryptData(String DPK_Spec, boolean cbcMode, byte icv[], byte data[], byte ocv[], byte encryptedData[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("EncryptData");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			if(cbcMode)
			{
				req.setContent("CM", CIPHER_MODE_CBC);
				req.setContent("ICV", icv);
			}
			if(DPK_Spec.length() < 16)
				req.setContent("DPK_Spec", new Key(Integer.parseInt(DPK_Spec)));
			else
				req.setContent("DPK_Spec", new Key(DPK_Spec));
			byte data8[];
			if(data.length % 8 == 0)
			{
				data8 = data;
			} else
			{
				data8 = new byte[(data.length / 8 + 1) * 8];
				System.arraycopy(data, 0, data8, 0, data.length);
			}
			req.setContent("Data", data8);
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			if(cbcMode)
				System.arraycopy(msg.getFieldContent("OCV"), 0, ocv, 0, ocv.length);
			byte tmp[] = msg.getFieldContent("eDPK");
			System.arraycopy(tmp, 0, encryptedData, 0, Math.min(tmp.length, encryptedData.length));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int decryptData(String DPK_Spec, boolean cbcMode, byte icv[], byte data[], byte ocv[], byte clearData[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("DecryptData");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			if(cbcMode)
			{
				req.setContent("CM", CIPHER_MODE_CBC);
				req.setContent("ICV", icv);
			}
			if(DPK_Spec.length() < 16)
				req.setContent("DPK_Spec", new Key(Integer.parseInt(DPK_Spec)));
			else
				req.setContent("DPK_Spec", new Key(DPK_Spec));
			byte data8[];
			if(data.length % 8 == 0)
			{
				data8 = data;
			} else
			{
				data8 = new byte[(data.length / 8 + 1) * 8];
				System.arraycopy(data, 0, data8, 0, data.length);
			}
			req.setContent("Data", data8);
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			if(cbcMode)
				System.arraycopy(msg.getFieldContent("OCV"), 0, ocv, 0, ocv.length);
			byte tmp[] = msg.getFieldContent("Data");
			System.arraycopy(tmp, 0, clearData, 0, Math.min(tmp.length, clearData.length));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int encryptClearPIN(String ppkSpec, String cardNumber, String PIN, byte encryptedPIN[])
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("EncryptClearPIN");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			req.setContent("Pin_Len", new byte[] {
					(byte)PIN.length()
			});
			if(PIN.length() % 2 != 0)
				PIN = "0" + PIN;
			req.setContent("PIN", NumberUtil.hex2byte(PIN));
			String TSP12 = cardNumber.substring(cardNumber.length() - 13, cardNumber.length() - 1);
			byte ANB[] = NumberUtil.hex2byte(TSP12);
			req.setContent("ANB", ANB);
			System.out.println("PPK="+ppkSpec);
			if(ppkSpec.length() < 16)
				req.setContent("PPK_Spec", new Key(Integer.parseInt(ppkSpec)));
			else
				req.setContent("PPK_Spec", new Key(ppkSpec));
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
			byte tmp[] = msg.getFieldContent("ePIN");
			System.arraycopy(tmp, 0, encryptedPIN, 0, encryptedPIN.length);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int generateClearRandomPIN(int PINlength, byte encryptedPIN[])
	{
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%generateClearRandomPIN%%%%%%%%%%%%%%%%%%%%%%");
		if(PINlength < 4 || PINlength > 8 || encryptedPIN.length != 8)
			return 11;
		String data;
		for(data = "" + (int)(Math.random() * Math.pow(10D, PINlength)); data.length() < PINlength; data = "0" + data);
		System.out.println("PIN DATA="+data);
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%generateClearRandomPIN%%%%%%%%%%%%%%%%%%%%%%");
		return encryptData("1111111111111111", false, null, data.getBytes(), null, encryptedPIN);
	}

	public static int buildPINBlockFromEncryptedPIN(String cardNumber, String encryptedPIN, byte PINBlock[], int PINlength,String PPK)
	{
		byte clearPIN[] = new byte[PINlength];
		decryptData("1111111111111111", false, null, NumberUtil.hex2byte(encryptedPIN), null, clearPIN);
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%generateClearRandomPIN%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("buildPINBlockFromEncryptedPIN clearPIN PIN DATA="+ new String(clearPIN) +" PPK="+PPK);
		return encryptClearPIN(PPK, cardNumber, new String(clearPIN), PINBlock);
	}

	public static int template()
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("");
		TCPIPConnection conn = null;
		try
		{
			conn = getConnection();
			conn.send(req);
			logger.info("Message sent " + NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			logger.info("Received response from host " + NumberUtil.hexString(conn.getRawData()));
			if(msg instanceof DummyResponse)
			{
				int i = msg.getResponseCode();
				return i;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte byte0 = -1;
			return byte0;
		}
		finally
		{
			pool.checkIn(conn);
		}
		return msg.getResponseCode();
	}

	public static int EMVValidation(String Action,int IMKindex,String MKMethod,String MKData,String ACKeyMethod,String ACKeyData,
			String ACMethod,String ACData,String AC,String ARPCKeyMethod,String ARPCKeyData,String ARPCMethod,String ARPCData,byte[] ARPC)
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("Gen_ARQC_ARPC_Request");
		TCPIPConnection conn = null;
		try{
			conn = getConnection();
			//Modify whatever attribute(s) of the request here before sending
			System.out.println("1 Action"+Action +"  "+ req);
			req.setContent("ACTION", NumberUtil.hex2byte(Action));
			System.out.println("2");
			req.setContent("IMK_AC", new Key(IMKindex));
			System.out.println("3");
			req.setContent("MK_METHOD", NumberUtil.hex2byte(MKMethod));
			System.out.println("4");
			req.setContent("MK_DATA", NumberUtil.hex2byte(MKData));
			System.out.println("5");
			req.setContent("AC_KEY_METHOD", NumberUtil.hex2byte(ACKeyMethod));
			System.out.println("6");
			req.setContent("AC_KEY_DATA", NumberUtil.hex2byte(ACKeyData));
			req.setContent("AC_METHOD", NumberUtil.hex2byte(ACMethod));
			System.out.println("7");
			req.setContent("AC_DATA", NumberUtil.hex2byte(ACData));
			System.out.println("8");
			req.setContent("AC", NumberUtil.hex2byte(AC));
			System.out.println("9");
			req.setContent("ARPC_KEY_METHOD", NumberUtil.hex2byte(ARPCKeyMethod));
			System.out.println("10");
			req.setContent("ARPC_KEY_DATA", NumberUtil.hex2byte(ARPCKeyData));
			req.setContent("ARPC_METHOD", NumberUtil.hex2byte(ARPCMethod));
			System.out.println("11 "+ARPCData);
			req.setContent("ARPC_DATA", NumberUtil.hex2byte(ARPCData));

			conn.send(req);
			logger.info("Message sent "+NumberUtil.hexString(req.pack()));
			msg = conn.receive();
			//CVV = new byte[2];
			logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));

			if(!Action.equals("01"))
			{

				byte ARPC1[] = msg.getFieldContent("ARPC");
				System.out.println("ARPC"+ARPC1);
				System.out.println("ARPC="+NumberUtil.hexString(ARPC1));

				System.arraycopy(msg.getFieldContent("ARPC"),0,ARPC,0,8);
			}
			System.out.println("Res Code1="+ msg.getResponseCode());
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


	public static int ImportKey(String KIR_Spec, byte[] KeyType,byte[] EncMode, String eKM_KeySpec,byte[] eKIR_K,byte[] KVC)
	{
		HSMMsg msg = new HSMMsg();
		HSMMsg req = DefaultMessageFactory.createRequest("ImportKey");
		TCPIPConnection conn = null;

		try
		{
			conn = getConnection();
			//Modify whatever attribute(s) of the request here before sending


			req.setContent("KIR_Spec",new Key(new Integer(KIR_Spec)));
			req.setContent("Key_Type",KeyType);
			System.out.println("CIPHER_MODE_CBC"+NumberUtil.hexString(CIPHER_MODE_CBC));
			req.setContent("ENC_Mode",EncMode);
			req.setContent("Key_Spec",NumberUtil.hex2byte(eKM_KeySpec));

			System.out.println(NumberUtil.hexString(req.pack()));
			conn.send(req);
			logger.info("Message sent"+NumberUtil.hexString(req.pack()));
			msg = conn.receive();

			logger.info("Received response from host "+NumberUtil.hexString(conn.getRawData()));
			if (msg instanceof DummyResponse)
				return msg.getResponseCode();
			System.arraycopy(msg.getFieldContent("KIR_Spec"),0,eKIR_K,0,eKIR_K.length);
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



	static Class class$vn$com$tivn$hsm$phw$ObjectPool; /* synthetic field */
	private static TCPIPConnectionPool pool = new TCPIPConnectionPool();
	private static Logger logger;
	public static boolean INITIALIZED;
	//Key flags
	public static final byte[]    KEY_SINGLE_LEN_DPK	= {0x00,0x01};
	public static final byte[]	KEY_SINGLE_LEN_PPK	= {0x00,0x02};
	public static final byte[]	KEY_SINGLE_LEN_MPK	= {0x00,0x04};
	public static final byte[]	KEY_DOUBLE_LEN_DPK	= {0x01,0x00};
	public static final byte[]	KEY_DOUBLE_LEN_PPK	= {0x12,0x00};
	public static final byte[]	KEY_DOUBLE_LEN_MPK	= {0x04,0x00};
	public static final byte[]	KEY_DOUBLE_LEN_KIS	= {0x08,0x00};
	public static final byte[]	KEY_DOUBLE_LEN_KTM	= {0x18,0x00};
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

	// Key Type

	public static final byte[]    DPK_KEY	    =	{0x00};
	public static final byte[]    PPK_KEY	    =	{0x01};
	public static final byte[]    MPK_KEY	    =	{0x02};
	public static final byte[]    KIS_KEY	    =	{0x03};
	public static final byte[]    KIR_KEY	    =	{0x04};
	public static final byte[]    KTM_KEY	    =	{0x05};

	public static final boolean PRINT_FROM_HSM = false;
	public static String defaultKey = "2";
	public static final boolean PHW_ISSUANCE = false;
	private static int msg_counter;

	static
	{
		logger = Logger.getLogger((class$vn$com$tivn$hsm$phw$ObjectPool != null ? class$vn$com$tivn$hsm$phw$ObjectPool : (class$vn$com$tivn$hsm$phw$ObjectPool = _mthclass$("vn.com.tivn.hsm.phw.ObjectPool"))).getName());
	}
}
