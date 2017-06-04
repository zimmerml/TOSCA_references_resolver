package tosca;

/*-
 * #%L
 * TOSCA_RR
 * %%
 * Copyright (C) 2017 Stuttgart Uni, IAAS
 * %%
 * Licensed under the Eclipse Public License v1.0 and Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Abstract.Language;
import tosca.xml_definitions.RR_DependsOn;
import tosca.xml_definitions.RR_PreDependsOn;

//import tosca.xml_definitions.PackageTemplate;

public class Package_Handler {

	static public final String Extension = ".deb";
	static public final String ScriptExtension = ".sh";

	// list with renamed packages
	private HashMap<String, String> rename;

	// list with already downloaded packages
	private List<String> downloaded;

	// packets to be ignored
	private List<String> ignore;

	private CSAR_handler ch;

	/**
	 * Constructor
	 */
	public Package_Handler(CSAR_handler new_ch) {
		downloaded = new LinkedList<String>();
		ignore = new LinkedList<String>();
		rename = new HashMap<String, String>();
		ch = new_ch;
	}

	/**
	 * Downloads packet, public functions. Calls private recursive function
	 * @param language
	 * @param packet to be download
	 * @param source
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void getPacket(Language language, String packet, String source)
			throws JAXBException, IOException {
		getPacket(language, packet, new LinkedList<String>(), source, source);
	}

	/**
	 * @param language
	 * @param packet
	 * @param listed
	 * @param source
	 * @param sourcefile
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void getPacket(Language language, String packet,
			List<String> listed, String source, String sourcefile)
					throws JAXBException, IOException {
		if (rename.containsKey(packet))
			packet = rename.get(packet);
		System.out.println("Get packet: " + packet);
		// if package is already listed: nothing to do
		if (listed.contains(packet) || ignore.contains(packet))
			return;
		// if this is the first call of recursive function, we need to add
		// architecture to package
		// but some packages are multyarchitecture, need to check it.
		if (source.equals(sourcefile)) {
			if (packetExists(packet + ch.getArchitecture()))
				packet = packet + ch.getArchitecture();
		}
		while (!packetExists(packet)) {
			packet = getSolution(packet);
			if (packet.equals(""))
				return;
		}
		String newName;
		List<String> dependensis;
		dependensis = getDependensies(packet);
		// check if package was already downloaded
		if (listed.contains(packet))
			return;
		listed.add(packet);
		if (!downloaded.contains(packet)) {
			downloaded.add(packet);
			packet = downloadPackage(packet);
			if (packet.equals(""))
				return;
		}
		newName = Utils.correctName(packet);
		String nodename = language.createTOSCA_Node(newName, sourcefile);
		if (source.equals(sourcefile))
			ch.AddDependenciesScript(Utils.correctName(source), nodename);
		else
			ch.AddDependenciesPacket(
					language.getNodeName(source, sourcefile),
					nodename, getDependencyType(source, packet));

		// check dependency recursively
//		if (source.equals(sourcefile)) // TODO
			for (String dPacket : dependensis) {

				getPacket(language, dPacket, listed, packet, sourcefile);
			}

		return;
	}

	private String downloadPackage(String packet) {
		String sourceName = packet;
		Boolean downloaded = false;
		String dir_name = "";
		String newName;
		Process proc;
		while (!downloaded) {
			try {
				// "apt-get download" downloads only to current folder
				System.out.println("apt-get download " + packet);
				Runtime rt = Runtime.getRuntime();
				proc = rt.exec("apt-get download " + packet);

				BufferedReader stdInput = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));

				BufferedReader stdError = new BufferedReader(
						new InputStreamReader(proc.getErrorStream()));

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
				for (File entry : new File("./").listFiles())
					if (file_downloaded(packet, entry)) {
						System.out.println("downloaded and found: "
								+ entry.getName());
						newName = Utils.correctName(packet);
						dir_name = Resolver.folder + newName + File.separator;
						new File(ch.getFolder() + dir_name).mkdirs();
						entry.renameTo(new File(ch.getFolder() + dir_name
								+ newName + Extension));
						downloaded = true;
						if (sourceName != packet)
							rename.put(sourceName, packet);
						ch.metaFile.addFileToMeta(dir_name + newName
								+ Extension, "application/deb");
						break;
					}
				if (downloaded == false) {
					System.out.println("downloaded packet " + packet
							+ " not found");

					packet = getSolution(packet);
					if (packet.equals(""))
						return "";
				}

			} catch (IOException e) {
				System.out.println("Download" + packet + "failed");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println("Download" + packet + "failed");
				e.printStackTrace();
			}
		}
		return packet;
	}

	private Boolean file_downloaded(String packet, File file) {
		return file.getName().endsWith(
				ch.getArchitecture().replaceAll(":", "") + Extension)
				&& ((packet.contains(":") && file.getName().startsWith(
						packet.substring(0, packet.indexOf(':')))))
						|| (!packet.contains(":") && file.getName().startsWith(packet));
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
		if (rename.containsKey(packet))
			packet = rename.get(packet);
		List<String> depend = new LinkedList<String>();
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + packet);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		System.out.print("dependensis : ");
		String s = null;
		// TODO Predepends
		while ((s = stdInput.readLine()) != null) {
			// TOCHECK
			String[] words = s.replaceAll("[;&]", "").split("\\s+");
			if (words.length == 3
					&& (words[1].equals("Depends:") || words[1]
							.equals("PreDepends:"))) {
				String to_add;
				if (words[2].startsWith("<") && words[2].endsWith(">"))
					to_add = stdInput.readLine().replaceAll("\\s+", "");
				else
					to_add = words[2];
				depend.add(to_add);
				System.out.print(to_add + ",");
			}
		}
		System.out.println("");
		return depend;
	}

	/**
	 * Checks if packet can be download
	 * 
	 * @param packet
	 *            to check
	 * @return
	 * @throws IOException
	 */
	private boolean packetExists(String packet) throws IOException {

		if (rename.containsKey(packet))
			packet = rename.get(packet);
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + packet);
		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				proc.getErrorStream()));

		if ((stdError.readLine()) != null)
			return false;
		else
			return true;
	}

	/**
	 * Ask user for solution, by undownloadable package
	 * 
	 * @param packet
	 *            old package name
	 * @returnnew package name
	 */
	@SuppressWarnings("resource")
	private String getSolution(String packet) {
		while (true) {
			System.out.println("cant find packet: " + packet);
			System.out.println("1) rename");
			System.out.println("2) retry");
			System.out.println("3) ignore");
			if (packet.contains(":"))
				System.out.println("4) remove architecture");
			try {
				int action = new Scanner(System.in).nextInt();
				switch (action) {
				case 1:
					System.out.print("Enter new name: ");
					String temp = new Scanner(System.in).nextLine();
					if (temp != null && !temp.equals(""))
						return temp;
					else
						System.out.println("incorect name");
					break;
				case 3:
					ignore.add(packet);
					System.out.println("packet " + packet
							+ " added to ignore list");
					return "";
				case 4:
					return packet.substring(0, packet.indexOf(':'));
				}
				return packet;
			} catch (InputMismatchException e) {

			}

		}
	}

	public String getDependencyType(String source, String target)
			throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + source);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		String s = null;
		String last = null;
		while ((s = stdInput.readLine()) != null) {
			String[] words = s.replaceAll("[;&<>]", "").split("\\s+");
			if (words.length == 3 && words[1].equals("Depends:")
					&& words[2].equals(target)) {
				return RR_DependsOn.Name;
			}

			if (words.length == 3 && words[1].equals("PreDepends:")
					&& words[2].equals(target)) {
				return RR_PreDependsOn.Name;
			}
			if (words.length == 2 && words[1].equals(target)) {
				return last;
			}
			if (words.length > 1 && words[1].equals("Depends:")) {
				last = RR_DependsOn.Name;
			}
			if (words.length > 1 && words[1].equals("PreDepends:")) {
				last = RR_PreDependsOn.Name;
			}
		}
		if (rename.containsKey(source) && source != rename.get(source))
			return getDependencyType(rename.get(source), target);
		if (rename.containsKey(target) && target != rename.get(target))
			return getDependencyType(source, rename.get(target));
		return null;
	}
}
