package vn.com.tivn.hsm.phw;
import java.io.ByteArrayOutputStream;

public class PrintPIN_Request extends HSMMsg
{
  public PrintPIN_Request()
  {
    super.setFunctionCode(new byte[] {(byte)0xEE,(byte)0x0E,(byte)0x05});
    super.addField("FM",new FixedLengthField());
    super.addField("ePPK",new FixedLengthField(8));
    super.addField("PPK_Spec",new VariableLengthField());
    super.addField("PFi",new FixedLengthField());
    super.addField("ANB",new FixedLengthField(6));
    super.addField("PAN",new FixedLengthField(8));
    super.addField("DataSets",new FixedLengthField(1));    
  }
  
  public void addPrintData(PrintDataSet[] pindata)
  {
    super.setContent("DataSets",new byte[]{(byte)pindata.length});
    for (int i=0;i<pindata.length;i++)
    {
      super.addField("LineNo",new FixedLengthField(1), pindata[i].getFieldContent("LineNo"));
      super.addField("ColumnNo",new FixedLengthField(1), pindata[i].getFieldContent("ColumnNo"));
      super.addField("Data",new VariableLengthField(), pindata[i].getFieldContent("Data"));
    }
    //return super.pack();
  }
}