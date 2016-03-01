package my;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class BinaryProcessor {
	public boolean is_binary_data(byte[] data) {
		String signature = new String(data, 0, 11);
		return signature.equals("OnlineParty");
	}

	public void process(DatagramSocket socket, byte[] data, Surfer requester) throws Exception {
		byte version = data[12];
		if(version==0) {
			byte request = data[13];
			if(request==request_sync_time) {
				return_time(socket, requester);
			}
		}
		else
		{
			System.out.println("The version of the data is unsupported. The version: " + version);
		}
	}





	static final byte request_sync_time = 0;

	/**
	 * @brief I return requester current time millis.
	 * @param requester
	 * @throws Exception
	 */
	private void return_time(DatagramSocket socket, Surfer requester) throws Exception {
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
        DatagramPacket dp = new DatagramPacket(reply_data, reply_data.length, InetAddress.getByName(requester.get_ip()), requester.get_port());
        socket.send(dp);
	}
}
