package TOSCA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public abstract class Language {
	
	//List of package managers supported by language
	protected List <PacketManager> packetManagers;
	
	//Extensions for this language
	protected List <String> extensions;
	
	//Language Name
	public String Name;

	/** Get supported extensions
	 * @return list with extensions
	 */
	public List <String> getExtensions() {
		return extensions;
	}
	
	/** Proceed file, transfer it to package managers
	 * @param cr CSAR manager
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void proceed(Control_references cr) throws FileNotFoundException, IOException
	{
		if(cr == null)
			throw new NullPointerException();
		for(String f:cr.getFiles())
			for(String suf:extensions)
				if(f.toLowerCase().endsWith(suf.toLowerCase()))
					for(PacketManager pm:packetManagers)
						pm.proceed(f,cr);
	}
}
