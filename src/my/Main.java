package my;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * 
 * @author yuni
 *
 * @see in this version, I will die when ID_count exceed the limit.
 */
public class Main {
	
	public static void main(String[] args) {
		try{
			Main me = new Main();
			
			System.out.println("server started");
			System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());
			System.out.println("port: " + port);
			
			me.process();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public Main() throws Exception{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		socket = new DatagramSocket(port);
		members = new ArrayList<Member>();
		ID_count = 0;
	}
	
	
	public void process() throws Exception{
		while(true){
			byte buffer[] = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buffer,  buffer.length);
			socket.receive(packet);
			
			show_info_got_message(packet);
			
			String orge_str = new String(packet.getData());
			System.out.println("    the message:");
			System.out.println(orge_str);
			
			Request request;
			try{
				request = mapper.readValue(orge_str, Request.class);
			}
			catch(Exception e){
				continue;
			}
			process_request(request, packet);
		}
	}


	private void show_info_got_message(DatagramPacket packet){
		InetSocketAddress sockaddr = (InetSocketAddress)packet.getSocketAddress();
		InetAddress ip = sockaddr.getAddress();
		System.out.println("you got a message:");
		System.out.println("    sender's socket id: " + sockaddr.toString());
		System.out.println("    sender's global ip: " + ip.getHostAddress());
		System.out.println("    sender's global port: " + sockaddr.getPort());
	}
	
	private void process_request(Request request, DatagramPacket packet) throws Exception {
		if(request.signature.equals("OnlineParty")==false){
			return;
		}
		
		if(request.version != 0){
			return;
		}
		
		if(request.request.equals("join")){
			join(request, packet);
			//match_request(request, packet);
		}
	}
	
	private void join(Request request, DatagramPacket packet)throws Exception{
		Surfer requester = get_requester(request, packet);
		int index = prepare_seet_and_get_index(requester);
		Member member = make_member(requester);
		members.get(index).copy(member);
		/*
		if(is_requester_already_exist(requester)==false){
			members.add(member);
		}
		//*/
		
		tell_requester_others(requester, members.get(index).ID);
		tell_others_rookie(requester, members.get(index).ID);
	}
	
	private int prepare_seet_and_get_index(Surfer requester){
		for(int index = 0; index < members.size(); ++index){
			if(members.get(index).surfer.equals(requester)){
				return index;
			}
		}
		members.add(new Member());
		return members.size()-1;
	}
	
	/*
	private boolean is_requester_already_exist(Surfer requester){
		for(Member member : members){
			if(member.surfer.equals(requester)){
				return true;
			}
		}
		return false;
	}
	//*/
	
	private Member make_member(Surfer requester){
		Member member = new Member();
		member.ID = ID_count;
		++ID_count;
		member.surfer = requester;
		return member;
	}
	
	private void tell_requester_others(Surfer requester, int ter_ID)throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("{\"signature\": \"OnlineParty\",\"version\": 0,\"reply\": \"join\",\"your ID\": ");
		builder.append(Integer.toString(ter_ID));
		builder.append(",\"the others\": [");
		builder.append(members.get(0).toJsonString());
		for(int i = 1; i< members.size(); ++i){
			builder.append(",");
			builder.append(members.get(i).toJsonString());
		}
		builder.append("]}");
		String reply = new String(builder);
		byte reply_data[] = reply.getBytes();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(requester.get_global().ip), requester.get_global().port);
        socket.send(dp);
	}
	
	private void tell_others_rookie(Surfer requester, int ter_ID)throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("{\"signature\": \"OnlineParty\",\"version\": 0,\"info\": \"rookie joined\",\"ID\": ");
		builder.append(ter_ID);
		builder.append(", \"IP\": \"");
		builder.append(requester.get_global().ip);
		builder.append("\", \"port\": ");
		builder.append(requester.get_global().port);
		builder.append("}");
		String reply = new String(builder);
		tell_others(reply, ter_ID);
	}
	
	private void tell_others(String reply, int ter_ID)throws Exception{
		byte reply_data[] = reply.getBytes();
		for(Member member : members){
			if(member.ID==ter_ID){continue;}
	        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(member.surfer.get_global().ip), member.surfer.get_global().port);
	        socket.send(dp);
		}
	}
	
	
	/*
	private void match_request(Request request, DatagramPacket packet) throws Exception {
		Surfer requester = get_requester(request, packet);
		if(does_surfer_has_another(requester)){
			tell_another(requester);
		}
		else {
			surfer = requester;
			tell_reject(packet);
			System.out.println("rejected");
			System.out.println("surfer was updated:");
			System.out.println("    global_ip: " + surfer.get_global().ip);
			System.out.println("    global_port: " + surfer.get_global().port);
		}
	}
	//*/
	
	/*
	private boolean does_surfer_has_another(Surfer requester){
		System.out.println("culculating surfer's differences...");
		if(surfer.is_available() == false){
			System.out.println("there are no waiting surfer.");
			return false;
		}
		
		System.out.println("waiting surfer's IP: " + surfer.get_global().ip);
		System.out.println("request surfer's IP: " + requester.get_global().ip);
		System.out.println("waiting surfer's port: " + surfer.get_global().port);
		System.out.println("request surfer's port: " + requester.get_global().port);
				
		if(surfer.equals(requester) == true){
			System.out.println("different IP.");
			return false;
		}
		
		return true;
	}
	//*/
	
	//*
	private Surfer get_requester(Request request, DatagramPacket packet){
		InetSocketAddress sockaddr = (InetSocketAddress)packet.getSocketAddress();
		InetAddress ip = sockaddr.getAddress();

		IpPort global = new IpPort();
		global.ip = ip.getHostAddress();
		global.port = sockaddr.getPort();
		
		IpPort local = new IpPort();
		local.ip = global.ip;
		local.port = global.port;
		
		Surfer requester = new Surfer();
		requester.set(local, global);
		return requester;
	}
	//*/
	
	/*
	private void tell_another(Surfer requester) throws Exception {
		tell_each(surfer, requester);
		tell_each(requester, surfer);
	}
	//*/
	
	/*
	private void tell_each(Surfer from, Surfer to) throws Exception {
		Tell tell = new Tell();
		tell.local_ip = from.get_local().ip;
		tell.local_port = from.get_local().port;
		tell.global_ip = from.get_global().ip;
		tell.global_port = from.get_global().port;
		
		String response = mapper.writeValueAsString(tell);
		byte reply_data[] = response.getBytes();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(to.get_global().ip), to.get_global().port);
        DatagramSocket ds = new DatagramSocket();
        ds.send(dp);
        ds.close();
        
        System.out.println("I send response:");
        System.out.println("    content:");
        System.out.println(response);
        System.out.println("    to...");
        System.out.println("        IP: " + to.get_global().ip);
        System.out.println("        InetAddress: " + InetAddress.getByName(to.get_global().ip).toString());
        System.out.println("        port: " + to.get_global().port);
	}
	//*/
	
	/*
	private void tell_reject(DatagramPacket packet) throws Exception {
		String response = mapper.writeValueAsString(new Reject());
		byte reply_data[] = response.getBytes();
		InetSocketAddress sockaddr = (InetSocketAddress)packet.getSocketAddress();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, sockaddr.getAddress(), sockaddr.getPort());
        DatagramSocket ds = new DatagramSocket();
        ds.send(dp);
        ds.close();
	}
	//*/
	
	static final int port = 9696;
	
	private ObjectMapper mapper;
	private DatagramSocket socket;
	private ArrayList<Member> members;
	private int ID_count;
	
	


}


