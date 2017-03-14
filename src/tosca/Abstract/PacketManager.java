package tosca.Abstract;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import tosca.Control_references;

/**
 * Package manager used by language
 * 
 * @author jery
 *
 */
public abstract class PacketManager {

	// Name of manager
	static public String Name;

	/**
	 * Proceed given file
	 * 
	 * @param filename
	 *            file to proceed
	 * @param cr
	 *            CSAR manager
	 * @throws FileNotFoundException
	 * @throws IOExceptions
	 * @throws JAXBException
	 */
	public abstract void proceed(String filename, Control_references cr) throws FileNotFoundException,
			IOException, JAXBException;

	/** 
	 * Proceed given file with different source (like archive)
	 * @param filename
	 * @param cr
	 * @param source
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public abstract void proceed(String filename, Control_references cr, String source)
			throws FileNotFoundException, IOException, JAXBException;
}
