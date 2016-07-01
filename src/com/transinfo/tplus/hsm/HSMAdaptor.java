package com.transinfo.tplus.hsm;

import java.util.Map;

import vn.com.tivn.hsm.phw.EracomPHW;
import vn.com.tivn.hsm.phw.NumberUtil;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusConfig;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.TPlusUtility;
import com.transinfo.tplus.db.TransactionDB;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.messaging.OnlineUtil;

public class HSMAdaptor implements HSMIF {


	public HSMAdaptor()
	{


		/*System.out.println("HSM Host and Port"+TPlusConfig.getHSMIP()+"    "+TPlusConfig.getHSMPort()+"    "+TPlusConfig.strCUPKeyIndex);
		if (DebugWriter.boolDebugEnabled) DebugWriter.write(" BEFORE HSM INITIALING");
		EracomPHW.init("192.168.6.95",1000,5,5000);
		System.out.println("11");
		if (DebugWriter.boolDebugEnabled) DebugWriter.write(" AFTER HSM INITIALING");
		System.out.println("BEFORE HSM INITIALING");
		EracomPHW.init(TPlusConfig.getHSMIP(),Integer.parseInt(TPlusConfig.getHSMPort()),5,5000);
		System.out.println("21");*/
		//EracomPHW.init("192.168.6.95",1000,5,5000);
		EracomPHW.init(TPlusConfig.getHSMIP(),Integer.parseInt(TPlusConfig.getHSMPort()),5,5000);

	}


