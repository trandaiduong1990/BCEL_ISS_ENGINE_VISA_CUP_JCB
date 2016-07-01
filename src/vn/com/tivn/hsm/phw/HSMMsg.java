package vn.com.tivn.hsm.phw;
import java.util.Enumeration;
import java.util.Hashtable;

public class HSMMsg  {
  private static int currentSequenceNumber = 0;

  private int sequenceNumber;
  protected byte[] FunctionCode = new byte[0];
  //All fields are stored in this special hashtable as a queue (FIFO)
  //All classes that extends HSMMsg should initialize the size of the Hashtable(not compulsory)
  protected TIHashtable fields = new TIHashtable();
  public static final String[] eftApiErrors =
  {
    "EFT_NO_ERROR",							//  0
    "EFT_DES_FAULT",							//  1
    "EFT_PM_NOT_ENABLED",						//  2
    "EFT_INCORRECT_MSG_LEN",					//  3
    "EFT_INVALID_DATA",						//  4
    "EFT_INVALID_KEY_INDEX",					//  5
    "EFT_INVALID_PIN_FMT_SPEC",				//  6
    "EFT_PIN_FORMAT_ERROR",					//  7
    "EFT_VERIFICATION_FAILED",					//  8
    "EFT_TAMPERED",							//  9
    "EFT_UNITIALISED_KEY",					// 10 (0x0A)
    "EFT_PIN_CHECK_LEN_ERROR",				// 11 (0x0B)
    "EFT_INCONSISTENT_REQ_FIELDS",			// 12 (0x0C)
    "EFT_INVALID_VISA_PIN_VER_KEY_IND = 0x0f",// 15 (0x0F)
    "EFT_INTERNAL__ERROR",					// 16 (0x10)
    "EFT_NO_ERRORLOG",						// 17 (0x11)
    "EFT_ERRORLOG_INTERNAL_ERR",				// 18 (0x12)
    "EFT_ERRORLOG_REQ_LEN_INVALID",			// 19 (0x13)
    "EFT_ERRORLOG_FILENO_INVALID",			// 20 (0x14)
    "EFT_ERRORLOG_INDEX_INVALID",				// 21 (0x15)
    "EFT_ERRORLOG_DATE_TIME_INVALID",			// 22 (0x16)
    "EFT_ERRORLOG_BEFORE_AFTER_FLG_INVALID",	// 23 (0x17)
    "EFT_INVALID_KEY_SPECIFIER_LEN = 0x20",   // 32 (0x20)
    "EFT_UNSUPPORTED_KEY_SPECIFIER",			// 33 (0x21)
    "EFT_INVALID_KEY_SPECIFIER_CONTENT",		// 34 (0x22)
    "EFT_INVALID_KEY_SPECIFIER_FORMAT",		// 35 (0x23)
    "EFT_INVALID_FUNCTION_MODIFIER",			// 36 (0x24)
    "EFT_PARAM_ERROR",						// 37 (0x25)
    "EFT_MEMORY_ERROR",						// 38 (0x26)
    "EFT_CL_ERROR",							// 39 (0x27)
    "EFT_INTERNAL_ERROR",						// 40 (0x28)
    "EFT_OUTPUT_VAR_FIELD_LENGTH_TOO_LONG"
  };

  private static int nextSequenceNumber(){
    return currentSequenceNumber++;
  }

  public HSMMsg() {
    sequenceNumber = nextSequenceNumber();
  }
  public HSMMsg(int seqNumber) {
    sequenceNumber = seqNumber;
  }

