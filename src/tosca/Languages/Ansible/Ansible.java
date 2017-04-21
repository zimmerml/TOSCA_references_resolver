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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.Control_references;
import tosca.Utils;
import tosca.zip;
import tosca.Abstract.Language;
import tosca.Abstract.PacketManager;
import tosca.xml_definitions.RR_NodeType;
import tosca.xml_definitions.RR_PackageArtifactTemplate;
import tosca.xml_definitions.RR_ScriptArtifactTemplate;
import tosca.xml_definitions.RR_TypeImplementation;

public class Ansible extends Language {

	/**
	 * Constructor list right extensions and creates package managers
	 * 
	 */
	public Ansible() {
		Name = "Ansible";
		extensions = new LinkedList<String>();
		extensions.add(".zip");
		extensions.add(".yml");

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new Apt(this));
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
						boolean isChanged = false;
						// String filename = new File(f).getName();
						String folder = new File(f).getParent()
								+ File.separator;
						List<String> files = zip.unZipIt(cr.getFolder() + f,
								folder);
						for (String file : files)
							if (file.toLowerCase().endsWith("yml"))
								proceed(folder + file, cr, f);
						if (isChanged) {
							new File(cr.getFolder() + f).delete();
							zip.zipIt(cr.getFolder() + f, folder);
						}
						//TODO zip.delete(new File(folder));
					} else
						proceed(f, cr, f);
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
	public void proceed(String filename, Control_references cr, String source)
			throws FileNotFoundException, IOException, JAXBException {
		for (PacketManager pm : packetManagers)
			pm.proceed(filename, cr, source);
	}
	
	

	public void createTOSCA_Node(Control_references cr, String packet, String source) throws IOException, JAXBException{
		if(created_packages.contains(packet+"+"+source))
			return;
		created_packages.add(packet+"+"+source);
		String newName = Utils.correctName(packet);
		RR_NodeType.createNodeType(cr, newName);
		RR_ScriptArtifactTemplate.createScriptArtifact(cr, newName);
		RR_PackageArtifactTemplate.createPackageArtifact(cr, newName);
		RR_TypeImplementation.createNT_Impl(cr, newName);
	}

}
