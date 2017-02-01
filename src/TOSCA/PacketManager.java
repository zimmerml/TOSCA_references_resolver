package TOSCA;

import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class PacketManager {
	protected String Name;
	protected String architecture;
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}
	public String getName(){
		return Name;
	}
	
	abstract void proceed(String filename,Control_references cr) throws FileNotFoundException, IOException;

}
