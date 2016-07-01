package com.transinfo.hsm.phw;

public class PrintDataSet extends HSMMsg
{
  public PrintDataSet()
  {
    super.addField("LineNo",new FixedLengthField(1));
    super.addField("ColumnNo",new FixedLengthField(1));
    super.addField("Data",new VariableLengthField());
  }

  public void setLineNo(byte[] LineNo)
  {
    super.setContent("LineNo",LineNo);
  }

  public void setColumnNo(byte[] ColumnNo)
  {
    super.setContent("ColumnNo",ColumnNo);
  }

  public void setData(byte[] Data)
  {
    super.setContent("Data",Data);
  }
}