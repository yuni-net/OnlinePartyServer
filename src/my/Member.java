package my;

public class Member {
	public int ID;
	public Surfer surfer;
	private long last_sync_ms;
	
	public Member() {
		last_sync_ms = System.currentTimeMillis();
	}

	public void copy(Member another){
		ID = another.ID;
		surfer = another.surfer;
		last_sync_ms = another.last_sync_ms;
	}
	
	public void update_last_sync_ms() {
		last_sync_ms = System.currentTimeMillis();
	}
	
	public boolean is_afk(long now) {
		final long limit_ms_to_afk = 18000;
		long gap = now-last_sync_ms;
		boolean is_it_afk = gap >= limit_ms_to_afk;
		System.out.println("now="+now+", member["+ID+"]: last_sync_ms="+last_sync_ms+", gap="+gap);
		return is_it_afk;
	}

	public String toJsonString(){
		StringBuilder builder = new StringBuilder();
		builder.append("{\"ID\": ");
		builder.append(Integer.toString(ID));
		builder.append(", \"IP\": \"");
		builder.append(surfer.get_global().ip);
		builder.append("\", \"port\": ");
		builder.append(surfer.get_global().port);
		builder.append("}");
		return new String(builder);
	}
}
