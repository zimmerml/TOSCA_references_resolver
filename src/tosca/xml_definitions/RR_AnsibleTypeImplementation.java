package tosca.xml_definitions;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import tosca.CSAR_handler;

/**
 * @author Yaroslav Template Implementation for packages
 */
public class RR_AnsibleTypeImplementation {
	public static final String extension = "_Impl.tosca";

	/**
	 * @author Yaroslav  Template Implementation description
	 */
	@XmlRootElement(name = "tosca:Definitions")
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Definitions {

		@XmlElement(name = "tosca:Import", required = true)
		public RR_Import import_script;
		@XmlElement(name = "tosca:Import", required = true)
		public RR_Import import_IA;
		@XmlElement(name = "tosca:NodeTypeImplementation", required = true)
		public NodeTypeImplementation nodeTypeImplementation;

		@XmlAttribute(name = "xmlns:tosca", required = true)
		public static final String tosca="http://docs.oasis-open.org/tosca/ns/2011/12";
		@XmlAttribute(name = "xmlns:winery", required = true)
		public static final String winery="http://www.opentosca.org/winery/extensions/tosca/2013/02/12";
		@XmlAttribute(name = "xmlns:ns1", required = true)
		public static final String ns1="http://www.eclipse.org/winery/model/selfservice";
		@XmlAttribute(name = "id", required = true)
		public String id;
		@XmlAttribute(name = "targetNamespace", required = true)
		public static final String targetNamespace="http://opentosca.org/nodetypeimplementations"; //TODO
		
		public Definitions() {
			nodeTypeImplementation = new NodeTypeImplementation();
			import_script = new RR_Import(RR_AnsibleArtifactType.Definitions.ArtifactType.targetNamespace,
					RR_AnsibleArtifactType.filename,"http://docs.oasis-open.org/tosca/ns/2011/12" );
			}

		
		public static class NodeTypeImplementation {

			@XmlElement(name = "tosca:ImplementationArtifacts", required = true)
			public ImplementationArtifacts implementationArtifacts;
			
			@XmlAttribute(name = "xmlns:ns0", required = true)
			public static final String ns0 = RR_NodeType.Definitions.targetNamespace;
			@XmlAttribute(name = "name", required = true)
			public String name;
			@XmlAttribute(name = "targetNamespace", required = true)
			public static final String targetNamespace = "http://opentosca.org/nodetypeimplementations";
			@XmlAttribute(name = "nodeType", required = true)
			public String nodeType;

			NodeTypeImplementation() {
				implementationArtifacts = new ImplementationArtifacts();
			}

			public static class ImplementationArtifacts {

				@XmlElement(name = "tosca:ImplementationArtifact", required = true)
				public ImplementationArtifact implementationArtifact;


				ImplementationArtifacts() {
					implementationArtifact = new ImplementationArtifact();
				}

				public static class ImplementationArtifact{
					@XmlAttribute(name = "xmlns:tbt", required = true)
					public static final String tbt = RR_AnsibleArtifactTemplate.Definitions.ArtifactTemplate.tbt;
					@XmlAttribute(name = "xmlns:ns6", required = true)
					public static final String ns6 = RR_AnsibleArtifactTemplate.Definitions.targetNamespace;
					@XmlAttribute(name = "name", required = true)
					public String name;
					@XmlAttribute(name = "interfaceName", required = true)
					public static final String interfaceName = RR_NodeType.Definitions.NodeType.Interfaces.Interface.name;
					@XmlAttribute(name = "operationName", required = true)
					public static final String operationName = RR_NodeType.Definitions.NodeType.Interfaces.Interface.Operation.name;
					@XmlAttribute(name = "artifactType", required = true)
					public static final String artifactType = "tbt:" + RR_AnsibleArtifactTemplate.Definitions.ArtifactTemplate.type;
					@XmlAttribute(name = "artifactRef", required = true)
					public String artifactRef;
					ImplementationArtifact() {
					}
				}
			}
		}
	}

	/** Create Type Implementation for my Node Type
	 * @param ch 
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void createNT_Impl(CSAR_handler ch,String packet)
			throws IOException, JAXBException {
		System.out.println("creating ansible Implementation");

		File temp = new File(ch.getFolder() + CSAR_handler.Definitions + getFileName(packet));
		if (temp.exists())
			temp.delete();
		temp.createNewFile();
		OutputStream output = new FileOutputStream(temp);

		JAXBContext jc = JAXBContext.newInstance(Definitions.class);

		Definitions template = new Definitions();
		template.id = "winery-defs-for_" + getTypeName(packet);
		template.import_IA = new RR_Import(RR_AnsibleArtifactTemplate.Definitions.targetNamespace,
				RR_AnsibleArtifactTemplate.getFileName(packet),"http://docs.oasis-open.org/tosca/ns/2011/12" );
		template.nodeTypeImplementation.name = getTypeName(packet);
		template.nodeTypeImplementation.nodeType = "ns0:" + RR_NodeType.getTypeName(packet);
		template.nodeTypeImplementation.implementationArtifacts.implementationArtifact.name = RR_AnsibleArtifactTemplate.getIAName(packet);
		template.nodeTypeImplementation.implementationArtifacts.implementationArtifact.artifactRef = "ns6:" + RR_AnsibleArtifactTemplate.getIAName(packet);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(template, output);
		ch.metaFile.addFileToMeta(CSAR_handler.Definitions + getFileName(packet), "application/vnd.oasis.tosca.definitions");
	}
	
	public static String getTypeName(String packet){
		return "RR_NT_"+packet + "_Impl";
	}
	
	public static String getFileName(String packet){
		return "RR_NT_"+packet + "_Impl" + ".tosca";
	}
}
