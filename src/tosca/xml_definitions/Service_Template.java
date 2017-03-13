package tosca.xml_definitions;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tosca.Control_references;

//TODO abstract NodeType? 1 Imlementation  -> n NodeTypes ??

public class Service_Template {

	public static final String Tosca = ".tosca";
	private static final String ToscaNS = "xmlns:RR_tosca_ns";
	private static final String myPrefix = "RR_tosca_ns:";

	public static final String Definitions = "Definitions/";
	List<RR_Link> links;
	List<ArtifactsRef> artifactsRefs;
	List<NodeRef> nodeRefs;
	List<NodeRef> myNodeRefs;

	public static class RR_Link {
		public String reference;
		public List<String> nodeTypeName;
		public List<String> serviceTemplateFiles;

		public RR_Link(String ref, String NTN, String STF) {
			nodeTypeName = new LinkedList<String>();
			serviceTemplateFiles = new LinkedList<String>();
			reference = ref;
			nodeTypeName.add(NTN);
			serviceTemplateFiles.add(STF);
		}
	}

	public static class ArtifactsRef {
		public String reference;
		public String artifactID;

		public ArtifactsRef(String ref, String AID) {
			reference = ref;
			artifactID = AID;
		}

		public boolean equals(Object o) {
			return ((ArtifactsRef) o).reference.equals(reference)
					&& ((ArtifactsRef) o).artifactID.equals(artifactID);
		}
	}

	public static class NodeRef {
		public String reference;
		public List<String> nodeTypeName;

		public NodeRef(String ref, String NTN) {
			nodeTypeName = new LinkedList<String>();
			reference = ref;
			nodeTypeName.add(NTN);
		}

		public NodeRef(String ref, List<String> NTN) {
			nodeTypeName = new LinkedList<String>();
			reference = ref;
			nodeTypeName.addAll(NTN);
		}

		public void addNodeType(String nodeType) {
			if (!nodeTypeName.contains(nodeType))
				nodeTypeName.add(nodeType);
		}
	}

	public Service_Template() {
		links = new LinkedList<RR_Link>();
		artifactsRefs = new LinkedList<ArtifactsRef>();
		nodeRefs = new LinkedList<NodeRef>();
	}

	public Service_Template(Control_references cr) {
		links = new LinkedList<RR_Link>();
		artifactsRefs = new LinkedList<ArtifactsRef>();
		nodeRefs = new LinkedList<NodeRef>();
		myNodeRefs = new LinkedList<NodeRef>();
		init(cr);
	}

	public void init(Control_references cr) {

		links.clear();
		artifactsRefs.clear();
		nodeRefs.clear();
		myNodeRefs.clear();

		File folder = new File(cr.getFolder() + Definitions);
		if (!folder.exists())
			return;
		System.out.println("Parse Artifacts");
		for (File entry : folder.listFiles()) {
			parseArtifacts(entry);
		}

		System.out.println("Parse Implementations");
		for (File entry : folder.listFiles()) {
			parseImplementations(entry);
		}

		System.out.println("Parse ServiceTemplates");
		for (File entry : folder.listFiles()) {
			parseServiceTemplates(entry);
		}

		for (RR_Link entry : links) {
			System.out.println(entry.reference + " : " + entry.nodeTypeName
					+ " : " + entry.serviceTemplateFiles);
		}
	}

