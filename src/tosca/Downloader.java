package tosca;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

//import tosca.xml_definitions.PackageTemplate;

public class Downloader {

	static public final String extension = ".deb";

	// level of recursive dependency, to be checked
	private Integer Dependency = null;

	// list with already downloaded packages
	private List<String> downloaded;

	public Downloader(){
		checkDependency();
		downloaded = new LinkedList<String>();
	}

	@SuppressWarnings("resource")
	private void checkDependency()
	{
		if(Dependency != null)
			return;
		System.out.print("enter Dependenscy level for apt-get:");
		Dependency = new Scanner(System.in).nextInt();
		// Fully resolving
		if (Dependency < 0)
			Dependency = 999;
	}

	public String getPacket(String packet, Control_references cr) throws JAXBException {
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
	 */
	public String getPacket(String packet, Control_references cr, int depth,
			List<String> listed) throws JAXBException {
		System.out.println("Get packet: " + packet);
		// if package is already listed: nothing to do
		if (listed.contains(packet))
			return "";
		// if this is the first call of recursive function, we need to add
		// architecture to package
		if (depth == Dependency)
			packet = packet + ":" + cr.getArchitecture();

		String packets = "References_resolver/" + packet + "/" + packet
				+ extension + " ";
		File folder = new File("./");
		Process proc;
		try {
			List<String> dependensis;
			if (depth > 0)
				dependensis = getDependensies(packet,listed);
			else
				dependensis = new LinkedList<String>();
			// check if package was already downloaded
			if(!listed.contains(packet)){	
				listed.add(packet);
				if (!downloaded.contains(packet))
				{
					downloaded.add(packet);
					// "apt-get download" downloads only to current folder
					System.out.println("apt-get download " + packet);
					Runtime rt = Runtime.getRuntime();
					proc = rt.exec("apt-get download " + packet);
	
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(
							proc.getInputStream()));
	
					BufferedReader stdError = new BufferedReader(new 
							InputStreamReader(proc.getErrorStream()));
	
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
					String dir_name = Resolver.folder + packet + File.separator;
					File dir = new File(cr.getFolder() + dir_name);
					for (File entry : folder.listFiles())
						if (entry.getName().endsWith(cr.getArchitecture() + extension)
								&& ((packet.contains(":") && entry.getName()
										.startsWith(packet.substring(0,	packet.indexOf(':')))))
										|| (!packet.contains(":") && entry.getName()
												.startsWith(packet))) {
							dir.mkdirs();
							entry.renameTo(new File(cr.getFolder() + dir_name
									+ packet + extension));
							found = true;
							break;
						}
					if (found == false)
						System.out.println("downloaded packet " + packet
								+ " not found");
					switch(cr.getResolving()){
					case ADDITION:
						break;
					case EXPANDING:
						break;
					default:
						break;
					
					}
					//				List<String> named_dep = new LinkedList<String>();
					//				for (String dep : dependensis)
					//					named_dep.add(dep);
					//				String fullPacketName = packet;
					//				PackageTemplate.createPackageTemplate(cr, Resolver.folder
					//						+ packet + "/", named_dep,
					//						fullPacketName); //TODO
	
					//				cr.metaFile.addFileToMeta(dir_name + fullPacketName
					//						+ PackageTemplate.extension, "text/xml");//TODO
					cr.metaFile.addFileToMeta(dir_name + packet + extension,
							"application/deb");
				}
			// check dependency recursively
			for (String dPacket : dependensis)
				packets += getPacket(dPacket, cr, depth - 1, listed);
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
	private List<String> getDependensies(String packet, List<String> listed) throws IOException {
		List<String> depend = new LinkedList<String>();
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + packet);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		System.out.print("dependensis : ");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			String[] words = s.replaceAll("[;&<>]", "").split("\\s+");
			if (words.length == 3 && words[1].equals("Depends:")) {
				if(!listed.contains(words[2]))
					depend.add(words[2]);
				System.out.print(words[2] + ",");
			}
		}
		System.out.println("");
		return depend;
	}

}
