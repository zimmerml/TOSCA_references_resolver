package TOSCA;

import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class PacketManager {
	protected String Name;
	public String getName(){
		return Name;
	}
	
	abstract void proceed(String filename,Control_references cr) throws FileNotFoundException, IOException;

}
