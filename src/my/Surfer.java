package my;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Surfer{
	public Surfer(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	public Surfer(DatagramPacket packet) {
		InetSocketAddress sockaddr = (InetSocketAddress)packet.getSocketAddress();
		InetAddress address = sockaddr.getAddress();
		
		ip = address.getHostAddress();
		port = sockaddr.getPort();
	}
	
	public boolean equals(Surfer another){
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
	
	public String get_ip() {
		return ip;
	}
	
	public int get_port() {
		return port;
	}
	
	
	
	
	private String ip;
	private int port;
}
