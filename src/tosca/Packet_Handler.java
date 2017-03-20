package tosca;

/*-
 * #%L
 * TOSCA_RR
 * %%
 * Copyright (C) 2017 Stuttgart Uni, IAAS
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Abstract.Resolving;
import tosca.xml_definitions.RR_DependsOn;
import tosca.xml_definitions.RR_NodeType;
import tosca.xml_definitions.RR_PackageArtifactTemplate;
import tosca.xml_definitions.RR_PreDependsOn;
import tosca.xml_definitions.RR_ScriptArtifactTemplate;
import tosca.xml_definitions.RR_TypeImplementation;

//import tosca.xml_definitions.PackageTemplate;

public class Packet_Handler {

	static public final String Extension = ".deb";
	static public final String ScriptExtension = ".sh";

	// level of recursive dependency, to be checked
	private Integer Dependency = null;

	//list with renamed packages
	private HashMap<String,String> rename;
	
	// list with already downloaded packages
	private List<String> downloaded;
	
	// packets to be ignored
	private List<String> ignore;

	/**
	 * Constructor
	 */
	public Packet_Handler() {
		checkDependency();
		downloaded = new LinkedList<String>();
		ignore = new LinkedList<String>();
		rename = new HashMap<String,String>();
	}

	/**
	 * Initialize dependency level
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

	/**
	 * Downloads packet, public functions. Calls private recursive function
	 * 
	 * @param packet
	 *            to be download
	 * @param cr
	 *            CSAR handler
	 * @return
	 * @throws JAXBException
	 * @throws IOException
	 */
	public String getPacket(String packet, Control_references cr)
			throws JAXBException, IOException {
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
	public String getPacket(String packet, Control_references cr, int depth,
			List<String> listed) throws JAXBException, IOException {
		String sourceName = packet;
		if(rename.containsKey(packet))
			packet = rename.get(packet);
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
			packet = getSolution(packet);
			if (packet.equals(""))
				return "";
		}
		String newName = packet.replace(':', '_');

		String packets = "References_resolver/" + packet + "/" + packet
				+ Extension + " ";
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
					String dir_name = Resolver.folder + newName
							+ File.separator;
					File dir = new File(cr.getFolder() + dir_name);
					while (true) {
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
						Boolean found = false;
						for (File entry : folder.listFiles())
							if (entry.getName().endsWith(
									cr.getArchitecture().replaceAll(":", "")
											+ Extension)
									&& ((packet.contains(":") && entry
											.getName()
											.startsWith(
													packet.substring(0,
															packet.indexOf(':')))))
									|| (!packet.contains(":") && entry
											.getName().startsWith(packet))) {
								dir.mkdirs();
								entry.renameTo(new File(cr.getFolder()
										+ dir_name + newName + Extension));
								found = true;
								break;
							}
						if (found == false) {
							System.out.println("downloaded packet " + packet
									+ " not found");

							packet = getSolution(packet);
							if (packet.equals(""))
								break;
						} else {
							if(sourceName != packet)
								rename.put(sourceName, packet);
							cr.metaFile.addFileToMeta(dir_name + newName
									+ Extension + Extension, "application/deb");
							break;
						}
					}
				}
				if (cr.getResolving() == Resolving.ADDITION) {
					RR_NodeType.createNodeType(cr, newName);
					RR_ScriptArtifactTemplate.createScriptArtifact(cr, newName);
					RR_PackageArtifactTemplate.createPackageArtifact(cr, newName);
					RR_TypeImplementation.createNT_Impl(cr, newName);
				}
				// check dependency recursively
				for (String dPacket : dependensis) {
					if (cr.getResolving() == Resolving.ADDITION
							&& !ignore.contains(dPacket)) {
						cr.AddDependenciesPacket(newName,
								dPacket.replace(':', '_'), getDependencyType(packet,dPacket));
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

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		System.out.print("dependensis : ");
		String s = null;
		//TODO Predepends
		while ((s = stdInput.readLine()) != null) {
			String[] words = s.replaceAll("[;&<>]", "").split("\\s+");
			if (words.length == 3 && (words[1].equals("Depends:") ||words[1].equals("PreDepends:")) ) {
				depend.add(words[2]);
				System.out.print(words[2] + ",");
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
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + packet);
		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				proc.getErrorStream()));

		if ((stdError.readLine()) != null)
			return false;
		else
			return true;
	}

	/** Ask user for solution, by undownloadable package
	 * @param packet old package name
	 * @returnnew package name
	 */
	@SuppressWarnings("resource")
	private String getSolution(String packet) {
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
				return temp;
			else
				System.out.println("incorect name");
			break;
		case 3:
			ignore.add(packet);
			System.out.println("packet " + packet + " added to ignore list");
			return "";
		}
		return packet;
	}
	

	public static String getDependencyType(String source, String target) throws IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + source);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		System.out.print("dependensis : ");
		String s = null;
		//TODO Predepends
		while ((s = stdInput.readLine()) != null) {
			String[] words = s.replaceAll("[;&<>]", "").split("\\s+");
			if (words.length == 3 && words[1].equals("Depends:") && words[2].equals(target) ) {
				return RR_DependsOn.Name;
			}

			if (words.length == 3 && words[1].equals("PreDepends:") && words[2].equals(target) ) {
				return RR_PreDependsOn.Name;
			}
		}
		return null;
	}
}
