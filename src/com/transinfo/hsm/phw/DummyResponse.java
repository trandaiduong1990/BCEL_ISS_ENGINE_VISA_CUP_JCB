package com.transinfo.hsm.phw;

//A special message that contains only the response code.
//This class is used when a HSMException occur to return the response code back to the main program
public class DummyResponse extends HSMMsg
{
  FixedLengthField RC = new FixedLengthField();
  public DummyResponse()
  {
    super.addField("RC",RC);
  }

  public DummyResponse(byte rc)
  {
    RC.setContent(new byte[]{rc});
    super.addField("RC",RC);
  }
}