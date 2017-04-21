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
import tosca.xml_definitions.RR_NodeType;
import tosca.xml_definitions.RR_PackageArtifactTemplate;
import tosca.xml_definitions.RR_PreDependsOn;
import tosca.xml_definitions.RR_ScriptArtifactTemplate;
import tosca.xml_definitions.RR_TypeImplementation;

//import tosca.xml_definitions.PackageTemplate;

public class Packet_Handler {

	static public final String Extension = ".deb";
	static public final String ScriptExtension = ".sh";

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
		downloaded = new LinkedList<String>();
		ignore = new LinkedList<String>();
		rename = new HashMap<String,String>();
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
	public String getPacket(Language language,String packet, Control_references cr, String source)
			throws JAXBException, IOException {
		return getPacket(language, packet, cr, new LinkedList<String>(),source, 0);
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
	public String getPacket(Language language, String packet, Control_references cr,
			List<String> listed, String source, int depth) throws JAXBException, IOException {
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
		if (depth == 0) {
			if (packetExists(packet + cr.getArchitecture()))
				packet = packet + cr.getArchitecture();
		}
		while (!packetExists(packet)) {
			packet = getSolution(packet);
			if (packet.equals(""))
				return "";
		}
		String newName = ""; 
		String dir_name = "";
		String packets = "";
		Process proc;
		try {
			List<String> dependensis;
			dependensis = getDependensies(packet);
			// check if package was already downloaded
			if (!listed.contains(packet)) {
				listed.add(packet);
				if (!downloaded.contains(packet)) {
					downloaded.add(packet);
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
						for (File entry : new File("./").listFiles())
							if (entry.getName().endsWith(cr.getArchitecture().replaceAll(":", "")+ Extension)
									&& ((packet.contains(":") && entry.getName()
											.startsWith(
													packet.substring(0,
															packet.indexOf(':')))))
									|| (!packet.contains(":") && entry
											.getName().startsWith(packet))) {
								System.out.println("downloaded and found: "+entry.getName());
								newName = Utils.correctName(packet);
								packets = "References_resolver/" + packet + "/" + packet + Extension + " ";
								dir_name = Resolver.folder + newName
										+ File.separator;
								File dir = new File(cr.getFolder() + dir_name);
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
								return "";
						} else {
							if(sourceName != packet)
								rename.put(sourceName, packet);
							cr.metaFile.addFileToMeta(dir_name + newName
									+ Extension + Extension, "application/deb");
							break;
						}
					}
				}

				newName = Utils.correctName(packet);
				language.createTOSCA_Node(cr, newName,source);
				if(depth == 0)
					cr.AddDependenciesScript(Utils.correctName(source), newName);
				else
					cr.AddDependenciesPacket(Utils.correctName(source), newName, getDependencyType(source,packet));
			
				// check dependency recursively
				for (String dPacket : dependensis) {

					packets += getPacket(language, dPacket, cr, listed, packet, 1);
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
		if(rename.containsKey(packet))
			packet = rename.get(packet);
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

		if(rename.containsKey(packet))
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

	/** Ask user for solution, by undownloadable package
	 * @param packet old package name
	 * @returnnew package name
	 */
	@SuppressWarnings("resource")
	private String getSolution(String packet) {
		while(true){
			System.out.println("cant find packet: " + packet);
			System.out.println("1) rename");
			System.out.println("2) retry");
			System.out.println("3) ignore");
			if(packet.contains(":"))
				System.out.println("4) remove architecture");
			try{
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
				case 4:
					return packet.substring(0,packet.indexOf(':'));
				}
				return packet;
			}
			catch( InputMismatchException e){
				
			}
			
		}
	}
	

	public String getDependencyType(String source, String target) throws IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("apt-cache depends " + source);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));

		String s = null;
		String last = null;
		while ((s = stdInput.readLine()) != null) {
			String[] words = s.replaceAll("[;&<>]", "").split("\\s+");
			if (words.length == 3 && words[1].equals("Depends:") && words[2].equals(target) ) {
				return RR_DependsOn.Name;
			}

			if (words.length == 3 && words[1].equals("PreDepends:") && words[2].equals(target) ) {
				return RR_PreDependsOn.Name;
			}
			if (words.length == 2 && words[1].equals(target) ) {
				return last;
			}
			if (words.length > 1 && words[1].equals("Depends:")) {
				last = RR_DependsOn.Name;
			}
			if (words.length > 1 && words[1].equals("PreDepends:")) {
				last = RR_PreDependsOn.Name;
			}
		}
		if(rename.containsKey(source) && source != rename.get(source))
			return getDependencyType(rename.get(source),target);
		if(rename.containsKey(target) && target != rename.get(target))
			return getDependencyType(source,rename.get(target));
		return null;
	}
}
