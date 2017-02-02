package TOSCA;

import java.util.LinkedList;

/** Script Language
 * @author jery
 *
 */
public final class Bash extends Language {

	//Language Name
	static public final String Name = "Bash";
	
	/** Constructor
	 * list right extensions and creates package managers
	 * 
	 */
	public Bash(){
		extensions = new LinkedList<String>();
		extensions.add(".sh");
		extensions.add(".bash");

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new PM_apt_get());
	}
}
