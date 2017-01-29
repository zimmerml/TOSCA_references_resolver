package TOSCA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public abstract class Language {
	protected List <PacketManager> packetManagers;
	protected List <String> extensions;
	protected String Name;
	protected abstract void Init(Control_references cr) throws IOException;
	public String getName(){
		return Name;
	}
	public List <String> getExtensions() {
		return extensions;
	}
	public void proceed(Control_references cr) throws FileNotFoundException, IOException
	{
		Init(cr);
		if(cr == null)
			throw new NullPointerException();
		for(String f:cr.getFiles())
			for(String suf:extensions)
				if(f.toLowerCase().endsWith(suf.toLowerCase()))
					for(PacketManager pm:packetManagers)
						pm.proceed(f,cr);
	}
}
