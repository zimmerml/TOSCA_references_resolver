package TOSCA;

import java.util.LinkedList;

public final class Bash extends Language {

	public Bash(String architecture){
		Name = "Bash";
		this.architecture = architecture;
		extensions = new LinkedList<String>();
		extensions.add(".sh");
		extensions.add(".bash");

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new PM_apt_get(architecture));
	}
}
