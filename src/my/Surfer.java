package my;

public class Surfer{
	public boolean is_available(){
		if(local == null){ return false; }
		if(global == null){ return false; }
		return true;
	}
	
	public boolean equals(Surfer another){
		return global.equals(another.get_global());
	}
	
	
	public void set(IpPort local, IpPort global){
		this.local = local;
		this.global = global;
	}
	
	public IpPort get_local(){
		return local;
	}
	public IpPort get_global(){
		return global;
	}
	
	
	
	public Surfer(){
		local = null;
		global = null;
	}
	
	private IpPort local;
	private IpPort global;
}
