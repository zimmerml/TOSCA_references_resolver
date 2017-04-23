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
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.Control_references;

public abstract class Language {

	// List of package managers supported by language
	protected List<PacketManager> packetManagers;

	// Extensions for this language
	protected List<String> extensions;

	// Language Name
	protected String Name;

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
	 * @param cr
	 *            CSAR manager
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void proceed(Control_references cr) throws FileNotFoundException,
			IOException, JAXBException {
		if (cr == null)
			throw new NullPointerException();
		for (String f : cr.getFiles())
			for (String suf : extensions)
				if (f.toLowerCase().endsWith(suf.toLowerCase()))
					for (PacketManager pm : packetManagers)
						pm.proceed(cr.getFolder() + f, cr, f);
	}

	public abstract String getNodeName(String packet, String source);
	
	public abstract String createTOSCA_Node(Control_references cr, String packet, String source) throws IOException, JAXBException;
}
