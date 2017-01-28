package TOSCA;

import java.util.List;

public abstract class Language {
	protected List <PacketManager> packetManagers;
	protected List <String> extensions;
	protected String Name;
	public String getName(){
		return Name;
	}
	public List <String> getExtensions() {
		return extensions;
	}
	public void proceed(Control_references cr)
	{
		if(cr == null)
			throw new NullPointerException();
		for(PacketManager pm:packetManagers)
			pm.proceed(cr);
	}
}
