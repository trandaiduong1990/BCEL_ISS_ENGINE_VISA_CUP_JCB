package vn.com.tivn.hsm.phw;

import java.util.LinkedList;
import java.util.logging.Logger;

public class ObjectPool {
  private class CheckingThread implements Runnable{
    int seconds = 0;
    
    public void run(){
      while (true){
        if (validatePool()){  // pool is opened, is active
          if (seconds > checkingInterval){
            synchronized(availableList){
              // remove unused objects
              /*
              while (availableList.size() > minCount){
                Object obj = availableList.removeFirst();
                releaseObject(obj);
              }
              */
            }
            seconds = 0;
          }
          seconds++;
        }
        try{
          Thread.sleep(1000);
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }
  }
  
  private Logger logger = Logger.getLogger(ObjectPool.class.getName());
  
  protected int initCount = 1;
  protected int minCount = 2;
  protected int maxCount = 2;
  protected int checkingInterval = 300;   // in seconds
  
  protected boolean waitForAvailableObject = true;
  protected int waitTimeout = 10;    // in seconds
  protected static int waitSleepTime = 1000;   // in miliseconds
  
  /**
   * The number of object that have been created by this pool.
   */
  protected int activeCount = 0;

  /**
   * The number of objects (created by this pool) that are currently in use.
   * useCount <= activeCount
   */
  protected int useCount = 0;

  /**
   * The list of available object that have been created but are not currently in use.
   * activeCount == useCount + availableList.size()
   */
  protected LinkedList availableList = new LinkedList();

  public ObjectPool() {
    // start checking thread
    new Thread(new ObjectPool.CheckingThread()).start();
  }
  
  /**
   * Internal used
   */
  private boolean initialized = false;
  private void createInitialObjects(){
    if (!initialized){
      // create some initial objects
      synchronized (availableList) {
        while (activeCount < initCount){
          try{
            availableList.addLast(createObject());
            activeCount++;
          }catch(Exception e){
            e.printStackTrace();
            break;
          }
        }
        initialized = true;
      }
    }
  }

  public int getInitCount() {
    return initCount;
  }
  public void setInitCount(int initCount) {
    this.initCount = initCount;
  }
  public int getCheckingInterval() {
    return checkingInterval;
  }

  public int getMinCount() {
    return minCount;
  }
  public void setMinCount(int minCount) {
    this.minCount = minCount;
  }

  public int getMaxCount() {
    return maxCount;
  }
  public void setMaxCount(int maxCount) {
    this.maxCount = maxCount;
  }

  public void setCheckingInterval(int checkingInterval) {
    this.checkingInterval = checkingInterval;
  }
  public boolean isWaitForAvailableObject() {
    return waitForAvailableObject;
  }
  public void setWaitForAvailableObject(boolean waitForAvailableObject) {
    this.waitForAvailableObject = waitForAvailableObject;
  }

  public int getWaitTimeout() {
    return waitTimeout;
  }
  public void setWaitTimeout(int waitTimeout) {
    this.waitTimeout = waitTimeout;
  }

  public int getWaitSleepTime() {
    return waitSleepTime;
  }
  public void setWaitSleepTime(int waitSleepTime) {
    this.waitSleepTime = waitSleepTime;
  }

  /**
   * sub-class should overwrite this method
   * @return true if this pool is valid (e.g can be used)
   */
  protected boolean validatePool(){
    return true;
  }
  
  /**
   * sub-class should overwrite this method
   * @return new valid object
   */
  protected synchronized Object createObject() throws Exception{
    return new Object();
  }
  /**
   * sub-class should overwrite this method
   * @return true if obj is valid (e.g can be used)
   * @param obj object to be validated
   */
  protected boolean validateObject(Object obj){
    return true;
  }
  /**
   * sub-class should overwrite this method
   * @param obj object to be released
   */
  protected synchronized void releaseObject(Object obj){
    logger.info("release object " + obj.hashCode());
  }
  
  public void removeAll()
  {
    synchronized (availableList)
    {
      if (!availableList.isEmpty())  
        availableList.clear();
    }
  }
  
  public Object checkOut() throws Exception{
    createInitialObjects();
    
    int waitSeconds = 0;
    
    logger.info("checkOut");
    
    validatePool();

    while (true) {
      logger.info("Check for timeout, activeCount=" + activeCount + ", useCount=" + useCount);

      if ((waitTimeout > 0) && (waitSeconds >= waitTimeout)) {
        break;
      }
      // Return an existing object from the pool if there is one
      synchronized (availableList) {
        // if (availableList.size() > 0){
        if (!availableList.isEmpty()) {
          // Allocate the first available object
          Object obj = availableList.removeFirst();
          logger.info("Found available object " + obj.hashCode());
          
          // make sure this object is valid
          if (!validateObject(obj)){
            logger.warning("Object is not valid, releasing");
            releaseObject(obj);
            activeCount--;
            continue;
          }
          
          useCount++;
          logger.info("Return allocated object, activeCount=" + activeCount + ", useCount=" + useCount);
          return obj;
        }
      }

      // Create a new object if we are not yet at the maximum
      synchronized(this){
        if ((maxCount > 0) && (activeCount < maxCount) && (maxCount > 1)) {
          Object obj = createObject();
          activeCount++;
          
          logger.info("Return new object, activeCount=" + activeCount + ", useCount=" + useCount);
          
          useCount++;
          return obj;
        }
      }
      
      if (!waitForAvailableObject){
        throw new Exception("checkOut: No available object.");
      }
      
      // Wait for an existing object to be returned
      logger.info("Sleep until next test");
    
      try {
        Thread.sleep(waitSleepTime);
        waitSeconds++;
      } catch (InterruptedException ignore) {}
    }
    // We have timed out awaiting an available connection
    logger.info("Timeout awaiting available object");
    throw new Exception("checkOut: Timeout awaiting available object");
  }
  
  public void checkIn(Object obj){
    synchronized (availableList) {
      availableList.addLast(obj);
      useCount--;
      logger.info("checkIn object " + obj.hashCode() + " activeCount=" + activeCount + ", useCount=" + useCount);
    }
  }

  static class TestObjectThread extends Thread{
    ObjectPool pool;
    TestObjectThread(ObjectPool pool){
      this.pool = pool;
    }
    public void run(){
      for (int i=0; i<100; i++){
        try{
          // check out
          Object obj = pool.checkOut();
          
          // do some thing
          Thread.sleep(100);
          
          // check in
          pool.checkIn(obj);
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }
  }
  public static void main(String[] args)  throws Exception{
    ObjectPool pool = new ObjectPool();
    
    pool.initCount = 2;
    pool.minCount = 4;
    pool.maxCount = 10;
    
    int MAX_THREAD = 20;
    for (int i=0; i<MAX_THREAD; i++){
      new TestObjectThread(pool).start();
    }
  }
}