  public byte[] pack()
  {
    int totalLength = CalculateMessageLength();
    System.out.println("totallength="+totalLength);
    totalLength+=FunctionCode.length;
    int accumulateLength = 0;
    byte [] b = new byte[totalLength];
    System.arraycopy(FunctionCode,0,b,0,FunctionCode.length);
    accumulateLength+=FunctionCode.length;
    Enumeration enmrt = fields.elements();


    while((enmrt.hasMoreElements())&&(accumulateLength<=totalLength))
    {
      Object temp = enmrt.nextElement();
      if (temp instanceof FixedLengthField)
      {
        FixedLengthField f = (FixedLengthField)temp;
        System.out.println("FL content="+NumberUtil.hexString(f.pack()));
        System.arraycopy(f.pack(),0,b,accumulateLength,f.getLength());
        accumulateLength+=f.getLength();
      }
      if (temp instanceof VariableLengthField)
      {
        VariableLengthField vf = (VariableLengthField)temp;
        System.out.println("VL content="+NumberUtil.hexString(vf.pack()));
        System.arraycopy(vf.pack(),0,b,accumulateLength,vf.getTotalLength());
        accumulateLength+=vf.getTotalLength();
      }
      if (temp instanceof KeySpecField)
	  {
	          KeySpecField vf = (KeySpecField)temp;
	          System.out.println("VL content="+NumberUtil.hexString(vf.pack()));
	          System.arraycopy(vf.pack(),0,b,accumulateLength,vf.getTotalLength());
	          accumulateLength+=vf.getTotalLength();
      }
    }
    return b;
  }

  public void unpack(byte[] input) throws HSMException
  {
    byte[] data;
    int index = 0;
    if (FunctionCode!=null)
    {
     data = new byte[input.length-FunctionCode.length];
     index = FunctionCode.length;
    }
    else
      data = new byte[input.length];
    System.arraycopy(input,index,data,0,input.length-index);
    Enumeration enmrt = fields.elements();
    index = 0;
    while(enmrt.hasMoreElements())
    {
      Object temp = enmrt.nextElement();
      if (temp instanceof FixedLengthField)
      {
        FixedLengthField f = (FixedLengthField)temp;
        int fieldlength = f.getLength();
        byte[] value = new byte[fieldlength];
        System.arraycopy(data,index,value,0,fieldlength);
        f.setContent(value);
        index+=fieldlength;
      }
      if (temp instanceof VariableLengthField)
      {
        VariableLengthField vf = (VariableLengthField)temp;
        byte[] value = new byte[data.length-index];

        System.arraycopy(data,index,value,0,data.length-index);
        try
        {
          vf.unpack(value);
        }catch(Exception e)
        {
          e.printStackTrace();
        }
        index+=vf.getTotalLength();
      }
      if (temp instanceof KeySpecField)
	        {
	          KeySpecField vf = (KeySpecField)temp;
	          byte[] value = new byte[data.length-index];

	          System.arraycopy(data,index,value,0,data.length-index);
	          try
	          {
	            vf.unpack(value);
	          }catch(Exception e)
	          {
	            e.printStackTrace();
	          }
	          index+=vf.getTotalLength();
      }
      if((getFieldContent("RC")!=null)&&((int)getFieldContent("RC")[0]!=0))
      {
        throw new HSMException(eftApiErrors[(int)getFieldContent("RC")[0]]);
      }
    }
  }

  //This function can be used to unpack a set of fields that occurs multiple times
  //Each set of fields is treated as a HSMMsg without a function code
  //n is the number of times to unpack this set, if set to 0 it will unpack until the end
  //of input
  public void unpack(byte[] input, HSMMsg msgTemplate, int n) throws HSMException
  {
    int index = 0;
    int counter = 0;
    int remainingLength = input.length;
    //byte[] data = new byte[remainingLength];

    while ((remainingLength > 0)&&((counter<n)||(counter==0)))
    {
      byte[] data = new byte[remainingLength];
      System.arraycopy(input,index,data,0,remainingLength);
      msgTemplate.unpack(data);
      fields.putAll(msgTemplate.getFieldList());
      remainingLength-=msgTemplate.CalculateMessageLength();
      index+=msgTemplate.CalculateMessageLength();
      counter++;
    }
  }

  public void addField (String fieldname, Object field)
  {
    if (field instanceof FixedLengthField)
    {
      FixedLengthField f = (FixedLengthField)field;
      fields.put(fieldname,f);
    }
    if (field instanceof VariableLengthField)
    {
      VariableLengthField vf = (VariableLengthField)field;
      fields.put(fieldname,vf);
    }
	if (field instanceof KeySpecField)
	{
	  KeySpecField vf = (KeySpecField)field;
	  fields.put(fieldname,vf);
    }

  }

