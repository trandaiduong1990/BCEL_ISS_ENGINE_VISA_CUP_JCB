package vn.com.tivn.hsm.phw;

public class DefaultMessageFactory implements MessageFactory
{
  public DefaultMessageFactory()
  {
  }

  public HSMMsg createResponseMessage(byte[] header, byte[] data)
  {
    if (data[0] == (byte)0x01)
    {
      return new HSM_Status_Response();
    }
    if (data[0] == (byte)0x9C)
    {
      return new CVV_Verify_Response();
    }
    if (data[0] == (byte)0x9B)
    {
      return new CVV_Generate_Response();
    }
    if (data[0] == (byte)0xE2)
    {
      return new PIN_Mail_Response();
    }
    if (data[0] == (byte)0x41)
    {
      return new IT_PPK_GEN_Response();
    }
    if (data[0] == (byte)0x61)
    {
      return new IBM3624_PIN_Ver_Response();
    }

    String st = NumberUtil.hexString(data);
    System.out.println("Data "+st);
    if(st.substring(0,6).equals("EE0200"))
    {
      return new Key_Import_Response();
      //return new BNV_Key_Rcv_Response();
    }

    if(st.substring(0,6).equals("EE0201"))
    {
      return new Key_Export_Response();
      //return new BNV_Key_Rcv_Response();
    }

    if(st.substring(0,6).equals("EE0403"))
    {
      return new II_Key_Rcv_Response();
      //return new BNV_Key_Rcv_Response();
    }
    if(st.substring(0,6).equals("EE0400"))
    {
      return new IT_Key_Gen_Response();
    }
    if(st.substring(0,6).equals("EE0402"))
    {
      return new II_Key_Gen_Response();
    }
    if(st.substring(0,6).equals("EE0803"))
    {
      return new CVV_Verify_I_Response();
    }
    if(st.substring(0,6).equals("EE0603"))
    {
      return new Verify_Random_PIN_Response();
    }
    if(st.substring(0,6).equals("EE0700"))
    {
      return new MAC_Update_Response();
    }
    if(st.substring(0,6).equals("EE0700"))
    {
      return new MAC_Update_Response();
    }
    if(st.substring(0,6).equals("EE0701"))
    {
      return new MAC_Gen_Final_Response();
    }
    if(st.substring(0,6).equals("EE0702"))
    {
      return new MAC_Ver_Final_Response();
    }
    if(st.substring(0,6).equals("EE0606"))
    {
      return new CalculatePVVfromOffset_Response();
    }
    if(st.substring(0,6).equals("EE0E04"))
    {
      return new GenerateRandomPIN_Response();
    }
    if(st.substring(0,6).equals("EE0E05"))
    {
      return new PrintPIN_Response();
    }
    if(st.substring(0,6).equals("EE0604"))
    {
      return new CalculateOffsetfromPIN_Response();
    }
    if(st.substring(0,6).equals("EE0607"))
    {
      return new CalculatePVVfromPIN_Response();
    }
    if(st.substring(0,6).equals("EE0602"))
    {
      return new PIN_Translate_Response();
    }

    if(st.substring(0,6).equals("EE0800"))
    {
      return new EncryptData_Response();
    }
    if(st.substring(0,6).equals("EE0801"))
    {
      return new DecryptData_Response();
    }

    if(st.substring(0,6).equals("EE0600"))
    {
      return new Clr_PIN_Encrypt_Response();
    }

	if (st.substring(0,6).equals("EE2018"))
	{
		if(st.length() == 8)
		{
      		return new Gen_ARQC_Validation_Response();
		}
		else
		{
			return new Gen_ARPC_Response();
		}
    }
    return null;
  }

  public static HSMMsg createRequest(String msgtype)
  {
    if ((msgtype.equals("HSMStatus"))||(msgtype.equals("01")))
      return new HSM_Status_Request();

    if ((msgtype.equals("VerifyCVV"))||(msgtype.equals("9C")))
      return new CVV_Verify_Request();

    if ((msgtype.equals("VerifyCVV_I"))||(msgtype.equals("EE0803")))
      return new CVV_Verify_I_Request();

    if ((msgtype.equals("GenerateCVV"))||(msgtype.equals("9B")))
      return new CVV_Generate_Request();

    if ((msgtype.equals("GeneratePVVfromPIN"))||(msgtype.equals("EE0607")))
      return new CalculatePVVfromPIN_Request();

    if ((msgtype.equals("VerifyPIN"))||(msgtype.equals("EE0605")))
      return new VerifyPIN_PVV_Request();

    if ((msgtype.equals("SessionKey"))||(msgtype.equals("EE0403")))
      return new II_Key_Rcv_Request();

	if ((msgtype.equals("ExportKey"))||(msgtype.equals("EE0201")))
      return new Key_Export_Request();

	if ((msgtype.equals("ImportKey"))||(msgtype.equals("EE0200")))
      return new Key_Import_Request();

    if (msgtype.equals("GenerateKey"))
    {
      System.out.println(System.getProperty("PHW_Type"));
      if (EracomPHW.PHW_ISSUANCE)
       // return new II_Key_Gen_Request();
        return new IT_Key_Gen_Request();
      else
        return new IT_Key_Gen_Request();
        //return new II_Key_Gen_Request();
    }

    if ((msgtype.equals("PrintRandomPIN"))||(msgtype.equals("E2")))
      return new PIN_Mail_Request();

    if ((msgtype.equals("GenerateRandomPIN"))||(msgtype.equals("EE0E04")))
      return new GenerateRandomPIN_Request();

    if ((msgtype.equals("PrintPIN"))||(msgtype.equals("EE0E05")))
      return new PrintPIN_Request();

    if ((msgtype.equals("VerifyRandomPIN"))||(msgtype.equals("61")))
      return new Verify_Random_PIN_Request();
      //return new IBM3624_PIN_Ver_Request();

    if ((msgtype.equals("TranslatePIN"))||(msgtype.equals("EE0602")))
      return new PIN_Translate_Request();

    if ((msgtype.equals("MACUpdate"))||(msgtype.equals("EE0700")))
    {
		System.out.println("MACUpdate");
    	return new MAC_Update_Request();
	}

    if ((msgtype.equals("MACGenFinal"))||(msgtype.equals("EE0701")))
    {
		System.out.println("MACFinal");
    	return new MAC_Gen_Final_Request();
	}

    if ((msgtype.equals("MACVerFinal"))||(msgtype.equals("EE0702")))
      return new MAC_Ver_Final_Request();

    if ((msgtype.equals("CalculatePVVfromOffset"))||(msgtype.equals("EE0606")))
      return new CalculatePVVfromOffset_Request();

    if ((msgtype.equals("CalculateOffsetfromPIN"))||(msgtype.equals("EE0604")))
      return new CalculateOffsetfromPIN_Request();

    if ((msgtype.equals("GenerateInitialPPK"))||(msgtype.equals("41")))
      return new IT_PPK_GEN_Request();

    if ((msgtype.equals("EncryptData"))||(msgtype.equals("EE0800")))
      return new EncryptData_Request();
    if ((msgtype.equals("DecryptData"))||(msgtype.equals("EE0801")))
      return new DecryptData_Request();

    if ((msgtype.equals("EncryptClearPIN"))||(msgtype.equals("EE0600")))
      return new Clr_PIN_Encrypt_Request();
	if ((msgtype.equals("Gen_ARQC_ARPC_Request"))||(msgtype.equals("EF2018")))
      return new Gen_ARQC_ARPC_Request();

    return null;
  }
}
