package tosca.Languages.Bash;

import java.util.LinkedList;

import tosca.Abstract.Language;
import tosca.Abstract.PacketManager;

/** Script Language
 * @author Yaroslav
 *
 */
public final class Bash extends Language {

	
	/** Constructor
	 * list right extensions and creates package managers
	 * 
	 */
	public Bash(){
		Name = "Bash";
		extensions = new LinkedList<String>();
		extensions.add(".sh");
		extensions.add(".bash");

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new PM_apt_get());
	}
}
