package my;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

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
				System.out.println("current time millis: " + System.currentTimeMillis());
				ByteBuffer buffer = ByteBuffer.allocate(8);
				buffer.putLong(System.currentTimeMillis());
				System.out.print("byte buffer current millis:");
				for(int i=0; i < 8; ++i) {
					System.out.print(" " + (buffer.get(i)&0xff));
				}
				System.out.println("");
				buffer = null;
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
			byte[] data = packet.getData();
			Surfer requester = get_requester(packet);

			update_last_sync_ms(requester);
			remove_afk(requester);

			if(is_it_binary_data(data)) {
				System.out.println("It's the binary data.");
				process_binary_data(data, requester);
				return;
			}

			String request_str = new String(data);
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
			process_request(request, requester);
	}

	/**
	 * @brief I return requester current time millis.
	 * @param requester
	 * @throws Exception
	 */
	private void return_time(Surfer requester) throws Exception {
		final String signature = new String("OnlineParty\0");
		final byte version = 0;
		final int length_version = 1;
		final int length_reply = 1;
		final int length_content = 8;
		final int capacity = signature.length()+length_version+length_reply+length_content;

		ByteBuffer buffer = ByteBuffer.allocate(capacity);
		buffer.put(signature.getBytes(), 0, signature.length());
		buffer.put(version);
		buffer.put(request_sync_time);
		buffer.putLong(System.currentTimeMillis());
		
		if(buffer.hasArray()==false) {
			System.out.println("failed to reply for syncing time millis");
			return;
		}
		
		byte[] reply_data = buffer.array();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(requester.get_global().ip), requester.get_global().port);
        socket.send(dp);
	}

	/**
	 * @brief I process the binary data for 'OnlineParty.'
	 * @param data: Set the data you received.
	 * @param requester: Set the object of Surfer which send the request.
	 */
	private void process_binary_data(byte[] data, Surfer requester) throws Exception {
		byte version = data[12];
		if(version==0) {
			byte request = data[13];
			if(request==request_sync_time) {
				return_time(requester);
			}
		}
		else
		{
			System.out.println("The version of the data is unsupported. The version: " + version);
		}
	}

	/**
	 * @brief I identify whether it is a binary data for 'OnlineParty.'
	 * @param data
	 * @return
	 * 		true: It is the data for 'OnlineParty.'
	 * 		false: It is NOT the data for 'OnlineParty.'
	 */
	private boolean is_it_binary_data(byte[] data) {
		String signature = new String(data, 0, 11);
		return signature.equals("OnlineParty");
	}

	/**
	 * @brief I remove the afk user from members.
	 * @param requester: Set the excepted user.
	 */
	private void remove_afk(Surfer requester) {
		long now = System.currentTimeMillis();
		for(int index=0; index < max_member; ++index) {
			Member member = members[index];
			if(member==null) { continue; }
			if(member.surfer.equals(requester)) { continue; }
			if(member.is_afk(now)) {
				System.out.println("members["+index+"] is afk. I removed it from the member");
				members[index] = null;
			}
		}
	}

	/**
	 * @brief I update 'last_sync_ms' of each member.
	 * @param requester: Set the Surfer object of who sent the request.
	 */
	private void update_last_sync_ms(Surfer requester) {
		System.out.println("I update 'last_sync_ms'");
		Member member = find_member_of(requester);
		if(member == null){
			System.out.println("who sent request was not found in member");
			return;
		}
		member.update_last_sync_ms();
	}

	/**
	 * @brief I find the member which matches the 'requester.'
	 * @param requester: Set the Surfer object of who sent the request.
	 * @return
	 *     I find it: the Member object is returned.
	 *     I couldn't find it: null is returned.
	 */
	private Member find_member_of(Surfer requester) {
		for(int index=0; index < max_member; ++index) {
			Member member = members[index];
			if(member==null){
				continue;
			}
			if(member.surfer.equals(requester)) {
				return member;
			}
		}
		return null;
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

	/**
	 * @brief I process the request from the cliant.
	 * @param request: Set the request data.
	 * @param requester: Set the Surfer object of the user.
	 * @throws Exception
	 */
	private void process_request(Request request, Surfer requester) throws Exception {
		if(request.signature.equals("OnlineParty")==false){
			System.out.println("the signature was invalid.");
			return;
		}

		if(request.version != 0){
			System.out.println("the version is not supported.");
			return;
		}

		if(request.request.equals("join")){
			join(requester);
		}
		else if(request.request.equals("sync_time")) {
			// todo
		}
		else if(request.request.equals("Im_finding_server")) {
			tell_user_about_me(requester);
		}
		else {
			System.out.println("that's an unknown request.");
		}
	}
	
	private void tell_user_about_me(Surfer requester) throws Exception {
		String reply = "{\"signature\": \"OnlineParty\", \"version\": 0, \"reply\": \"Im_finding_server\"}";
		byte reply_data[] = reply.getBytes();
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(requester.get_global().ip), requester.get_global().port);
        socket.send(dp);
	}

	/**
	 * @brief I attempt to register the user to the member.
	 * @param requester: Set the Surfer object of the user.
	 * @throws Exception
	 */
	private void join(Surfer requester)throws Exception{
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

	/**
	 * @brief I make Member object based on Surfer object and ID.
	 * @param requester: Set the Surfer object.
	 * @param ID
	 * @return the Member object is returned.
	 */
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

	/**
	 * @brief I make Surfer object based on the 'packet.'
	 * @param packet
	 * @return the Surfer object is returned.
	 */
	private Surfer get_requester(DatagramPacket packet){
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
	static final byte request_sync_time = 0;

	private ObjectMapper mapper;
	private DatagramSocket socket;
	private Member[] members;




}


