package my;

public class IpPort{
	public String ip;
	public int port;
	
	public boolean equals(IpPort another){
		if(ip.equals(another.ip) == false){
			System.out.println("different IP.");
			return false;
		}
		if(port != another.port){
			System.out.println("different port");
			return false;
		}
		return true;
	}
}
