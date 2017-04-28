package tosca;

/*-
 * #%L
 * TOSCA_RR
 * %%
 * Copyright (C) 2017 Universität Stuttgart, IAAS
 * %%
 * Licensed under the Eclipse Public License v1.0 (the "License1") and the Apache License, Version 2.0 (the "License2");
 * you may not use this file except in compliance with the License1 or License2.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. 
 * You may obtain a copy of the License1 at
 * 
 *     https://eclipse.org/org/documents/epl-v10.php
 * 
 * You may obtain a copy of the License2 at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License2 is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License2 for the specific language governing permissions and
 * limitations under the License2.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Abstract.Language;
import tosca.Packet_Handler;

//unpack 
/**
 * @author jery
 * 
 */
public class Control_references {

	// input CSAR file name
	private String CSAR;

	// folder containing extracted files
	private String folder;

	// extracted files
	private List<String> files;

	// architecture of packages
	private String architecture;

	// Metafile description
	public MetaFile metaFile;

	// Download and proceed packets
	private Packet_Handler packet_handler;

	// Updates service templates
	private Topology_Handler service_template;

	public static final String ArchitectureFileName = "arch";
	public static final String Definitions = "Definitions/";

	/**
	 * Download and add packet to csar
	 * 
	 * @param packet
	 *            name to download
	 * @return
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void getPacket(Language language, String packet, String source)
			throws JAXBException, IOException {
		packet_handler.getPacket(language, packet, source);
	}

	/**
	 * Update Service Template
	 * 
	 * @param reference
	 *            to script, which downloads packet
	 * @param packet
	 *            to be added to TOSCA
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void AddDependenciesScript(String reference, String packet)
			throws JAXBException, IOException {
		service_template.addDependencyToScript(reference, packet);
	}

	/**
	 * Update service template
	 * 
	 * @param source
	 *            packet, which needs target packet
	 * @param target
	 *            new packet needed by source
	 * @throws JAXBException
	 * @throws IOException
	 */
	public void AddDependenciesPacket(String source, String target,
			String dependencyType) throws JAXBException, IOException {
		service_template.addDependencyToPacket(source, target, dependencyType);
	}

	/**
	 * init system
	 * 
	 * @param filename
	 *            CSAR archive
	 * @throws IOException
	 */
	public Control_references(String filename) throws FileNotFoundException,
			IOException {
		metaFile = new MetaFile();
		init(filename);
		packet_handler = new Packet_Handler(this);
		service_template = new Topology_Handler(this);
	}

	/**
	 * extract archive and read architecture
	 * 
	 * @param filename
	 *            CSAR archive
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void init(String filename) throws FileNotFoundException, IOException {
		if (filename == null)
			throw new NullPointerException();
		CSAR = filename;
		unpack();
		readArchitecture();
	}

	/**
	 * List extracted files
	 * 
	 * @return list with files
	 */
	public List<String> getFiles() {
		// List<String> fullFiles = new LinkedList<String>();
		// for (String s : files)
		// fullFiles.add(folder + s);
		return files;
	}

	/**
	 * Get folder containing extracted files
	 * 
	 * @return folder name
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * Unpack CSAR
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void unpack() throws FileNotFoundException, IOException {
		folder = CSAR + "_temp_references_resolver";
		File folderfile = new File(folder);
		folder = folderfile + File.separator;
		zip.delete(new File(folder));
		files = zip.unZipIt(CSAR, folder);
		metaFile.init(folder);
	}

	/**
	 * Pack changed CSAR back to zip
	 * 
	 * @param filename
	 *            target archive filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void pack(String filename) throws FileNotFoundException, IOException {
		metaFile.pack(folder);
		if (filename == null)
			throw new NullPointerException();
		zip.zipIt(filename, folder);
	}

	/**
	 * Get archive filename
	 * 
	 * @return archive filename
	 */
	public String getCSARname() {
		return CSAR;
	}

	/**
	 * Get current architecture
	 * 
	 * @return architecture
	 */
	public String getArchitecture() {
		return architecture;
	}

	/**
	 * reads Architecture from extracted data or from user input
	 * 
	 * @throws IOException
	 */
	// no need to close user input
	@SuppressWarnings("resource")
	public void readArchitecture() throws IOException {
		File arch = new File(folder + Resolver.folder + ArchitectureFileName);
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(arch));
			String line = br.readLine();
			br.close();
			if (line != null && !line.equals(""))
				architecture = line;
			else {
				new File(folder + Resolver.folder + ArchitectureFileName)
						.delete();
				throw new FileNotFoundException();
			}

		} catch (FileNotFoundException e) {
			new File(folder + Resolver.folder).mkdir();
			FileWriter bw = new FileWriter(arch);
			System.out.println("Please enter architecure.");
			System.out.println("Example: i386(default), amd64, arm, noarch.");
			System.out.print("architecture:");
			architecture = new Scanner(System.in).nextLine();
			if (architecture.equals(""))
				architecture = "i386";
			architecture = ":" + architecture;
			if (architecture.equals(":noarch"))
				architecture = "";
			bw.write(architecture);
			bw.close();
		}
		metaFile.addFileToMeta(Resolver.folder + ArchitectureFileName,
				"text/txt");
	}

	/**
	 * Set specific architecture
	 * 
	 * @param arch
	 * @throws IOException
	 */
	public void setArchitecture(String arch) throws IOException {
		if (arch == null)
			throw new NullPointerException();
		architecture = arch;

		// delete old file
		File fArch = new File(folder + Resolver.folder + ArchitectureFileName);
		fArch.delete();

		// create new file
		FileWriter bw = new FileWriter(fArch);
		bw.write(arch);
		bw.flush();
		bw.close();
	}

}
