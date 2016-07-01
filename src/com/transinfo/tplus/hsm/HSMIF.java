package com.transinfo.tplus.hsm;


public interface HSMIF
{

	public int PINLEN   = 6;
	public int KEYLEN   = 16;
	public int KEYTYPE  = 1;
	public int BUFF_SIZE = 256;
	// public int PIN_FORMAT_ANSI = 1;
	//public int PIN_FORMAT_PINPAD = 3;
	public int HSM_NO_ERROR = 0;
	public int KEY_SINGLE_LEN_DPK = 0x0001;
	public int KEY_SINGLE_LEN_PPK = 0x0002;
	public int KEY_SINGLE_LEN_MPK = 0x0004;
	public int KEY_DOUBLE_LEN_DPK = 0x0100;
	public int KEY_DOUBLE_LEN_PPK = 0x0200;
	public int KEY_DOUBLE_LEN_MPK = 0x0400;

	// Cipher mode
	public static final byte[]	CIPHER_MODE_ECB	  =	{0x00};
	public static final byte[]	CIPHER_MODE_CBC	  =	{0x01};

	//PIN format
	public static final byte[]    PIN_FORMAT_ANSI	    =	{0x01};
	public static final byte[]	PIN_FORMAT_PINPAD 	=	{0x03};
	public static final byte[]	PIN_FORMAT_DOCUTEL  =	{0x08};
	public static final byte[]	PIN_FORMAT_ZKA		  =	{0x09};
	public static final byte[]	PIN_FORMAT_ISO0		  =	{0x10};
	public static final byte[]	PIN_FORMAT_ISO1		  =	{0x11};
	public static final byte[]	PIN_FORMAT_ISO3		  =	{0x13};

	// Key Type

	public static final byte[]    DPK_KEY	    =	{0x00};
	public static final byte[]    PPK_KEY	    =	{0x01};
	public static final byte[]    MPK_KEY	    =	{0x02};
	public static final byte[]    KIS_KEY	    =	{0x03};
	public static final byte[]    KIR_KEY	    =	{0x04};
	public static final byte[]    KTM_KEY	    =	{0x05};


	//  public boolean generateKey(String KTM_Spec,byte[] keyFlag, byte[] eKTM, byte[] KS_Spec, byte[] KVC)throws Exception;
	// public boolean generateTMK(String KTM_Spec, byte[] eKTM, byte[] KS_Spec, byte[] KVC)throws Exception;
	//  public boolean exportKey(String KIS_Spec, byte[] KeyType,byte[] EncMode, String eKM_KeySpec,byte[] eKIS_K,byte[] KVC)throws Exception;

	//  public boolean generateMAC(byte[] MAC,String Data,String MPK_Spec)throws Exception;
	public boolean importKey(String KIR_Spec, byte[] KeyType,byte[] EncMode, String eKM_KeySpec,byte[] eKIR_K,byte[] KVC)throws Exception;
	public int TranslatePIN(String pinBlock, String PPKi_Spec, byte[] PFi, String Cardnumber, byte[] PFo, String PPKo_Spec, byte[] newPinBlock)throws Exception;
	public int verifyPIN_PVV(String cardProductId,String strIssuerId,String cardNumber,String dbPVV,String pinBlock,String strAcqBin)throws Exception;
	public int verifyCVV(String cardProductId,String strIssuerId,String cardNumber,String expDate,String serviceCode,String cvv,String strAcqBin)throws Exception;
	public int activatePIN(String cardProductId,String strIssuerId,String Cardnumber, String encryptedPIN,String strAcqBin)throws Exception;
	public int changePIN(String cardProductId,String strIssuerId,String Cardnumber, String encryptedPIN,String strAcqBin)throws Exception;
	public int EMVValidation(String Action,int IMKindex,String MKMethod,String MKData,String ACKeyMethod,String ACKeyData,String ACMethod,String ACData,String AC,String ARPCKeyMethod,String ARPCKeyData,String ARPCMethod,String ARPCData,byte[] ARPC)throws Exception;
	public int getEMVKey(String strPANData,String strATC,byte[] EMVKey) throws Exception;
	public int getJCBEMVKey(String strPANData,String strATC,byte[] EMVKey) throws Exception;
	//  public boolean validateARQC(String strARQC,String strARQCSrc,String strKey)throws Exception;
	//  public String generateARPC(String strARQC ,String strResCode,String sKey)throws Exception;






}