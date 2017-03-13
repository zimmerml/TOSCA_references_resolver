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
	
	
	/** Constructor
	 * list right extensions and creates package managers
	 * 
	 */
	public Ansible(){
		Name = "Ansible";
		extensions = new LinkedList<String>();
		extensions.add(".zip");
		extensions.add(".yml");

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new Apt());
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
						List<String> files = zip.unZipIt(cr.getFolder() + f, folder);
						for(String file:files)
							if(file.toLowerCase().endsWith("yml"))
								proceed(folder + file,cr,f);
						if(isChanged){
							new File(cr.getFolder() +f).delete();
							zip.zipIt(cr.getFolder() +f, folder);
						} 
						//TODO
						//zip.delete(new File(folder));
					}
					else{
						proceed(f, cr, f);
					}
				}
	}
	public void proceed(String filename, Control_references cr, String source) throws FileNotFoundException, IOException, JAXBException {
		for(PacketManager pm:packetManagers)
			pm.proceed(filename, cr, source);
	}

}
