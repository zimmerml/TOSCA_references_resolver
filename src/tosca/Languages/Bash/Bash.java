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

import tosca.CSAR_handler;
import tosca.Utils;
import tosca.Abstract.Language;
import tosca.Abstract.PackageManager;
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
	public Bash(CSAR_handler new_ch) {
		ch = new_ch;
		Name = "Bash";
		extensions = new LinkedList<String>();
		extensions.add(".sh");
		extensions.add(".bash");
		
		created_packages = new LinkedList<String>();

		packetManagers = new LinkedList<PackageManager>();
		packetManagers.add(new PM_apt_get(this, ch));
		packetManagers.add(new PM_aptitude(this, ch));
	}
	
	/* (non-Javadoc)
	 * @see tosca.Abstract.Language#createTOSCA_Node(java.lang.String, java.lang.String)
	 */
	public String createTOSCA_Node(String packet, String source) throws IOException, JAXBException{
		if(created_packages.contains(packet+"+"+source))
			return packet;
		created_packages.add(packet+"+"+source);
		packet = getNodeName(packet, source);
		RR_NodeType.createNodeType(ch, packet);
		RR_ScriptArtifactTemplate.createScriptArtifact(ch, packet);
		RR_PackageArtifactTemplate.createPackageArtifact(ch, packet);
		RR_TypeImplementation.createNT_Impl(ch, packet);
		return packet;
	}
	
	/* (non-Javadoc)
	 * @see tosca.Abstract.Language#getNodeName(java.lang.String, java.lang.String)
	 */
	public String getNodeName(String packet, String source){
		return Utils.correctName(packet);
	}
}
