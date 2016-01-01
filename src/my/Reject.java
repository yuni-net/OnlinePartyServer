package my;

public class Reject{
	public String signature;
	public String command;
	
	public Reject(){
		signature = "original message";
		command = "wait another";
	}
	
	@Override
	public String toString(){
		return "Reject [signature=" + signature + ", command=" + command + ":c]";
	}
}

