/**

public class MessageHandler{

	private String port;
	private String messageType;
	private String messageHandler;
	private String messageProcessor;

	public void setPort(String port){

		this.port = port;
	}

	public String getPort(){

		return this.port;
	}

	public void setMessageType(String messageType){

		this.messageType = messageType;
	}

	public String getMessageType(){

		return this.messageType;
	}


	public void setMessageHandler(String messageHandler){

		this.messageHandler = messageHandler;
	}

	public String getMessageHandler(){

		return this.messageHandler;
	}


	public void setMessageProcessor(String messageProcessor){

		this.messageProcessor = messageProcessor;
	}

	public String getMessageProcessor(){

		return this.messageProcessor;
	}


	public String toString(){

		return this.port + " -- " + this.messageHandler;
	}
}