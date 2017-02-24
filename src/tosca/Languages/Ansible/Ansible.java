package tosca.Languages.Ansible;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.Control_references;
import tosca.zip;
import tosca.Abstract.Language;
import tosca.Abstract.PacketManager;

public class Ansible extends Language {
	
	//Language Name
	static public final String Name = "Ansible";
	
	/** Constructor
	 * list right extensions and creates package managers
	 * 
	 */
	public Ansible(){
		extensions = new LinkedList<String>();
		extensions.add(".zip");

		packetManagers = new LinkedList<PacketManager>();
	}
	
	public void proceed(Control_references cr) throws FileNotFoundException, IOException, JAXBException {
		if (cr == null)
			throw new NullPointerException();
		for (String f : cr.getFiles())
			for (String suf : extensions)
				if (f.toLowerCase().endsWith(suf.toLowerCase())){
					if(suf.equals(".zip")){
						boolean isChanged = false;
//						String filename = new File(f).getName();
						String folder = new File(f).getParent() + File.separator ;
						List<String> files = zip.unZipIt(f, folder);
						for(String file:files)
							proceed(file);
						if(isChanged){
							new File(f).delete();
							zip.zipIt(f, folder);
						} 
						//zip.delete(new File(folder));
					}
					else{
						proceed(f);
					}
				}
	}
	public void proceed(String filename) throws FileNotFoundException, IOException, JAXBException {
		
	}

}
