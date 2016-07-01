package vn.com.tivn.hsm.phw;
import java.util.Enumeration;
import java.util.Vector;

public class TIHashtable
{
  private Vector keys = new Vector();
  private Vector elements = new Vector();
  private int index = 0; // used to map the keys with the objects
  
  public TIHashtable()
  {
  }
  
  public TIHashtable(int size)
  {
    keys.setSize(size);
    elements.setSize(size);
  }
  
  public void put(Object key, Object element) 
  {
    int i = -1;
    if (!keys.isEmpty())
      i = keys.indexOf(key);
    if (i>=0) //existing key
    {
      elements.remove(i);
      elements.add(i,element);
    }
    else
    {
      keys.add(index,key);
      elements.add(index,element);
      index++;
    }
  }
  
  //Add the objects at the end of the queue, ignoring duplication
  public void append(Object key, Object element)
  {
    keys.add(index,key);
    elements.add(index,element);
    index++;    
  }
  
  public Object get(Object key)
  {
    if (keys.indexOf(key)>=0)
      return elements.elementAt(keys.indexOf(key));
    return null;
  }
  
  public Object getElementAt(int index)
  {
    return elements.elementAt(index);
  }
  
  public Object getKeyAt(int index)
  {
    return keys.elementAt(index);
  }
  
  public int getIndex()
  {
    return index;
  }
  
  public Enumeration keys()
  {
    return keys.elements();
  }
  
  public Enumeration elements()
  {
    return elements.elements();
  }
  
  public void putAll(TIHashtable t)
  {
    Enumeration enmrt = t.keys();
    Enumeration elem = t.elements();
    
    while ((enmrt.hasMoreElements())&&(elem.hasMoreElements()))
    {
      Object key = enmrt.nextElement();
      Object element = elem.nextElement();
      this.append(key,element);
    }
  }
  
   public Object getKeyfromElement (Object e)
  {
    if (elements.indexOf(e)>-1)
    {
      System.out.println("Index "+elements.indexOf(e));
      return keys.elementAt(elements.indexOf(e));
    }
    return null;
  }
  
}