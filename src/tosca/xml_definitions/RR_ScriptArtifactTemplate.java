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

import tosca.Control_references;
import tosca.Resolver;

/**
 * @author Yaroslav Script Artifact Template for packages
 */
public class RR_ScriptArtifactTemplate {
	public static final String extension = "_IA.tosca";

	/**
	 * @author Yaroslav Script Artifact Template description
	 */
	@XmlRootElement(name = "tosca:Definitions")
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Definitions {

		@XmlElement(name = "tosca:Import", required = true)
		public Import tImport;
		@XmlElement(name = "ArtifactTemplate", required = true)
		public ArtifactTemplate artifactTemplate;

		@XmlAttribute(name = "xmlns:tosca", required = true)
		public static final String tosca="http://docs.oasis-open.org/tosca/ns/2011/12";
		@XmlAttribute(name = "xmlns:winery", required = true)
		public static final String winery="http://www.opentosca.org/winery/extensions/tosca/2013/02/12";
		@XmlAttribute(name = "xmlns:ns1", required = true)
		public static final String ns1="http://www.eclipse.org/winery/model/selfservice";
		@XmlAttribute(name = "id", required = true)
		public static final String id = "winery_RR_Script_Template";
		@XmlAttribute(name = "targetNamespace", required = true)
		public static final String targetNamespace="http://opentosca.org/artifacttemplates"; //TODO
		
		public Definitions() {
			artifactTemplate = new ArtifactTemplate();
			tImport = new Import(RR_ScriptArtifactType.Definitions.ArtifactType.targetNamespace,
					RR_ScriptArtifactType.filename,"http://docs.oasis-open.org/tosca/ns/2011/12" );
		}

		public static class ArtifactTemplate {

			@XmlElement(name = "tosca:ArtifactReferences", required = true)
			public ArtifactReferences artifactReferences;
			
			@XmlAttribute(name = "xmlns:tbt", required = true)
			public static final String tbt = RR_ScriptArtifactType.Definitions.ArtifactType.targetNamespace;
			@XmlAttribute(name = "id", required = true)
			public static final String id = "RR_ScriptTemplate";
			@XmlAttribute(name = "type", required = true)
			public static final String type = RR_ScriptArtifactType.Definitions.ArtifactType.name;

			ArtifactTemplate() {
				artifactReferences = new ArtifactReferences();
			}

			public static class ArtifactReferences {

				@XmlElement(name = "tosca:ArtifactReference", required = true)
				public ArtifactReference artifactReference;


				ArtifactReferences() {
					artifactReference = new ArtifactReference();
				}

				public static class ArtifactReference{
					@XmlAttribute(name = "reference", required = true)
					public static final String reference = script;
					ArtifactReference() {
					}
				}
				
			}
		}
	}
	// output filename
	public static final String filename = "RR_Script_Template.tosca";
	public static final String script = Resolver.folder + "RR_Script.sh";	

	/** Create template for package
	 * @param cr 
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void init(Control_references cr)
			throws IOException, JAXBException {
		System.out.println("creating Script Template " );

		File temp = new File(cr.getFolder() + Control_references.Definitions + filename);
		if (temp.exists())
			temp.delete();
		temp.createNewFile();
		OutputStream output = new FileOutputStream(temp);

		JAXBContext jc = JAXBContext.newInstance(Definitions.class);

		Definitions template = new Definitions();
		
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(template, output);
		cr.metaFile.addFileToMeta(Control_references.Definitions + filename, "application/vnd.oasis.tosca.definitions");
	}

}