	// adds Templates and Relation
	public void addDependencyToPacket(Control_references cr,
			String source_packet, String target_packet) {
		NodeRef currentNode = null;
		for (NodeRef nodeRef : myNodeRefs)
			if (nodeRef.reference.equals(source_packet))
				currentNode = new NodeRef(nodeRef.reference,
						nodeRef.nodeTypeName);
		if (currentNode == null)
			return;
		for (String filename : currentNode.nodeTypeName) {
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder documentBuilder;
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(cr.getFolder()
						+ Control_references.Definitions + filename);
				NodeList nodes = document.getElementsByTagName("*");
				for (int i = 0; i < nodes.getLength(); i++)
					if (nodes.item(i).getNodeName().endsWith(":NodeTemplate")
							|| nodes.item(i).getNodeName()
									.equals("NodeTemplate")) {
						String type = ((Element) nodes.item(i))
								.getAttribute("type");
						String sourceID = ((Element) nodes.item(i))
								.getAttribute("id");
						if ((type.equals("RRnt:"
								+ RR_NodeType.Definitions.NodeType.name))
								&& sourceID.equals(getID(source_packet))) {
							// right NodeTemplate found
							// need to create new Node Template
							// and reference
							Node topology = nodes.item(i).getParentNode();
							createPacketTemplate(document, topology,
									target_packet);
							createPacketDependency(document, topology,
									sourceID, getID(target_packet));
							boolean found = false;
							for (NodeRef node : myNodeRefs)
								if (node.reference == target_packet) {
									found = true;
									node.addNodeType(filename);
								}
							if (!found)
								myNodeRefs.add(new NodeRef(target_packet,
										filename));
						}
					}
				addRRImport(document);
				Transformer transformer = TransformerFactory.newInstance()
						.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "4");
				Result output = new StreamResult(new File(cr.getFolder()
						+ Control_references.Definitions + filename));
				Source input = new DOMSource(document);
				transformer.transform(input, output);

			} catch (ParserConfigurationException | SAXException | IOException
					| TransformerFactoryConfigurationError
					| TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void addDependencyToScript(Control_references cr,
			String script_filename, String target_packet) {
		for (RR_Link link : links)
			if (link.reference.equals(script_filename))
				for (String filename : link.serviceTemplateFiles) {
					try {
						DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
								.newInstance();
						DocumentBuilder documentBuilder;
						documentBuilder = documentBuilderFactory
								.newDocumentBuilder();
						Document document = documentBuilder.parse(cr
								.getFolder()
								+ Control_references.Definitions
								+ filename);
						NodeList nodes = document.getElementsByTagName("*");
						for (int i = 0; i < nodes.getLength(); i++)
							if (nodes.item(i).getNodeName()
									.endsWith(":NodeTemplate")
									|| nodes.item(i).getNodeName()
											.equals("NodeTemplate")) {
								String type = ((Element) nodes.item(i))
										.getAttribute("type");
								for (String nodeType : link.nodeTypeName)
									if (type.endsWith(":" + nodeType)
											|| type.equals(nodeType)) {
										String sourceID = ((Element) nodes
												.item(i)).getAttribute("id");
										// right NodeTemplate found
										// need to create new Node Template
										// and reference
										Node topology = nodes.item(i)
												.getParentNode();
										createPacketTemplate(document,
												topology, target_packet);
										createPacketDependency(document,
												topology, sourceID,
												getID(target_packet));
										boolean found = false;
										for (NodeRef node : myNodeRefs)
											if (node.reference == target_packet) {
												found = true;
												node.addNodeType(filename);
											}
										if (!found)
											myNodeRefs.add(new NodeRef(
													target_packet, filename));
									}
							}
						addRRImport(document);
						Transformer transformer = TransformerFactory
								.newInstance().newTransformer();
						transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						transformer.setOutputProperty(
								"{http://xml.apache.org/xslt}indent-amount",
								"4");
						Result output = new StreamResult(new File(
								cr.getFolder() + Control_references.Definitions
										+ filename));
						Source input = new DOMSource(document);
						transformer.transform(input, output);

					} catch (ParserConfigurationException | SAXException
							| IOException
							| TransformerFactoryConfigurationError
							| TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	}

	private String getID(String packet) {
		return packet + "_template";
	}

	private void createPacketTemplate(Document document, Node topology,
			String packet) {
		NodeList nodes = document.getElementsByTagName("*");
		for (int i = 0; i < nodes.getLength(); i++)
			if (nodes.item(i).getNodeName().endsWith(":NodeTemplate")
					|| nodes.item(i).getNodeName().equals("NodeTemplate")) {
				if (((Element) nodes.item(i)).getAttribute("id").equals(
						getID(packet)))
					return;
			}
		System.out.println("Add template for " + packet);
		Element template = document.createElement(myPrefix + "NodeTemplate");
		template.setAttribute("xmlns:RRnt",
				RR_NodeType.Definitions.NodeType.targetNamespace);
		template.setAttribute("id", getID(packet));
		template.setAttribute("name", packet);
		template.setAttribute("type", "RRnt:"
				+ RR_NodeType.Definitions.NodeType.name);
		Element deploymentArtifacts = document.createElement(myPrefix
				+ "DeploymentArtifacts");
		template.appendChild(deploymentArtifacts);
		Element deploymentArtifact = document.createElement(myPrefix
				+ "DeploymentArtifact");
		deploymentArtifact
				.setAttribute(
						"xmlns:RRpt",
						RR_PackageArtifactType.Definitions.ArtifactType.targetNamespace);
		deploymentArtifact.setAttribute("xmlns:RRda",
				RR_PackageArtifactTemplate.Definitions.targetNamespace);
		deploymentArtifact.setAttribute("artifactType", "RRpt:"
				+ RR_PackageArtifactType.Definitions.ArtifactType.name);
		deploymentArtifact.setAttribute("name", packet + "_DA");
		deploymentArtifact.setAttribute("artifactRef", "RRda:"
				+ RR_PackageArtifactTemplate.getID(packet));

		deploymentArtifacts.appendChild(deploymentArtifact);
		// winery? TODO
		// winery? TODO
		topology.appendChild(template);
	}

	private void createPacketDependency(Document document, Node topology,
			String sourceID, String targetID) {
		System.out.println("Add relation from " + sourceID + " to " + targetID);
		NodeList nodes = document.getElementsByTagName("*");
		for (int i = 0; i < nodes.getLength(); i++)
			if (nodes.item(i).getNodeName().endsWith(":RelationshipTemplate")
					|| nodes.item(i).getNodeName()
							.equals("RelationshipTemplate")) {
				if (((Element) nodes.item(i)).getAttribute("id").equals(
						getID(sourceID + "_" + targetID)))
					return;
			}
		Element relation = document.createElement(myPrefix
				+ "RelationshipTemplate");
		relation.setAttribute("xmlns:RRrt",
				RR_DependsOn.Definitions.RelationshipType.targetNamespace);
		relation.setAttribute("id", sourceID + "_" + targetID);
		relation.setAttribute("name", sourceID + "_needs_" + targetID);
		relation.setAttribute("type", "RRrt:"
				+ RR_DependsOn.Definitions.RelationshipType.name);
		topology.appendChild(relation);
		Element sourceElement = document.createElement(myPrefix
				+ "SourceElement");
		sourceElement.setAttribute("ref", sourceID);
		relation.appendChild(sourceElement);
		Element targetElement = document.createElement(myPrefix
				+ "TargetElement");
		targetElement.setAttribute("ref", targetID);
		relation.appendChild(targetElement);
	}

	private void addRRImport(Document document) {
		Node definitions = document.getFirstChild();
		if (definitions.getAttributes().getNamedItem(ToscaNS) == null) {
			((Element) definitions).setAttribute(ToscaNS,
					"http://docs.oasis-open.org/tosca/ns/2011/12");
			Element tImport = document.createElement("RR_tosca_ns:Import");
			tImport.setAttribute("importType",
					"http://docs.oasis-open.org/tosca/ns/2011/12");
			tImport.setAttribute("location", RR_DependsOn.filename);
			tImport.setAttribute("namespace",
					RR_DependsOn.Definitions.RelationshipType.targetNamespace);
			definitions.insertBefore(tImport, definitions.getFirstChild());
			tImport = document.createElement("RR_tosca_ns:Import");
			tImport.setAttribute("importType",
					"http://docs.oasis-open.org/tosca/ns/2011/12");
			tImport.setAttribute("location", RR_NodeType.filename);
			tImport.setAttribute("namespace",
					RR_NodeType.Definitions.NodeType.targetNamespace);
			definitions.insertBefore(tImport, definitions.getFirstChild());
		}
	}

	private void parseArtifacts(File file) {
		// System.out.println("Parse " + file.getName());
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nodes = document.getElementsByTagName("*");
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeName().endsWith(":ArtifactTemplate")
						|| nodes.item(i).getNodeName()
								.equals("ArtifactTemplate")) {
					Element e = (Element) nodes.item(i);
					String ID = e.getAttribute("id");
					NodeList ArtifactReferences = e.getChildNodes();
					for (int k = 0; k < ArtifactReferences.getLength(); k++) {
						if (ArtifactReferences.item(k).getNodeName()
								.endsWith(":ArtifactReferences")
								|| ArtifactReferences.item(k).getNodeName()
										.equals("ArtifactReferences")) {
							if (ArtifactReferences.item(k).getNodeType() == Node.ELEMENT_NODE) {
								Element elem = (Element) ArtifactReferences
										.item(k);
								NodeList ArtifactReferenceList = elem
										.getChildNodes();
								for (int j = 0; j < ArtifactReferenceList
										.getLength(); j++) {
									if (ArtifactReferenceList.item(j)
											.getNodeType() == Node.ELEMENT_NODE) {
										Element ref = (Element) ArtifactReferenceList
												.item(j);
										String REF = java.net.URLDecoder
												.decode(ref
														.getAttribute("reference"),
														"UTF-8");
										if (!artifactsRefs
												.contains(new ArtifactsRef(REF,
														ID)))
											artifactsRefs.add(new ArtifactsRef(
													REF, ID));
									}
								}
							}
						}

					}
				}

			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseImplementations(File file) {

		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		// System.out.println("Parse " + file.getName());
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nodes = document.getElementsByTagName("*");
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeName()
						.endsWith(":NodeTypeImplementation")
						|| nodes.item(i).getNodeName()
								.equals("NodeTypeImplementation")) {
					Element e = (Element) nodes.item(i);
					String nodeType = e.getAttribute("nodeType");
					if (nodeType.contains(":"))
						nodeType = nodeType.substring(
								nodeType.indexOf(':') + 1, nodeType.length());
					NodeList artifactLists = e.getChildNodes();
					for (int k = 0; k < artifactLists.getLength(); k++) {
						if (artifactLists.item(k).getNodeName()
								.endsWith(":ImplementationArtifacts")
								|| artifactLists.item(k).getNodeName()
										.equals("ImplementationArtifacts")
								|| artifactLists.item(k).getNodeName()
										.endsWith(":DeploymentArtifacts")
								|| artifactLists.item(k).getNodeName()
										.equals("DeploymentArtifacts")) {
							if (artifactLists.item(k).getNodeType() == Node.ELEMENT_NODE) {
								Element elem = (Element) artifactLists.item(k);
								NodeList artifacts = elem.getChildNodes();
								for (int j = 0; j < artifacts.getLength(); j++) {
									if (artifacts.item(j).getNodeType() == Node.ELEMENT_NODE) {
										Element artifactImplementation = (Element) artifacts
												.item(j);
										String artifactRef = artifactImplementation
												.getAttribute("artifactRef");
										if (artifactRef.contains(":"))
											artifactRef = artifactRef
													.substring(artifactRef
															.indexOf(':') + 1,
															artifactRef
																	.length());
										addNodeTypeRef(nodeType, artifactRef);
									}
								}
							}
						}

					}
				}

			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addNodeTypeRef(String nodeType, String reference) {
		for (ArtifactsRef artRef : artifactsRefs) {
			if (artRef.artifactID.equals(reference)) {
				boolean found = false;
				for (NodeRef nodeRef : nodeRefs) {
					if (nodeRef.reference.equals(artRef.reference)) {
						found = true;
						nodeRef.addNodeType(nodeType);
					}
				}
				if (!found)
					nodeRefs.add(new NodeRef(artRef.reference, nodeType));
			}
		}
	}

	private void parseServiceTemplates(File file) {
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		// System.out.println("Parse " + file.getName());
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nodes = document.getElementsByTagName("*");
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeName().endsWith(":NodeTemplate")
						|| nodes.item(i).getNodeName().equals("NodeTemplate")) {
					Element e = (Element) nodes.item(i);
					String nodeType = e.getAttribute("type");
					if (nodeType.contains(":"))
						nodeType = nodeType.substring(
								nodeType.indexOf(':') + 1, nodeType.length());
					AddFileToNode(nodeType, file.getName());
				}

			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void AddFileToNode(String nodeType, String name) {
		for (RR_Link link : links) {
			if (link.nodeTypeName.contains(nodeType)
					&& !link.serviceTemplateFiles.contains(name)) {
				link.serviceTemplateFiles.add(name);
			}
		}
		Iterator<NodeRef> it = nodeRefs.iterator();
		while (it.hasNext()) {
			NodeRef nodeRef = it.next();
			if (nodeRef.nodeTypeName.contains(nodeType)) {
				links.add(new RR_Link(nodeRef.reference, nodeType, name));
				nodeRef.nodeTypeName.remove(nodeType);
				if (nodeRef.nodeTypeName.isEmpty())
					it.remove();
			}
		}

	}

}
