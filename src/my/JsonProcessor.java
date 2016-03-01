package my;

import java.net.DatagramSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonProcessor {
	public JsonProcessor() {
		mapper = new ObjectMapper();
	}

	public void process_as_needed(DatagramSocket socket, String request_str, Surfer requester) throws Exception {
		Request request;
		try{
			request = mapper.readValue(request_str, Request.class);
		}
		catch(Exception e){
			System.out.println("the data was NOT json data.");
			return;
		}
		process_request(socket, request, requester);
	}






	private ObjectMapper mapper;

	/**
	 * @brief I process the request from the cliant.
	 * @param request: Set the request data.
	 * @param requester: Set the Surfer object of the user.
	 * @throws Exception
	 */
	private void process_request(DatagramSocket socket, Request request, Surfer requester) throws Exception {
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
		else if(request.request.equals("sync_time"))
		{
			// todo
		}
		else {
			System.out.println("that's an unknown request.");
		}
	}

	/**
	 * @brief I attempt to register the user to the member.
	 * @param requester: Set the Surfer object of the user.
	 * @throws Exception
	 */
	private void join(MemberManager member_manager, Surfer requester)throws Exception{
		int ID = add_member_ifneed(member_manager, requester);
		if(ID == -1) {
			// Then there are no vacant table.
			System.out.println("Oops! Unfortunately, there are no vacant table.");
			tell_fully_occupied(requester);
			return;
		}

		tell_requester_others(requester, ID);
	}

}
