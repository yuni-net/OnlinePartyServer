package my;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class OnlinePartyServer {
	public OnlinePartyServer() throws Exception {
		socket = new DatagramSocket(port);
		member_manager = new MemberManager();
		binary_processor = new BinaryProcessor();
		json_processor = new JsonProcessor();
	}

	public void process() {
		byte buffer[] = new byte[65536];
		DatagramPacket packet = new DatagramPacket(buffer,  buffer.length);
		socket.receive(packet);

		show_info_got_message(packet);
		byte[] data = packet.getData();
		Surfer requester = new Surfer(packet);

		member_manager.update(requester);

		if(binary_processor.is_binary_data(data)) {
			System.out.println("It's the binary data.");
			binary_processor.process(socket, data, requester);
			return;
		}

		String request_str = new String(data);
		System.out.println("    the message:");
		System.out.println(request_str);

		json_processor.process_as_needed(socket, request_str, requester);
	}













	private static final int port = 9696;

	private DatagramSocket socket;
	private MemberManager member_manager;
	private BinaryProcessor binary_processor;
	private JsonProcessor json_processor;

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


}
