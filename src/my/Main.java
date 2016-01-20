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
 */
public class Main {
	
	public static void main(String[] args) {
		try{
			Main me = new Main();
			
			System.out.println("server started");
			
			while(true){
				System.out.println("server's IP: " + InetAddress.getLocalHost().getHostAddress());
				System.out.println("server's port: " + port);
				System.out.println("waiting any requests...");
				me.process();
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public Main() throws Exception{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		socket = new DatagramSocket(port);
		members = new Member[max_member];
	}
	
	
	public void process() throws Exception{
			byte buffer[] = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buffer,  buffer.length);
			socket.receive(packet);
			
			show_info_got_message(packet);
			
			String request_str = new String(packet.getData());
			System.out.println("    the message:");
			System.out.println(request_str);
			
			Request request;
			try{
				request = mapper.readValue(request_str, Request.class);
			}
			catch(Exception e){
				System.out.println("the data was NOT json data.");
				return;
			}
			process_request(request, packet);
	}

	/**
	 * @brief I will show you the infomation about the message you got.
	 * @param packet: Set the packet you got.
	 */
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
			System.out.println("the signature was invalid.");
			return;
		}
		
		if(request.version != 0){
			System.out.println("the version is not supported.");
			return;
		}
		
		if(request.request.equals("join")){
			join(request, packet);
		}
		else {
			System.out.println("that's an unknown request.");
		}
	}
	
	private void join(Request request, DatagramPacket packet)throws Exception{
		Surfer requester = get_requester(request, packet);
		int ID = add_member_ifneed(requester);
		if(ID == -1) {
			// Then there are no vacant table.
			System.out.println("Oops! Unfortunately, there are no vacant table.");
			tell_fully_occupied(requester);
			return;
		}
		
		tell_requester_others(requester, ID);
	}
	
	/**
	 * @brief I add the new member if the member has NOT registered yet.
	 * @param requester
	 * @return The member's ID is returned.
	 */
	private int add_member_ifneed(Surfer requester)throws Exception {
		int exist_index = find_member(requester);
		if(exist_index != -1) {
			// Then he has already registered.
			System.out.println("He has already registered.");
			return exist_index;
		}
		
		int index = find_empty_seet();
		if(index == -1) {
			// Then there're no vacant table.
			return -1;
		}
		
		members[index] = make_member(requester, index);
		System.out.println("The new user was registered");
		return index;
	}
	
	/**
	 * @brief I find the user who is registered on the members.
	 * @param requester
	 * @return I return the ID of the member.
	 */
	private int find_member(Surfer requester) {
		for(int index=0; index < max_member; ++index) {
			Member member = members[index];
			if(member == null) {
				continue;
			}
			
			if(member.surfer.equals(requester)==true) {
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * @brief I find a empty seet for the new member.
	 * @return
	 *  I return the index of the variable 'members' that I found it empty.
	 *  If the empty seet was not found, I return -1.
	 */
	private int find_empty_seet()
	{
		for(int index=0; index < max_member; ++index)
		{
			if(members[index]==null)
			{
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * @brief I tell the user that there are no vacant table.
	 * @param requester: Set the user who did request to join.
	 * @throws Exception
	 */
	private void tell_fully_occupied(Surfer requester)throws Exception
	{
		String reply = "{\"signature\": \"OnlineParty\", \"version\": 0, \"reply\": \"fully occupied\"}";
		byte reply_data[] = reply.getBytes();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(requester.get_global().ip), requester.get_global().port);
        socket.send(dp);
	}
	
	private Member make_member(Surfer requester, int ID){
		Member member = new Member();
		member.ID = ID;
		member.surfer = requester;
		return member;
	}
	
	/**
	 * @brief I tell the user the IP addresses and the ports of the others
	 *        who have already joined us.
	 * @param requester: Set the user who did request to join.
	 * @param ter_ID: Set the ID of the requester.
	 * @throws Exception
	 */
	private void tell_requester_others(Surfer requester, int ter_ID)throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("{\"signature\": \"OnlineParty\", \"version\": 0, \"reply\": \"join\", \"your ID\": ");
		builder.append(Integer.toString(ter_ID));
		builder.append(", \"the others\": [");
		
		boolean is_first = true;
		for(int i = 0; i < max_member; ++i) {
			if(i == ter_ID){continue;}
			if(members[i] == null){continue;}
			if(is_first) {
				is_first = false;
			}
			else {
				builder.append(", ");
			}
			builder.append(members[i].toJsonString());
		}
		builder.append("]}");
		String reply = new String(builder);
		System.out.println("I did reply:");
		System.out.println(reply);
		byte reply_data[] = reply.getBytes();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(requester.get_global().ip), requester.get_global().port);
        socket.send(dp);
	}
	
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
	
	static final int port = 9696;
	static final int max_member = 20;
	
	private ObjectMapper mapper;
	private DatagramSocket socket;
	private Member[] members;
	
	


}


