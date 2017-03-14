package tosca;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Abstract.Resolving;
import tosca.xml_definitions.RR_PackageArtifactTemplate;

//import tosca.xml_definitions.PackageTemplate;

public class Packet_Handler {

	static public final String Extension = ".deb";
	static public final String ScriptExtension = ".sh";

	// level of recursive dependency, to be checked
	private Integer Dependency = null;

	// list with already downloaded packages
	private List<String> downloaded;
	//packets to be ignored
	private List<String> ignore;

	/**
	 *  Constructor
	 */
	public Packet_Handler() {
		checkDependency();
		downloaded = new LinkedList<String>();
		ignore = new LinkedList<String>();
	}

	/**
	 *  Initialize dependency level
	 */
	@SuppressWarnings("resource")
	private void checkDependency() {
		if (Dependency != null)
			return;
		System.out.print("enter Dependenscy level for apt-get:");
		Dependency = new Scanner(System.in).nextInt();
		// Fully resolving
		if (Dependency < 0)
			Dependency = 999;
	}

	/**		Downloads packet, public functions. Calls private recursive function
	 * @param packet to be download
	 * @param cr CSAR handler
	 * @return
	 * @throws JAXBException
	 * @throws IOException
	 */
	public String getPacket(String packet, Control_references cr) throws JAXBException, IOException {
		return getPacket(packet, cr, Dependency, new LinkedList<String>());
	}

	/**
	 * Download package and check its dependency
	 * 
	 * @param packet
	 *            package name
	 * @param cr
	 *            CSAR manager
	 * @param depth
	 *            dependency level to be checked
	 * @param listed
	 *            list with already included packages
	 * @return list of packages
	 * @throws JAXBException
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public String getPacket(String packet, Control_references cr, int depth, List<String> listed)
			throws JAXBException, IOException {
		System.out.println("Get packet: " + packet);
		// if package is already listed: nothing to do
		if (listed.contains(packet) || ignore.contains(packet))
			return "";
		// if this is the first call of recursive function, we need to add
		// architecture to package
		// but some packages are multyarchitecture, need to check it.
		if (depth == Dependency) {
			if (packetExists(packet + cr.getArchitecture()))
				packet = packet + cr.getArchitecture();
		}
		while (!packetExists(packet)) {

			// Fault during download
			System.out.println("cant find packet: " + packet);
			System.out.println("1) rename");
			System.out.println("2) retry");
			System.out.println("3) ignore");

			int action = new Scanner(System.in).nextInt();
			switch (action) {
			case 1:
				System.out.print("Enter new name: ");
				String temp = new Scanner(System.in).nextLine();
				if (temp != null && !temp.equals(""))
					packet = temp;
				else
					System.out.println("incorect name");
				break;
			case 3:
				ignore.add(packet);
				System.out.println("packet " + packet + " added to ignore list");
				return "";
			default:
				break;
			}
		}
		String newName = packet.replace(':', '_');

		String packets = "References_resolver/" + packet + "/" + packet + Extension + " ";
		File folder = new File("./");
		Process proc;
		try {
			List<String> dependensis;
			if (depth > 0)
				dependensis = getDependensies(packet);
			else
				dependensis = new LinkedList<String>();
			// check if package was already downloaded
			if (!listed.contains(packet)) {
				listed.add(packet);
				if (!downloaded.contains(packet)) {
					downloaded.add(packet);
					// "apt-get download" downloads only to current folder
					System.out.println("apt-get download " + packet);
					Runtime rt = Runtime.getRuntime();
					proc = rt.exec("apt-get download " + packet);

					BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

					BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

					String s = null;
					while ((s = stdInput.readLine()) != null) {
						System.out.print(s);
					}

					// read any errors from the attempted command
					System.out.println("Errors:\n");
					while ((s = stdError.readLine()) != null) {
						System.out.println(s);
					}
					proc.waitFor();
					System.out.println("done");
					// need to move package to right folder
					Boolean found = false;
					String dir_name = Resolver.folder + newName + File.separator;
					File dir = new File(cr.getFolder() + dir_name);
					for (File entry : folder.listFiles())
						if (entry.getName().endsWith(cr.getArchitecture().replaceAll(":", "") + Extension)
								&& ((packet.contains(":") && entry.getName().startsWith(
										packet.substring(0, packet.indexOf(':')))))
								|| (!packet.contains(":") && entry.getName().startsWith(packet))) {
							dir.mkdirs();
							entry.renameTo(new File(cr.getFolder() + dir_name + newName + Extension));
							found = true;
							break;
						}
					if (found == false)
						System.out.println("downloaded packet " + packet + " not found");
					cr.metaFile.addFileToMeta(dir_name + packet + Extension, "application/deb");
				}
				// check dependency recursively
				for (String dPacket : dependensis) {
					if (cr.getResolving() == Resolving.ADDITION && !ignore.contains(dPacket)) {
						cr.AddDependenciesPacket(newName, dPacket.replace(':', '_'));
						RR_PackageArtifactTemplate.createPackageTemplate(cr, newName);
					}
					packets += getPacket(dPacket, cr, depth - 1, listed);
				}
			}

		} catch (IOException e) {
			System.out.println("Download" + packet + "failed");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Download" + packet + "failed");
			e.printStackTrace();
		}
		return packets;
	}

	/**
	 * Get dependency list for package
	 * 
	 * @param packet
	 *            , package to be checked
	 * @return list with depended packages
	 * @throws IOException
	 */
	private List<String> getDependensies(String packet) throws IOException {
		List<String> depend = new LinkedList<String>();
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + packet);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		System.out.print("dependensis : ");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			String[] words = s.replaceAll("[;&<>]", "").split("\\s+");
			if (words.length == 3 && words[1].equals("Depends:")) {
				depend.add(words[2]);
				System.out.print(words[2] + ",");
			}
		}
		System.out.println("");
		return depend;
	}

	/**	Checks if packet can be download
	 * @param packet to check
	 * @return
	 * @throws IOException
	 */
	private boolean packetExists(String packet) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + packet);
		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		if ((stdError.readLine()) != null)
			return false;
		else
			return true;
	}
}
