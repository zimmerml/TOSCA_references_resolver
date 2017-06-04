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
 * @author Yaroslav Script Artifact Template for packages
 */
public class RR_AnsibleArtifactTemplate {
	public static final String extension = "_IA.tosca";

	/**
	 * @author Yaroslav Script Artifact Template description
	 */
	@XmlRootElement(name = "tosca:Definitions")
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Definitions {

		@XmlElement(name = "tosca:Import", required = true)
		public RR_Import tImport;
		@XmlElement(name = "tosca:ArtifactTemplate", required = true)
		public ArtifactTemplate artifactTemplate;

		@XmlAttribute(name = "xmlns:tosca", required = true)
		public static final String tosca="http://docs.oasis-open.org/tosca/ns/2011/12";
		@XmlAttribute(name = "xmlns:winery", required = true)
		public static final String winery="http://www.opentosca.org/winery/extensions/tosca/2013/02/12";
		@XmlAttribute(name = "xmlns:ns1", required = true)
		public static final String ns1="http://www.eclipse.org/winery/model/selfservice";
		@XmlAttribute(name = "id", required = true)
		public String id;
		@XmlAttribute(name = "targetNamespace", required = true)
		public static final String targetNamespace="http://opentosca.org/artifacttemplates"; //TODO
		
		public Definitions() {
			artifactTemplate = new ArtifactTemplate();
			tImport = new RR_Import(RR_AnsibleArtifactType.Definitions.ArtifactType.targetNamespace,
					RR_AnsibleArtifactType.filename,"http://docs.oasis-open.org/tosca/ns/2011/12" );
		}

		public static class ArtifactTemplate {

			@XmlElement(name = "tosca:Properties", required = true)
			public Properties properties;
			
			@XmlElement(name = "tosca:ArtifactReferences", required = true)
			public ArtifactReferences artifactReferences;
			
			@XmlAttribute(name = "xmlns:tbt", required = true)
			public static final String tbt = RR_ScriptArtifactType.Definitions.ArtifactType.targetNamespace;
			@XmlAttribute(name = "id", required = true)
			public String id;
			@XmlAttribute(name = "type", required = true)
			public static final String type = RR_ScriptArtifactType.Definitions.ArtifactType.name;

			ArtifactTemplate() {
				artifactReferences = new ArtifactReferences();
				properties = new Properties();
			}

			public static class Properties {
				@XmlElement(name = "ns21:Properties", required = true)
				public Properties2 properties;
				
				public Properties(){
					properties = new Properties2();
				}

				public static class Properties2 {
					@XmlAttribute(name = "xmlns:ns21", required = true)
					public static final String ns21 = "http://opentosca.org/artifacttypes/propertiesdefinition/winery";

					@XmlAttribute(name = "xmlns", required = true)
					public static final String xmlns = "http://opentosca.org/artifacttypes/propertiesdefinition/winery";

					@XmlElement(name = "Playbook", required = true)
					public static final String playbook = "main.yml";
					@XmlElement(name = "Variables", required = true)
					public static final String variables = "";
				}
				
			}
			public static class ArtifactReferences {

				@XmlElement(name = "tosca:ArtifactReference", required = true)
				public ArtifactReference artifactReference;


				ArtifactReferences() {
					artifactReference = new ArtifactReference();
				}

				public static class ArtifactReference{
					@XmlAttribute(name = "reference", required = true)
					public String reference;
					ArtifactReference() {
					}
				}
				
			}
		}
	}

	/** Create template for package
	 * @param ch 
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void createAnsibleArtifact(CSAR_handler ch, String packet, String artifact)
			throws IOException, JAXBException {
		System.out.println("creating Ansible Template " );

		File temp = new File(ch.getFolder() + CSAR_handler.Definitions + getFileName(packet));
		if (temp.exists())
			temp.delete();
		temp.createNewFile();
		OutputStream output = new FileOutputStream(temp);

		JAXBContext jc = JAXBContext.newInstance(Definitions.class);

		Definitions template = new Definitions();
		
		template.id = "winery-defs-for_"+getIAName(packet);
		template.artifactTemplate.id = getIAName(packet);
		template.artifactTemplate.artifactReferences.artifactReference.reference = artifact;
		
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(template, output);
		
		ch.metaFile.addFileToMeta(CSAR_handler.Definitions + getFileName(packet), "application/vnd.oasis.tosca.definitions");

	
	}
	public static String getIAName(String packet){
		return "RR_"+packet+"_IA";
	}
	
	public static String getFileName(String packet){
		return "RR_"+packet+"_IA" + ".tosca";
	}
}