  public void addField (String fieldname, Object field, byte[] content)
  {
    if (field instanceof FixedLengthField)
    {
      FixedLengthField f = (FixedLengthField)field;
      f.setContent(content);
      fields.append(fieldname,f);
    }
    if (field instanceof VariableLengthField)
    {
      VariableLengthField vf = (VariableLengthField)field;
      vf.setContent(content);
      fields.append(fieldname,vf);
    }
    if (field instanceof KeySpecField)
    {
      KeySpecField vf = (KeySpecField)field;
      vf.setContent(content);
      fields.append(fieldname,vf);
    }
  }

  public void setContent (String fieldname, byte[] content)
  {

    Object field = fields.get(fieldname);
    if (field instanceof FixedLengthField)
    {
		System.out.println("in Fixed");
      FixedLengthField f = (FixedLengthField)field;
      f.setContent(content);
      fields.put(fieldname,f);
    }
    if (field instanceof VariableLengthField)
    {
		System.out.println("***SetContent**"+fieldname);

      VariableLengthField vf = (VariableLengthField)field;
      vf.setContent(content);
      fields.put(fieldname,vf);
    }
    if (field instanceof KeySpecField)
    {
		System.out.println("in KeySpecField");
      KeySpecField vf = (KeySpecField)field;
      vf.setContent(content);
      fields.put(fieldname,vf);
    }
  }

  public void setContent (String fieldname, Key k)
  {
    setContent(fieldname, k.getBytes());
  }

  public byte[] getFieldContent(String fieldname)
  {
    Object temp = fields.get(fieldname);
    if (temp == null)
      return null;
    if (temp instanceof FixedLengthField)
    {
      FixedLengthField f = (FixedLengthField)temp;
      return f.getContent();
    }
    if (temp instanceof VariableLengthField)
    {
      VariableLengthField vf = (VariableLengthField)temp;
      byte [] b = new byte[vf.getTotalLength()];
      System.arraycopy(vf.getLengthPrefix(),0,b,0,vf.getLengthPrefix().length);
      System.arraycopy(vf.getContent(),0,b,vf.getLengthPrefix().length,vf.getContent().length);
      //return b;
      return vf.getContent();
    }
    if (temp instanceof KeySpecField)
    {
      KeySpecField vf = (KeySpecField)temp;
      byte [] b = new byte[vf.getTotalLength()];
      System.arraycopy(vf.getLengthPrefix(),0,b,0,vf.getLengthPrefix().length);
      System.arraycopy(vf.getContent(),0,b,vf.getLengthPrefix().length,vf.getContent().length);
      //return b;
      return vf.getContent();
    }
    return null;
  }

  public byte[] getFunctionCode()
  {
    return FunctionCode;
  }

  public void setFunctionCode(byte[] fc)
  {
    FunctionCode = new byte[fc.length];
    System.arraycopy(fc,0,FunctionCode,0,fc.length);
  }

  public void setFunctionCode(byte b)
  {
    FunctionCode = new byte[1];
    FunctionCode[0] = b;
  }

  public int getSequenceNumber() {
    return sequenceNumber;
  }

  public int CalculateMessageLength()
  {
    Enumeration enmrt = fields.elements();
    int len = 0;
    while(enmrt.hasMoreElements())
    {
      Object temp = enmrt.nextElement();
      if (temp instanceof FixedLengthField)
      {
        FixedLengthField f = (FixedLengthField)temp;
        len += f.getLength();
      }
      if (temp instanceof VariableLengthField)
      {
        VariableLengthField vf = (VariableLengthField)temp;
        len += vf.getTotalLength();
      }
      if (temp instanceof KeySpecField)
      {
        KeySpecField vf = (KeySpecField)temp;
        len += vf.getTotalLength();
      }
    }
    return len;
  }

  public TIHashtable getFieldList()
  {
    return fields;
  }

  public int getResponseCode()
  {
    return (int)getFieldContent("RC")[0];
  }
}
