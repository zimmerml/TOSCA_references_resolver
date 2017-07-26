package tosca.Abstract;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.CSAR_handler;
import tosca.Utils;

public abstract class Language {

	// List of package managers supported by language
	protected List<PackageManager> packetManagers;

	// Extensions for this language
	protected List<String> extensions;

	// Language Name
	protected String Name;

	// To access package topology
	protected CSAR_handler ch;

	// List with already created packages
	protected List <String> created_packages;
	
	/**
	 * get Language name
	 * 
	 * @return
	 */
	public String getName() {
		return Name;
	}

	/**
	 * Get supported extensions
	 * 
	 * @return list with extensions
	 */
	public List<String> getExtensions() {
		return extensions;
	}

	/**
	 * Proceed file, transfer it to package managers
	 * 
	 * @param new_ch
	 *            CSAR manager
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void proceed() throws FileNotFoundException,
			IOException, JAXBException {
		if (ch == null)
			throw new NullPointerException();
		for (String f : ch.getFiles())
			for (String suf : extensions)
				if (f.toLowerCase().endsWith(suf.toLowerCase()))
				{
					List<String> packages = new LinkedList<String>();
					for (PackageManager pm : packetManagers)
						packages.addAll(pm.proceed(ch.getFolder() + f, f));
					if(packages.size() > 0 && ch.getResolving() == CSAR_handler.Resolving.Single){
						List<String> templist = new LinkedList<String>();
						for(String temp:packages)
							templist.add(Utils.correctName(temp));
						createTOSCA_Node(templist, f);
						ch.AddDependenciesScript(Utils.correctName(f), getNodeName(f));
					}
				}
	}

	/**	Generate node name for specific packages
	 * @param packet
	 * @param source
	 * @return
	 */
	public  String getNodeName(String packet, String source)
	{
		return Utils.correctName(Name + "_" + packet + "_"
				+ source.replace("/", "_"));
	}

	/**	Generate node name for specific packages
	 * @param source
	 * @return
	 */
	public String getNodeName(String source){	
		return Utils.correctName(Name + "_for_" + source.replace("/", "_"));
		
	}
	
	
	/**	Generate Node for TOSCA Topology
	 * @param packet
	 * @param source
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public abstract String createTOSCA_Node(String packet, String source) throws IOException, JAXBException;
	public abstract String createTOSCA_Node(List<String> packages, String source) throws IOException, JAXBException;
}
