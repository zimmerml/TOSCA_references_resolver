package tosca.Languages.Bash;

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

import java.io.IOException;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import tosca.Control_references;
import tosca.Abstract.Language;
import tosca.Abstract.PacketManager;
import tosca.xml_definitions.RR_NodeType;
import tosca.xml_definitions.RR_PackageArtifactTemplate;
import tosca.xml_definitions.RR_ScriptArtifactTemplate;
import tosca.xml_definitions.RR_TypeImplementation;

/**
 * Script Language
 * 
 * @author Yaroslav
 *
 */
public final class Bash extends Language {

	/**
	 * Constructor list right extensions and creates package managers
	 * 
	 */
	public Bash() {
		Name = "Bash";
		extensions = new LinkedList<String>();
		extensions.add(".sh");
		extensions.add(".bash");
		
		created_packages = new LinkedList<String>();

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new PM_apt_get(this));
	}
	public void createTOSCA_Node(Control_references cr, String packet, String source) throws IOException, JAXBException{
		if(created_packages.contains(packet+"+"+source))
			return;
		created_packages.add(packet+"+"+source);
		RR_NodeType.createNodeType(cr, packet);
		RR_ScriptArtifactTemplate.createScriptArtifact(cr, packet);
		RR_PackageArtifactTemplate.createPackageArtifact(cr, packet);
		RR_TypeImplementation.createNT_Impl(cr, packet);
	}

}
