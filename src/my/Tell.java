package my;

public class Tell{
	public String signature;
	public String command;
	public String local_ip;
	public int local_port;
	public String global_ip;
	public int global_port;
	
	public Tell(){
		signature = "original message";
		command = "you can match with ...";
	}
	
	@Override
	public String toString(){
		return
				"Tell [signature=" + signature +
				", command=" + command +
				", local_ip=" + local_ip +
				", local_port=" + local_port +
				", global_ip=" + global_ip +
				", global_port=" + global_port +
				"]";
	}
}
