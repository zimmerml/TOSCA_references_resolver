package tosca.xml_definitions;

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

/**
 * @author Yaroslav Template Implementation for packages
 */
public class RR_TemplateImplementation {
	public static final String extension = "_Impl.tosca";

	/**
	 * @author Yaroslav  Template Implementation description
	 */
	@XmlRootElement(name = "tosca:Definitions")
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Definitions {

		@XmlElement(name = "tosca:Import", required = true)
		public Import import_script;
		@XmlElement(name = "tosca:Import", required = true)
		public Import import_package;
		@XmlElement(name = "tosca:Import", required = true)
		public Import import_IA;
		@XmlElement(name = "tosca:Import", required = true)
		public Import import_DA;
		@XmlElement(name = "NodeTypeImplementation", required = true)
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
			import_script = new Import(RR_ScriptArtifactType.Definitions.ArtifactType.targetNamespace,
					RR_ScriptArtifactType.filename,"http://docs.oasis-open.org/tosca/ns/2011/12" );
			import_package = new Import(RR_PackageArtifactType.Definitions.ArtifactType.targetNamespace,
					RR_PackageArtifactType.filename,"http://docs.oasis-open.org/tosca/ns/2011/12" );
		}

		
		public static class NodeTypeImplementation {

			@XmlElement(name = "tosca:ImplementationArtifacts", required = true)
			public ImplementationArtifacts implementationArtifacts;
			@XmlElement(name = "tosca:DeploymentArtifacts", required = true)
			public DeploymentArtifacts deploymentArtifacts;
			
			@XmlAttribute(name = "xmlns:ns0", required = true)
			public static final String ns0 = RR_NodeType.Definitions.targetNamespace;
			@XmlAttribute(name = "name", required = true)
			public String name;
			@XmlAttribute(name = "targetNamespace", required = true)
			public static final String targetNamespace = "http://opentosca.org/nodetypeimplementations";
			@XmlAttribute(name = "nodeType", required = true)
			public static final String nodeType = "ns0:" + RR_NodeType.Definitions.NodeType.name;

			NodeTypeImplementation() {
				implementationArtifacts = new ImplementationArtifacts();
				deploymentArtifacts = new DeploymentArtifacts();
			}

			public static class ImplementationArtifacts {

				@XmlElement(name = "tosca:ImplementationArtifact", required = true)
				public ImplementationArtifact implementationArtifact;


				ImplementationArtifacts() {
					implementationArtifact = new ImplementationArtifact();
				}

				public static class ImplementationArtifact{
					@XmlAttribute(name = "xmlns:tbt", required = true)
					public static final String tbt = RR_ScriptArtifactTemplate.Definitions.ArtifactTemplate.tbt;
					@XmlAttribute(name = "xmlns:ns6", required = true)
					public static final String ns6 = RR_ScriptArtifactType.Definitions.targetNamespace;
					@XmlAttribute(name = "name", required = true)
					public String name ;
					@XmlAttribute(name = "interfaceName", required = true)
					public static final String interfaceName = RR_NodeType.Definitions.NodeType.Interfaces.Interface.name;
					@XmlAttribute(name = "operationName", required = true)
					public static final String operationName = RR_NodeType.Definitions.NodeType.Interfaces.Interface.Operation.name;
					@XmlAttribute(name = "artifactType", required = true)
					public static final String artifactType = "tbt:" + RR_ScriptArtifactTemplate.Definitions.ArtifactTemplate.type;
					@XmlAttribute(name = "artifactRef", required = true)
					public String artifactRef;
					ImplementationArtifact() {
					}
				}
				
			}

			public static class DeploymentArtifacts {

				@XmlElement(name = "tosca:DeploymentArtifact", required = true)
				public DeploymentArtifact deploymentArtifact;


				DeploymentArtifacts() {
					deploymentArtifact = new DeploymentArtifact();
				}

				public static class DeploymentArtifact{
					@XmlAttribute(name = "xmlns:tbt", required = true)
					public static final String tbt = RR_PackageArtifactTemplate.Definitions.ArtifactTemplate.tbt;
					@XmlAttribute(name = "xmlns:ns6", required = true)
					public static final String ns6 = RR_PackageArtifactType.Definitions.targetNamespace;
					@XmlAttribute(name = "name", required = true)
					public String name ;
					@XmlAttribute(name = "interfaceName", required = true)
					public static final String interfaceName = RR_NodeType.Definitions.NodeType.Interfaces.Interface.name;
					@XmlAttribute(name = "operationName", required = true)
					public static final String operationName = RR_NodeType.Definitions.NodeType.Interfaces.Interface.Operation.name;
					@XmlAttribute(name = "artifactType", required = true)
					public static final String artifactType = "tbt:" + RR_PackageArtifactTemplate.Definitions.ArtifactTemplate.type;
					@XmlAttribute(name = "artifactRef", required = true)
					public String artifactRef;
					DeploymentArtifact() {
					}
				}
				
			}
		}
	}

	/** Create template for package
	 * @param cr 
	 * @param folder, where template will be created
	 * @param dependensis, list with dependencies for package
	 * @param packet, name of packet
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void createPackageTemplate(Control_references cr, String packet)
			throws IOException, JAXBException {
		System.out.println("creating Package Template for " + packet  );

		File temp = new File(cr.getFolder() + Control_references.Definitions + getFilename(packet));
		if (temp.exists())
			temp.delete();
		temp.createNewFile();
		OutputStream output = new FileOutputStream(temp);

		JAXBContext jc = JAXBContext.newInstance(Definitions.class);

		Definitions template = new Definitions();
		template.id = getWineryID(packet);
		template.nodeTypeImplementation.name = getID(packet);
		

		template.import_IA = new Import(RR_ScriptArtifactTemplate.Definitions.targetNamespace,
				RR_ScriptArtifactTemplate.getFilename(packet),"http://docs.oasis-open.org/tosca/ns/2011/12" );
		template.import_DA = new Import(RR_PackageArtifactTemplate.Definitions.targetNamespace,
				RR_PackageArtifactTemplate.getFilename(packet),"http://docs.oasis-open.org/tosca/ns/2011/12" );
		
		template.nodeTypeImplementation.implementationArtifacts.implementationArtifact.name = RR_ScriptArtifactTemplate.getID(packet);
		template.nodeTypeImplementation.implementationArtifacts.implementationArtifact.artifactRef = "ns6:" + RR_ScriptArtifactTemplate.getID(packet);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(template, output);
		cr.metaFile.addFileToMeta(Control_references.Definitions + getFilename(packet), "application/vnd.oasis.tosca.definitions");
		
	}
	
	public static String getWineryID(String packet){
		return "winery-defs-for_"+packet+"_Impl";
	}
	
	public static String getID(String packet){
		return packet+"_Impl";
	}

	public static String getFilename(String packet){
		return "RR_" + packet + extension;
	}
}
