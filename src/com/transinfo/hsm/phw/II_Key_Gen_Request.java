package com.transinfo.hsm.phw;

public class II_Key_Gen_Request extends HSMMsg
{
  public II_Key_Gen_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x04,(byte)0x02});
    super.addField("FM", new FixedLengthField());
    //super.addField("KIS_Spec", new VariableLengthField());
    //use KTM_Spec as label for compatibility reason. The actual key used is KIS
    super.addField("KTM_Spec", new VariableLengthField());
    super.addField("Key_Flags", new FixedLengthField(2));
  }
}