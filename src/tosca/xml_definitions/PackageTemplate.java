package tosca.xml_definitions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

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
 * @author Yaroslav Template for packages
 */
public class PackageTemplate {
	public static final String extension = ".xml";

	/**
	 * @author Yaroslav Template description
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class ArtifactTemplate {
		@XmlAttribute
		public String id;
		@XmlAttribute
		public static String type = "PackageType";

		public ArtifactTemplate() {
			properties = new Properties();
			artifactReferences = new ArtifactReferences();
		}

		public static class Properties {
			public Properties() {
				prop = new PackageTypeArtifactProperties();
			}

			public static class PackageTypeArtifactProperties {
				PackageTypeArtifactProperties() {
					dependsOn = new LinkedList<String>();
				}

				@XmlElement(name = "dependsOn")
				public List<String> dependsOn;

			}

			@XmlElement(name = "PackageTypeArtifactProperties", required = true)
			PackageTypeArtifactProperties prop;
		}

		public static class ArtifactReferences {
			
			public ArtifactReferences(){
				artifactReference = new ArtifactReference();
			}
			
			static class ArtifactReference{
				public ArtifactReference(){
					
				}
				//TODO
				
			}
//
			@XmlElement(name = "ArtifactReference", required = true)
			public ArtifactReference artifactReference;
		}

		public Properties properties;
		public ArtifactReferences artifactReferences;
	}

	@XmlElement(name = "ArtifactTemplate", required = true)
	public ArtifactTemplate artifactTemplate;

	/** Create template for package
	 * @param cr 
	 * @param folder, where template will be created
	 * @param dependensis, list with dependencies for package
	 * @param packet, name of packet
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void createPackageTemplate(Control_references cr,
			String folder, List<String> dependencies, String packet)
			throws IOException, JAXBException {
		System.out.println("creating Package Template in " + folder + ", for " + packet + " with dependencies: " + dependencies );

		File dir = new File(cr.getFolder() + folder);
		dir.mkdirs();
		File temp = new File(cr.getFolder() + folder + packet + extension);
		if (temp.exists())
			temp.delete();
		temp.createNewFile();
		OutputStream output = new FileOutputStream(temp);

		JAXBContext jc = JAXBContext.newInstance(ArtifactTemplate.class);

		ArtifactTemplate template = new ArtifactTemplate();
		template.id = packet;
		template.properties.prop.dependsOn.addAll(dependencies);

		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(template, output);
	}

}
