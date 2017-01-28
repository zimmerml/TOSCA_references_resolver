package TOSCA;

public abstract class PacketManager {
	protected String Name;
	public String getName(){
		return Name;
	}
	
	abstract void proceed(Control_references cr);

}
