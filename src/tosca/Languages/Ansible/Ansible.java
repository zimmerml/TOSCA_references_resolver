package tosca.Languages.Ansible;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Control_references;
import tosca.Packet_Handler;
import tosca.Resolver;
import tosca.Utils;
import tosca.zip;
import tosca.Abstract.Language;
import tosca.Abstract.PacketManager;
import tosca.xml_definitions.RR_AnsibleArtifactTemplate;
import tosca.xml_definitions.RR_AnsibleTypeImplementation;
import tosca.xml_definitions.RR_NodeType;

public class Ansible extends Language {

	private HashMap<String, Ansible_setup> ansible_setup;

	public static class Ansible_setup {
		String config;
		String hosts;
		String connection;
		String become;
	}

	/**
	 * Constructor list right extensions and creates package managers
	 * 
	 */
	public Ansible(Control_references cr) {
		this.cr = cr;
		Name = "Ansible";
		extensions = new LinkedList<String>();
		extensions.add(".zip");
		extensions.add(".yml");

		ansible_setup = new HashMap<String, Ansible_setup>();
		created_packages = new LinkedList<String>();

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new Apt(this, cr));
	}

	/*
	 * Ansible files can be packed. need to unpack them and proceed separately
	 * (non-Javadoc)
	 * 
	 * @see tosca.Abstract.Language#proceed(tosca.Control_references)
	 */
	public void proceed(Control_references cr) throws FileNotFoundException,
	IOException, JAXBException {
		if (cr == null)
			throw new NullPointerException();
		for (String f : cr.getFiles())
			for (String suf : extensions)
				if (f.toLowerCase().endsWith(suf.toLowerCase())) {
					if (suf.equals(".zip")) {
						proceedZIP(f);
					} else
						proceed(f, f);
				}
	}

	/**
	 * proceed given file
	 * 
	 * @param filename
	 * @param cr
	 * @param source
	 *            of file, example - archive
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void proceed(String filename, String source)
			throws FileNotFoundException, IOException, JAXBException {
		for (PacketManager pm : packetManagers)
			pm.proceed(filename, source);
	}

	/**
	 * Handle ZIP package
	 * 
	 * @param zipfile
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	private void proceedZIP(String zipfile) throws FileNotFoundException,
	IOException, JAXBException {
		boolean isChanged = false;
		// String filename = new File(f).getName();
		String folder = new File(cr.getFolder() + zipfile).getParent()
				+ File.separator + "temp_RR_ansible_folder" + File.separator;
		List<String> files = zip.unZipIt(cr.getFolder() + zipfile, folder);
		for (String file : files)
			if (file.toLowerCase().endsWith("yml"))
				proceed(folder + file, zipfile);
		if (isChanged) {
			new File(cr.getFolder() + zipfile).delete();
			zip.zipIt(cr.getFolder() + zipfile, folder);
		}
		zip.delete(new File(folder));

	}

	/**
	 * Create Ansible setup to generate own ansible packages
	 * 
	 * @param source
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private void create_ansible_setup(String source) throws IOException {
		Ansible_setup setup = new Ansible_setup();
		setup.config = "";
		// i hope that everything what needed for ansible - correct config file.
		if (source.endsWith(".zip")) {
			String config = source.substring(0, source.length() - 4)
					+ File.separator + "ansible.cfg";
			if (!new File(config).exists()) {
				throw new FileNotFoundException(config + " not found");
			}
			List<String> lines = Files.readAllLines(Paths.get(config));
			for (String s : lines) {
				setup.config += (s) + "\r";
			}
		}
		System.out.println("Please, entrer hosts for " + source);
		setup.hosts = new Scanner(System.in).nextLine();
		System.out.println("Please, entrer connection for " + source);
		setup.connection = new Scanner(System.in).nextLine();
		System.out.println("Please, entrer become for " + source);
		setup.become = new Scanner(System.in).nextLine();
		ansible_setup.put(source, setup);
	}

	public String createTOSCA_Node(String packet, String source)
			throws IOException, JAXBException {

		String artifact_name = getNodeName(packet, source);
		if (created_packages.contains(packet + "+" + source))
			return artifact_name;
		created_packages.add(packet + "+" + source);
		if (!ansible_setup.containsKey(source))
			create_ansible_setup(source);
		Ansible_setup setup = ansible_setup.get(source);
		String file = Resolver.folder + packet + File.separator + artifact_name;
		String folder = cr.getFolder() + file + "_temp" + File.separator;
		new File(folder).mkdir();

		FileWriter file_writer = new FileWriter(
				new File(folder + "ansible.cfg"));
		file_writer.write(setup.config);
		file_writer.flush();
		file_writer.close();

		file_writer = new FileWriter(new File(folder + "main.yml"));
		file_writer.write("- name: install package\r");
		if (!setup.hosts.equals(""))
			file_writer.write("  hosts: " + setup.hosts + "\r");
		if (!setup.connection.equals(""))
			file_writer.write("  connection: " + setup.connection + "\r");
		if (!setup.become.equals(""))
			file_writer.write("  become: " + setup.become + "\r");
		file_writer
		.write("  tasks:\r    - name: install task\r      command: dpkg -i "
				+ packet + Packet_Handler.Extension + "\r");
		file_writer.flush();
		file_writer.close();

		new File(folder + "files").mkdir();
		Files.copy(
				Paths.get(cr.getFolder() + Resolver.folder + packet
						+ File.separator + packet + Packet_Handler.Extension),
						Paths.get(folder + "files" + File.separator + packet
								+ Packet_Handler.Extension));

		zip.zipIt(cr.getFolder() + file, folder);
		zip.delete(new File(folder));
		cr.metaFile.addFileToMeta(Resolver.folder + "ansible_properties.xsd",
				"text/xml");

		RR_NodeType.createNodeType(cr, artifact_name);
		RR_AnsibleArtifactTemplate
		.createAnsibleArtifact(cr, artifact_name, file);
		RR_AnsibleTypeImplementation.createNT_Impl(cr, artifact_name);
		return artifact_name;
	}

	public String getNodeName(String packet, String source) {
		return Utils.correctName(Name + "_" + packet + "_"
				+ source.replace("/", "_"));
	}

}
