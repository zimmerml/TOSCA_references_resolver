package TOSCA;

import java.io.FileNotFoundException;
import java.io.IOException;

/** Package manager used by language
 * @author jery
 *
 */
public abstract class PacketManager {
	
	//Name of manager
	static public String Name;
	
	/** Proceed given file
	 * @param filename file to proceed
	 * @param cr CSAR manager
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	abstract void proceed(String filename,Control_references cr) throws FileNotFoundException, IOException;

}
