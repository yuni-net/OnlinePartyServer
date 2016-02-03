package my;

public class Member {
	public int ID;
	public Surfer surfer;
	
	public void copy(Member another){
		ID = another.ID;
		surfer = another.surfer;
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
