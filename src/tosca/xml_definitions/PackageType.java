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
import tosca.Resolver;

public class PackageType {

	/**
	 * @author Yaroslav PackageType XML description
	 */
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
	public static class Shema {
		public Shema() {
			artifactType = new ArtifactType();
			element = new Element();
			complexType2 = new ComplexType2();
		}

		public static class Element {
			@XmlAttribute
			public static final String name = "PackageTypeArtifactProperties";

			Element() {
				complexType = new ComplexType();
			}

			public static class ComplexType {
				ComplexType() {
					complexContent = new ComplexContent();
				}

				public static class ComplexContent {
					ComplexContent() {
						extension = new Extension();
					}

					public static class Extension {
						@XmlAttribute
						public static final String base = "tPackageTypeArtifactProperties";
					}

					public Extension extension;
				}

				public ComplexContent complexContent;
			}

			public ComplexType complexType;
		}

		public static class ComplexType2 {
			@XmlAttribute
			public static final String name = "tPackageTypeArtifactProperties";

			ComplexType2() {
				complexContent2 = new ComplexContent2();
			}

			public static class ComplexContent2 {

				ComplexContent2() {
					extension2 = new Extension2();
				}

				public static class Extension2 {
					Extension2() {
						sequence = new Sequence();
					}

					@XmlAttribute
					public static final String base = "tExtensibleElements";

					public static class Sequence {
						@XmlAttribute
						public static final String minOccurs = "0";
						@XmlAttribute
						public static final String maxOccurs = "unbounded";

						Sequence() {
							element2 = new Element2();
						}

						public static class Element2 {
							@XmlAttribute
							public final String name = "dependsOn";
							@XmlAttribute
							public final String type = "string";
						}

						@XmlElement(name = "element", required = true)
						public Element2 element2;
					}

					public Sequence sequence;
				}

				@XmlElement(name = "extension", required = true)
				public Extension2 extension2;
			}

			@XmlElement(name = "ComplexContent", required = true)
			public ComplexContent2 complexContent2;
		}

		@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
		public static class ArtifactType {
			@XmlAttribute
			public static final String name = "PackageType";

			@XmlElement(name = "documentation", required = true)
			public static final String documentation = "Downloaded package to install later";

			ArtifactType() {
				propertiesDefinition = new PropertiesDefinition();
			}

			public static class PropertiesDefinition {
				@XmlAttribute
				public static final String element = "PackageTypeArtifactProperties";
			}

			public PropertiesDefinition propertiesDefinition;
		}

		public Element element;
		@XmlElement(name = "ComplexType", required = true)
		public ComplexType2 complexType2;
		@XmlElement(name = "ArtifactType", required = true)
		public ArtifactType artifactType;
	}

	// output filename
	private static String filename = "PackageType.xml";

	/**
	 * Create PackageType xml description
	 * 
	 * @param cr
	 * @throws JAXBException
	 * @throws IOException
	 */
	public static void init(Control_references cr) throws JAXBException,
			IOException {
		File dir = new File(cr.getFolder() + Resolver.folder);
		dir.mkdirs();
		File temp = new File(cr.getFolder() + Resolver.folder + filename);
		if (temp.exists())
			temp.delete();
		temp.createNewFile();
		OutputStream output = new FileOutputStream(cr.getFolder()
				+ Resolver.folder + filename);

		JAXBContext jc = JAXBContext.newInstance(Shema.class);

		Shema shema = new Shema();

		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(shema, output);
		cr.metaFile.addFileToMeta(Resolver.folder + filename, "text/xml");
	}
}