	public boolean generateTMK(String KTM_Spec, byte[] eKTM, byte[] KS_Spec, byte[] KVC)throws Exception {

		boolean resStatus=false;

		try
		{
			System.out.println("************************ GENERATE KTM **********************");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("************************ GENERATE KTM **********************");

			EracomPHW.GenerateTerminalKey(KTM_Spec,eKTM,KS_Spec,KVC);
			System.out.println(NumberUtil.hexString(eKTM));
			System.out.println("Key to store: "+NumberUtil.hexString(KS_Spec));
			System.out.println("KVC : "+NumberUtil.hexString(KVC));

			resStatus = true;

		}catch(Exception exp)
		{
			System.out.println(" KTM Generation EXCEPTION="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor KTM Generation EXCEPTION="+exp);
			//throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: at PINVERIFY:"+exp.getMessage());
		}

		return resStatus;

	}

	public boolean generateMAC(byte[] MAC,String MACData,String MPK_Spec)throws Exception
	{

		boolean resStatus=false;

		try
		{

			System.out.println("************************ MACING **********************");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("************************ MACING **********************");
			System.out.println("Data="+MACData);

			EracomPHW.GenerateMAC(NumberUtil.hex2byte("0000000000000000"),MAC,MACData,MPK_Spec);
			System.out.println(NumberUtil.hexString(MAC));

			resStatus = true;

		}catch(Exception exp)
		{
			System.out.println(" Import Key Exception="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(" Macing Key Exception="+exp);

		}

		return resStatus;

	}


	public int TranslatePIN(String pinBlock, String PPKi_Spec, byte[] PFi, String Cardnumber, byte[] PFo, String PPKo_Spec, byte[] newPinBlock)throws Exception
	{


		int resStatus=-1;

		try
		{

			System.out.println("************************ PIN TRANSLATE **********************");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("************************ PIN TRANSLATE **********************");


			EracomPHW.TranslatePIN(pinBlock, PPKi_Spec, PFi, Cardnumber,  PFo,  PPKo_Spec, newPinBlock);

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PINBLOCK AFTER PIN TRANSLATE "+ NumberUtil.hexString(newPinBlock));

			System.out.println(NumberUtil.hexString(newPinBlock));

			resStatus = 0;

		}catch(Exception exp)
		{
			System.out.println(" Import Key Exception="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(" PIN Translate Key Exception="+exp);

		}

		return resStatus;

	}


	public int verifyPIN_PVV(String cardTypeId,String strIssuerId,String cardNumber,String dbPVV, String encryptedPINBlock,String strTranxChannel)throws Exception {

		int res=-1;

		try
		{
			System.out.println("*********************************** VERIFY PIN **********************");
			System.out.println("1");
			TransactionDB objTranx = new TransactionDB();

			System.out.println("1");
			Map keyData=null;

			keyData = objTranx.getKeyIndex(cardTypeId,strTranxChannel);


			System.out.println("2");
			//String strOffset = objTranx.getPVV(cardNumber);
			//System.out.println("PVV"+strOffset);
			System.out.println("PPK... :: "+(String)keyData.get("PPK"));
			System.out.println("PVK... :: "+(String)keyData.get("PVK"));

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PPK... :: "+(String)keyData.get("PPK"));
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("PVK... :: "+(String)keyData.get("PVK"));

			byte[] offset = new byte [6];

			res = EracomPHW.CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI,cardNumber,(String)keyData.get("PPK"),new Integer((String)keyData.get("PVK")).intValue(),offset,encryptedPINBlock);
			if(!NumberUtil.hexString(offset).equals("") && !NumberUtil.hexString(offset).equals("000000000000") )
			{
				dbPVV = ("000000000000"+dbPVV).substring(dbPVV.length());

				System.out.println("OLD and NEW PIN OFFSET"+NumberUtil.hexString(offset)+"   "+dbPVV);
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("OLD and NEW PIN OFFSET "+NumberUtil.hexString(offset)+"   "+dbPVV);
				if(NumberUtil.hexString(offset).equals(dbPVV))
				{
					System.out.println("PIN Valid");
					res=0;
				}
				else
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: PIN VERIFICATION FAILED for "+cardNumber);
					res=-1;
				}
			}
			else
			{
				throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: No PVV Generated for the card "+cardNumber);
			}

		}catch(Exception exp)
		{
			System.out.println(" PIN VERIFICATION EXCEPTION="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: PIN VERIFICATION EXCEPTION="+exp);
			//throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: at PINVERIFY:"+exp.getMessage());
		}

		return res;



	}

	public int verifyCVV(String cardTypeId,String strIssuerId,String cardNumber, String expDate ,String serviceCode,String cvv,String strAcqBin)throws Exception {

		int res=-1;

		try
		{
			System.out.println("*********************************** VERIFY CVV **********************");
			System.out.println("1");
			TransactionDB objTranx = new TransactionDB();


			System.out.println("1");
			Map keyData=null;
			//keyData = objTranx.getKeyIndex(cardTypeId,strAcqBin);

			// modified by Nishandan on 05-10-2013
			// we will not have CVK OFFUS record
			keyData = objTranx.getKeyIndex(cardTypeId,"ONUS");
			// end

			/*if(strAcqBin.equals("ONUS"))
			{
            	 keyData = objTranx.getKeyIndex(cardTypeId,"ONUS");
			}
            else
            {
         	    keyData = objTranx.getKeyIndex(cardTypeId,"CUP");
		 	}*/

			System.out.println("2");

			res = EracomPHW.verifyCVV(new Integer((String)keyData.get("CVK")).intValue(),cardNumber,expDate,serviceCode,cvv);

			if (res ==0)
			{
				System.out.println("CVV Valid");
				res=0;
			}
			else
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: CVV VERIFICATION FAILED for "+cardNumber);
				res=-1;
			}

		}catch(Exception exp)
		{
			System.out.println(" CVV VERIFICATION EXCEPTION="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: CVV VERIFICATION EXCEPTION="+exp);
			//throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: at PINVERIFY:"+exp.getMessage());
		}

		return res;
	}

	public int activatePIN(String cardTypeId,String strIssuerId,String cardNumber, String encryptedPINBlock,String strAcqBin)throws Exception {

		int res=-1;

		try {

			System.out.println("*********************************** ACTIVATE PIN **********************");

			TransactionDB objTranx = new TransactionDB();


			Map keyData = objTranx.getKeyIndex(cardTypeId,strAcqBin);
			if(keyData !=null)
			{
				String strPVV = objTranx.getPVV(cardNumber);
				System.out.println("PVV"+strPVV);
				System.out.println("PPK"+(String)keyData.get("PPK"));
				System.out.println("PVK"+(String)keyData.get("PVK"));

				byte[] offset = new byte [6];
				//res = EracomPHW.generatePVV(new Integer((String)keyData.get("PVK")).intValue(), cardNumber,	(String)keyData.get("PPK"),EracomPHW.PIN_FORMAT_ANSI,encryptedPINBlock,PINLEN, PVV );

				System.out.println("Sending for offset");
				res = EracomPHW.CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI,cardNumber,(String)keyData.get("PPK"),new Integer((String)keyData.get("PVK")).intValue(),offset,encryptedPINBlock);
				System.out.println("After offset"+NumberUtil.hexString(offset));

				if(!NumberUtil.hexString(offset).equals("") && !NumberUtil.hexString(offset).equals("000000000000") )
				{
					System.out.println("offset value="+NumberUtil.hexString(offset));
					objTranx.updatePINStatus(NumberUtil.hexString(offset),strPVV,new Long(cardNumber).longValue());
				}
				else
				{
					throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: No PVV Generated for the card "+cardNumber);
				}

			}
			else
			{
				throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: No Key Data for this Issuer "+strIssuerId);
			}

		}catch(Exception exp) {
			System.out.println(" PIN ACTIVATION EXCEPTION="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: PIN ACTIVATION EXCEPTION="+exp);
			res=-1;
			//throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: at PINVERIFY:"+exp.getMessage());
		}

		return res;
	}


	public int changePIN(String cardTypeId,String strIssuerId,String cardNumber, String encryptedPINBlock,String strAcqBin)throws Exception {

		int res=-1;

		try
		{
			System.out.println("*********************************** CHANGE PIN **********************");

			TransactionDB objTranx = new TransactionDB();

			/*if(encryptedPINBlock != null)
			{
				objTranx.updatePINStatus(encryptedPINBlock,new Long(cardNumber).longValue());
			}
			else
			{
				throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: No PVV Generated for the card "+cardNumber);
			}*/

			Map keyData = objTranx.getKeyIndex(cardTypeId,strAcqBin);
			if(keyData !=null) {
				String strPVV = objTranx.getPVV(cardNumber);
				System.out.println("PVV"+strPVV);
				System.out.println("PPK"+(String)keyData.get("PPK"));
				System.out.println("PVK"+(String)keyData.get("PVK"));
				System.out.println("CardNo"+cardNumber);
				System.out.println("F47"+encryptedPINBlock);
				System.out.println("AcqBin"+strAcqBin);				byte[] offset = new byte [6];
				//res = EracomPHW.generatePVV(new Integer((String)keyData.get("PVK")).intValue(), cardNumber,	(String)keyData.get("PPK"),EracomPHW.PIN_FORMAT_ANSI,encryptedPINBlock,PINLEN, PVV );
				res = EracomPHW.CalculateOffsetfromPIN(EracomPHW.PIN_FORMAT_ANSI,cardNumber,(String)keyData.get("PPK"),new Integer((String)keyData.get("PVK")).intValue(),offset,encryptedPINBlock);
				if(!NumberUtil.hexString(offset).equals("")  && !NumberUtil.hexString(offset).equals("000000000000")) {
					objTranx.updatePINStatus(NumberUtil.hexString(offset),strPVV,new Long(cardNumber).longValue());
				}
				else {
					throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: No PVV Generated for the card "+cardNumber);
				}

			}
			else {
				throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: No Key Data for this Issuer "+strIssuerId);
			}

		}catch(Exception exp) {
			System.out.println(" CHANGE PIN EXCEPTION="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: CHANGE PIN EXCEPTION="+exp);
			res=-1;
			//throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: at PINVERIFY:"+exp.getMessage());
		}

		return res;

	}


	public int EMVValidation(String Action,int IMKindex,String MKMethod,String MKData,String ACKeyMethod,String ACKeyData,String ACMethod,String ACData,String AC,String ARPCKeyMethod,String ARPCKeyData,String ARPCMethod,String ARPCData,byte[] ARPC)throws Exception
	{

		int resStatus=-1;

		try
		{

			System.out.println("************************ ARQC & ARPC Validation Generation **********************");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("************************ ARQC & ARPC Validation Generation **********************");


			resStatus = EracomPHW.EMVValidation(Action,IMKindex,MKMethod,MKData,ACKeyMethod,ACKeyData,ACMethod,ACData,AC,ARPCKeyMethod,ARPCKeyData,ARPCMethod,ARPCData,ARPC);

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("ARQC & ARPC Validation Generation "+ NumberUtil.hexString(ARPC));

			System.out.println(NumberUtil.hexString(ARPC));

			//resStatus = 0;

		}catch(Exception exp)
		{
			System.out.println(" ARQC & ARPC Validation Generation="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(" ARQC & ARPC Validation Generation="+exp);

		}

		return resStatus;

	}



	public int verifyCVV_I(String cardTypeId,String strIssuerId,String cardNumber, String expDate ,String serviceCode,String cvv,String strAcqBin)throws Exception {

		int res=-1;

		try
		{
			System.out.println("*********************************** VERIFY iCVV **********************");
			System.out.println("1");
			TransactionDB objTranx = new TransactionDB();


			System.out.println("1");
			Map keyData=null;
			//keyData = objTranx.getKeyIndex(cardTypeId,strAcqBin);

			// modified by Nishandan on 05-10-2013
			// we will not have CVK OFFUS record
			keyData = objTranx.getKeyIndex(cardTypeId,"ONUS");
			// end

			/*if(strAcqBin.equals("ONUS"))
			{
            	 keyData = objTranx.getKeyIndex(cardTypeId,"ONUS");
			}
            else
            {
         	    keyData = objTranx.getKeyIndex(cardTypeId,"CUP");
		 	}*/

			System.out.println("2");

			res = EracomPHW.verifyCVV(new Integer((String)keyData.get("CVK")).intValue(),cardNumber,expDate,serviceCode,cvv);

			if (res ==0)
			{
				System.out.println("CVV Valid");
				res=0;
			}
			else
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: CVV VERIFICATION FAILED for "+cardNumber);
				res=-1;
			}

		}catch(Exception exp)
		{
			System.out.println(" CVV VERIFICATION EXCEPTION="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: CVV VERIFICATION EXCEPTION="+exp);
			//throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: at PINVERIFY:"+exp.getMessage());
		}

		System.out.println("CVV Res Send");
		return res;

	}

	public int verifyCVV2(String cardTypeId,String strIssuerId,String cardNumber, String expDate ,String serviceCode,String cvv,String strAcqBin, String oc)throws Exception {

		int res=-1;

		try
		{
			System.out.println("*********************************** VERIFY iCVV **********************");
			System.out.println("1");
			TransactionDB objTranx = new TransactionDB();


			System.out.println("1");
			Map keyData=null;
			//keyData = objTranx.getKeyIndex(cardTypeId,strAcqBin);

			// modified by Nishandan on 05-10-2013
			// we will not have CVK OFFUS record
			keyData = objTranx.getKeyIndex(cardTypeId,"ONUS");
			// end

			/*if(strAcqBin.equals("ONUS"))
			{
            	 keyData = objTranx.getKeyIndex(cardTypeId,"ONUS");
			}
            else
            {
         	    keyData = objTranx.getKeyIndex(cardTypeId,"CUP");
		 	}*/

			int cv2k = new Integer((String)keyData.get("CV2K")).intValue();

			if("3".equals(oc)){
				cv2k = 1;
				expDate = expDate.substring(2,4)+expDate.substring(0,2);
			}

			System.out.println("2");

			res = EracomPHW.verifyCVV(cv2k,cardNumber,expDate,serviceCode,cvv);

			if (res ==0)
			{
				System.out.println("CVV2 Valid");
				res=0;
			}
			else
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: CVV2 VERIFICATION FAILED for "+cardNumber);
				res=-1;
			}

		}catch(Exception exp)
		{
			System.out.println(" CVV2 VERIFICATION EXCEPTION="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("HSMAdaptor: CVV2 VERIFICATION EXCEPTION="+exp);
			//throw new TPlusException(TPlusCodes.APPL_ERR,TPlusCodes.getErrorDesc(TPlusCodes.APPL_ERR)+". Error: at PINVERIFY:"+exp.getMessage());
		}

		return res;

	}

	public int getEMVKey(String strPANData, String strATC, byte[] EMVKey)throws Exception
	{


		int resStatus=-1;
		byte[] encData1 = new byte[8];
		byte[] encData2 = new byte[8];
		byte[] OCV = new byte[4];

		try
		{

			System.out.println("************************ EMV Key **********************");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("************************ EMV Key **********************");

			EracomPHW.encryptData(TPlusConfig.getCUPDPKIndex(),false,NumberUtil.hex2byte("0000000000000000"),NumberUtil.hex2byte(strPANData),OCV, encData1);

			String strKey1 = TPlusUtility.isOddParity(encData1);

			System.out.println("encData1="+NumberUtil.hexString(encData1));

			EracomPHW.encryptData(TPlusConfig.getCUPDPKIndex(),false,NumberUtil.hex2byte("0000000000000000"),NumberUtil.hex2byte(TPlusUtility.strXor(strPANData,"FFFFFFFFFFFFFFFF")),OCV, encData2);

			String strKey2 = TPlusUtility.isOddParity(encData2);

			System.out.println("encData2="+NumberUtil.hexString(encData1)+"   "+NumberUtil.hexString(encData2));

			System.out.println("key ATC="+strKey1+strKey2+strKey1+"  "+strATC+"  "+TPlusUtility.lpad(strATC,"0",16));

			byte De1[] = OnlineUtil.encrypt3DES(NumberUtil.hex2byte(strKey1+strKey2+strKey1),NumberUtil.hex2byte(TPlusUtility.lpad(strATC,"0",16)));

			String key1 = TPlusUtility.isOddParity(De1);

			System.out.println("key1= "+key1);

			byte De2[] = OnlineUtil.encrypt3DES(NumberUtil.hex2byte(strKey1+strKey2+strKey1),NumberUtil.hex2byte("000000000000"+TPlusUtility.strXor(strATC,"FFFF")));

			String key2 = TPlusUtility.isOddParity(De2);

			System.out.println("EMVKey= "+key1+key2);
			byte[] skey = NumberUtil.hex2byte(key1+key2);

			System.arraycopy(skey, 0, EMVKey, 0, skey.length);

			resStatus = 0;

		}catch(Exception exp)
		{
			System.out.println(" Import Key Exception="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(" PIN Translate Key Exception="+exp);

		}

		return resStatus;

	}


	public int getJCBEMVKey(String strPANData, String strATC, byte[] EMVKey)throws Exception
	{


		int resStatus=-1;
		byte[] encData1 = new byte[8];
		byte[] encData2 = new byte[8];
		byte[] OCV = new byte[4];

		try
		{

			System.out.println("************************ EMV Key **********************");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("************************ EMV Key **********************");

			EracomPHW.encryptData(TPlusConfig.getJCBDPKIndex(),false,NumberUtil.hex2byte("0000000000000000"),NumberUtil.hex2byte(strPANData),OCV, encData1);

			String strKey1 = TPlusUtility.isOddParity(encData1);

			System.out.println("encData1="+NumberUtil.hexString(encData1)+"  "+strKey1);

			EracomPHW.encryptData(TPlusConfig.getJCBDPKIndex(),false,NumberUtil.hex2byte("0000000000000000"),NumberUtil.hex2byte(TPlusUtility.strXor(strPANData,"FFFFFFFFFFFFFFFF")),OCV, encData2);

			String strKey2 = TPlusUtility.isOddParity(encData2);

			byte[] skey = NumberUtil.hex2byte(strKey1+strKey2);

			System.arraycopy(skey, 0, EMVKey, 0, skey.length);

			resStatus = 0;

		}catch(Exception exp)
		{
			System.out.println(" Import Key Exception="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(" PIN Translate Key Exception="+exp);

		}

		return resStatus;

	}



	public boolean importKey(String KIR_Spec, byte[] KeyType,byte[] EncMode, String eKM_KeySpec,byte[] eKIR_K,byte[] KVC)throws Exception
	{

		boolean resStatus=false;
		try
		{

			System.out.println("************************ IMPORT KEY **********************");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("************************ IMPORT KEY **********************");

			EracomPHW.ImportKey(KIR_Spec,KeyType,EncMode,eKM_KeySpec,eKIR_K,KVC);

			if (DebugWriter.boolDebugEnabled) DebugWriter.write(" AFTER IMPORT KEY KVC"+NumberUtil.hexString(KVC));
			System.out.println("Key to store: "+NumberUtil.hexString(eKIR_K));
			System.out.println("KVC : "+NumberUtil.hexString(KVC));
			resStatus = true;

		}catch(Exception exp)
		{
			System.out.println(" Import Key Exception="+exp);
			if (DebugWriter.boolDebugEnabled) DebugWriter.write(" Import Key Exception="+exp);

		}

		return resStatus;

	}


}