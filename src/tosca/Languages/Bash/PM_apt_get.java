package tosca.Languages.Bash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Control_references;
import tosca.Resolver;
import tosca.Utils;
import tosca.Abstract.PacketManager;
import tosca.xml_definitions.PackageTemplate;

public final class PM_apt_get extends PacketManager {

	// package manager name
	static public final String Name = "apt-get";
	static public final String extension = ".deb";

	// level of recursive dependency, to be checked
	static private int Dependency = 2;

	// list with already downloaded packages
	private List<String> downloaded;

	/**
	 * Constructor Initialize list and ask for dependency level
	 */
	@SuppressWarnings("resource")
	public PM_apt_get() {
		downloaded = new LinkedList<String>();
		System.out.print("enter Dependenscy level for apt-get:");
		Dependency = new Scanner(System.in).nextInt();
		// Fully resolving
		if (Dependency < 0)
			Dependency = 999;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TOSCA.PacketManager#proceed(java.lang.String,
	 * TOSCA.Control_references)
	 */
	protected void proceed(String filename, Control_references cr)
			throws IOException, JAXBException {
		String prefix = "";
		for (int i = 0; i < Utils.getPathLength(filename) - 1; i++)
			prefix = prefix + "../";
		if (cr == null)
			throw new NullPointerException();
		System.out.println(Name + " proceed " + filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		boolean isChanged = false;
		String line = null;
		String newFile = "";
		while ((line = br.readLine()) != null) {
			// split string to words
			String[] words = line.replaceAll("[;&]", "").split("\\s+");
			// skip space at the beginning of string
			int i = 0;
			if (words[i].equals(""))
				i = 1;
			// look for apt-get
			if (words.length >= 1 + i && words[i].equals("apt-get")) {
				// apt-get found
				if (words.length >= 3 + i && words[1 + i].equals("install")) {
					// replace "apt-get install" by "dpkg -i"
					System.out.println("apt-get found:" + line);
					isChanged = true;
					for (int packet = 2 + i; packet < words.length; packet++) {
						System.out.println("packet: " + words[packet]);
						newFile += "dpkg -i ";
						for (String p : getPacket(words[packet], cr,
								Dependency, new LinkedList<String>()).split(
								"\\s+"))
							newFile += prefix + p + " ";
						newFile += "\n";
					}
				} else {
					// comment apt-get
					newFile += "#//References resolver//" + line + '\n';
				}
			} else
				newFile += line + '\n';
		}
		br.close();
		if (isChanged) {
			// references found, need to replace file
			// delete old
			File file = new File(filename);
			file.delete();

			// create new file
			FileWriter wScript = new FileWriter(file);
			wScript.write(newFile, 0, newFile.length());
			wScript.close();
		}
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
	private String getPacket(String packet, Control_references cr, int depth,
			List<String> listed) throws JAXBException {
		// if package is already listed: nothing to do
		if (listed.contains(packet))
			return "";
		// if this is the first call of recursive function, we need to add
		// architecture to package
		if (depth == Dependency)
			packet = packet + ":" + cr.getArchitecture();

		String packets = "References_resolver/Bash/" + packet + "/" + packet
				+ ".deb" + " ";
		File folder = new File("./");
		Process proc;
		try {

			List<String> dependensis;
			if (depth > 0)
				dependensis = getDependensies(packet);
			else
				dependensis = new LinkedList<String>();
			// check if package was already downloaded
			if (!downloaded.contains(packet))
				;
			{
				// "apt-get download" downloads only to current folder
				System.out.println("apt-get downloading " + packet);
				proc = Runtime.getRuntime().exec("apt-get download " + packet);
				proc.waitFor();
				System.out.println("done");
				// need to move package to right folder
				Boolean found = false;
				String dir_name = Resolver.folder + Bash.Name + File.separator
						+ packet + File.separator;
				File dir = new File(cr.getFolder() + dir_name);
				for (File entry : folder.listFiles())
					if (entry.getName().endsWith(cr.getArchitecture() + ".deb")
							&& ((packet.contains(":") && entry.getName()
									.startsWith(
											packet.substring(0,
													packet.indexOf(':')))))
							|| (!packet.contains(":") && entry.getName()
									.startsWith(packet))) {
						dir.mkdirs();
						entry.renameTo(new File(cr.getFolder() + dir_name
								+ packet + extension));
						listed.add(packet);
						downloaded.add(packet);
						found = true;
						break;
					}
				if (found == false)
					System.out.println("downloaded packet " + packet
							+ " not found");

				List<String> named_dep = new LinkedList<String>();
				for (String dep : dependensis)
					named_dep.add(Bash.Name + "_" + Name + "_" + dep);
				String fullPacketName = Bash.Name + "_" + Name + "_" + packet;
				PackageTemplate.createPackageTemplate(cr, Resolver.folder
						+ Bash.Name + "/" + packet + "/", named_dep,
						fullPacketName);

				cr.metaFile.addFileToMeta(dir_name + fullPacketName
						+ PackageTemplate.extension, "text/xml");
				cr.metaFile.addFileToMeta(dir_name + packet + extension,
						"application/deb");
			}
			// check dependency recursively
			for (String dPacket : dependensis)
				packets += getPacket(dPacket, cr, depth - 1, listed);

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

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		System.out.print("depends for " + packet + ": ");
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
}
