package my;

public class MemberManager {
	public MemberManager() {
		members = new Member[max_member];
	}

	public void update(Surfer requester) {
		update_last_sync_ms(requester);
		remove_afk(requester);
	}






	private static final int max_member = 20;

	private Member[] members;

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

}
