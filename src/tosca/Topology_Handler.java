package tosca;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
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

import tosca.xml_definitions.RR_DependsOn;
import tosca.xml_definitions.RR_NodeType;
import tosca.xml_definitions.RR_PackageArtifactTemplate;
import tosca.xml_definitions.RR_PackageArtifactType;
import tosca.xml_definitions.RR_PreDependsOn;

/**
 * @author jery Service Template Handler
 */
public class Topology_Handler {

	public static final String Tosca = ".tosca";
	private static final String ToscaNS = "xmlns:RR_tosca_ns";
	private static final String myPrefix = "RR_tosca_ns:";
	public static final String Definitions = "Definitions/";
	private static final String Type_glue = "_update_RR_";

	// Reference from NodeType to files with Service Templates
	HashMap<String, List<String>> NodeTypeToServiceTemplate;

	// Reference from Script position to ArtifactID
	HashMap<String, List<String>> RefToArtID;

	// Reference from Script position to Node Type
	HashMap<String, List<String>> RefToNodeType;

	public HashMap<String, List<String>> getRefToNodeType() {
		return RefToNodeType;
	}

	CSAR_handler ch;

	/**
	 * simple Constructor
	 */
	/**
	 * Constructor with initialization
	 * 
	 * @param ch
	 */
	public Topology_Handler(CSAR_handler new_ch) {
		NodeTypeToServiceTemplate = new HashMap<String, List<String>>();
		RefToArtID = new HashMap<String, List<String>>();
		RefToNodeType = new HashMap<String, List<String>>();
		init(new_ch);
	}

	/**
	 * Init all local references, search for script positions and dependent Node
	 * Types
	 * 
	 * @param ch
	 */
	public void init(CSAR_handler new_ch) {

		ch = new_ch;

		NodeTypeToServiceTemplate.clear();
		RefToArtID.clear();
		RefToNodeType.clear();

		File folder = new File(ch.getFolder() + Definitions);
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

		System.out.println("RefToNodeType");
		for (String key : RefToNodeType.keySet()) {
			System.out.println(key + " : " + RefToNodeType.get(key));
		}
		System.out.println("NodeTypeToServiceTemplate");
		for (String key : NodeTypeToServiceTemplate.keySet()) {
			System.out.println(key + " : " + NodeTypeToServiceTemplate.get(key));
		}
	}

	/**
	 * Add Node Template for new packet, and depends it to packet created by me
	 * 
	 * @param source_packet
	 *            packet already in Service Template
	 * @param target_packet
	 *            packet to be created
	 * @param dependencyType
	 * @throws UnsupportedEncodingException
	 */
	public void addDependencyToPacket(String source_packet, String target_packet, String dependencyType)
			throws UnsupportedEncodingException {
		source_packet = encode(source_packet);
		target_packet = encode(target_packet);
		for (String filename : NodeTypeToServiceTemplate.get(source_packet)) {
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder;
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(ch.getFolder() + CSAR_handler.Definitions + filename);
				NodeList nodes = document.getElementsByTagName("*");
				for (int i = 0; i < nodes.getLength(); i++)
					if (nodes.item(i).getNodeName().endsWith(":NodeTemplate")
							|| nodes.item(i).getNodeName().equals("NodeTemplate")) {
						String type = ((Element) nodes.item(i)).getAttribute("type");
						String sourceID = ((Element) nodes.item(i)).getAttribute("id");
						if ((type.equals("RRnt:" + RR_NodeType.getTypeName(source_packet)))
								&& sourceID.startsWith(getID(source_packet) + Type_glue)) {
							// right NodeTemplate found
							// need to create new Node Template
							// and reference
							Node topology = nodes.item(i).getParentNode();
							updateTopology(document, topology, filename, sourceID, target_packet, dependencyType);
						}
					}
				addRRImport_NT(document, target_packet);
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				Result output = new StreamResult(new File(ch.getFolder() + CSAR_handler.Definitions + filename));
				Source input = new DOMSource(document);
				transformer.transform(input, output);

			} catch (ParserConfigurationException | SAXException | IOException | TransformerFactoryConfigurationError
					| TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void updateTopology(Document document, Node topology, String filename, String sourceID,
			String target_packet, String dependencyType) throws UnsupportedEncodingException {
		createPacketTemplate(document, topology, target_packet, sourceID);
		createPacketDependency(document, topology, sourceID, getID(target_packet), dependencyType);

		if (!NodeTypeToServiceTemplate.containsKey(target_packet))
			NodeTypeToServiceTemplate.put(target_packet, new LinkedList<String>());
		if (!NodeTypeToServiceTemplate.get(target_packet).contains(filename))
			NodeTypeToServiceTemplate.get(target_packet).add(filename);
	}

	/**
	 * Generates and return all files, which use give script
	 * 
	 * @param reference
	 *            script position
	 * @return list with each files containing service templates for given script
	 *         position
	 */
	private List<String> getServiceTemplatesFromRef(String reference) {
		List<String> serviceTemplates = new LinkedList<String>();

		for (String nodeType : RefToNodeType.get(reference)) {
			for (String serviceTemplate : NodeTypeToServiceTemplate.get(nodeType))
				if (!serviceTemplates.contains(serviceTemplate))
					serviceTemplates.add(serviceTemplate);
		}
		return serviceTemplates;
	}

	/**
	 * Add new NodeTemplate and dependency to existing NodeTemplate by given script
	 * position
	 * 
	 * @param script_filename
	 *            script position
	 * @param target_packet
	 *            packet to be added
	 * @throws UnsupportedEncodingException
	 */
	public void addDependencyToScript(String script_filename, String target_packet)
			throws UnsupportedEncodingException {
		List<String> files = getServiceTemplatesFromRef(script_filename);
		target_packet = encode(target_packet);
		for (String filename : files) {
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder;
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(ch.getFolder() + CSAR_handler.Definitions + filename);
				NodeList nodes = document.getElementsByTagName("*");
				for (int i = 0; i < nodes.getLength(); i++)
					if (nodes.item(i).getNodeName().endsWith(":NodeTemplate")
							|| nodes.item(i).getNodeName().equals("NodeTemplate")) {
						String type = ((Element) nodes.item(i)).getAttribute("type");
						for (String nodeType : RefToNodeType.get(script_filename))
							if (type.endsWith(":" + nodeType) || type.equals(nodeType)) {
								String sourceID = ((Element) nodes.item(i)).getAttribute("id");
								// right NodeTemplate found
								// need to create new Node Template
								// and reference
								Node topology = nodes.item(i).getParentNode();
								updateTopology(document, topology, filename, sourceID, target_packet,
										RR_PreDependsOn.Name);
							}
					}
				addRRImport_NT(document, target_packet);
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				Result output = new StreamResult(new File(ch.getFolder() + CSAR_handler.Definitions + filename));
				Source input = new DOMSource(document);
				transformer.transform(input, output);

			} catch (ParserConfigurationException | SAXException | IOException | TransformerFactoryConfigurationError
					| TransformerException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generate NodeTemplate ID for given packet
	 * 
	 * @param packet
	 *            packet name
	 * @return ID
	 * @throws UnsupportedEncodingException
	 */
	private String getID(String packet) {
		return RR_NodeType.getTypeName(packet);// +
		// "_template";
	}

	public static String encode(String packet) throws UnsupportedEncodingException {
		return java.net.URLEncoder.encode(packet, "UTF-8");// +
		// "_template";
	}

	/**
	 * Creates NodeTemplate
	 * 
	 * @param document
	 *            to be proceed
	 * @param topology
	 *            Node Containing Topology of Service Template
	 * @param packet
	 *            packet name
	 * @throws UnsupportedEncodingException
	 */
	private void createPacketTemplate(Document document, Node topology, String packet, String source)
			throws UnsupportedEncodingException {
		NodeList nodes = document.getElementsByTagName("*");
		for (int i = 0; i < nodes.getLength(); i++)
			if (nodes.item(i).getNodeName().endsWith(":NodeTemplate")
					|| nodes.item(i).getNodeName().equals("NodeTemplate")) {
				if (((Element) nodes.item(i)).getAttribute("id").equals(getID(packet)))
					return;
			}
		System.out.println("Add template for " + packet);
		Element template = document.createElement(myPrefix + "NodeTemplate");
		template.setAttribute("xmlns:RRnt", RR_NodeType.Definitions.NodeType.targetNamespace);
		template.setAttribute("id", getID(packet + Type_glue + source));
		template.setAttribute("name", packet);
		template.setAttribute("type", "RRnt:" + RR_NodeType.getTypeName(packet));
		topology.appendChild(template);
	}

	/**
	 * Creates dependency between sourceID and targetID
	 * 
	 * @param document
	 * @param topology
	 *            node containing topology
	 * @param sourceID
	 *            packet which needs targetID
	 * @param targetID
	 *            is needed by sourceID
	 */
	private void createPacketDependency(Document document, Node topology, String sourceID, String targetID,
			String type) {
		if (type == null)
			throw new NullPointerException();
		System.out.println("Add relation from " + sourceID + " to " + targetID);
		NodeList nodes = document.getElementsByTagName("*");
		for (int i = 0; i < nodes.getLength(); i++)
			if (nodes.item(i).getNodeName().endsWith(":RelationshipTemplate")
					|| nodes.item(i).getNodeName().equals("RelationshipTemplate")) {
				if (((Element) nodes.item(i)).getAttribute("id").equals(sourceID + "_" + targetID))
					return;
			}
		Element relation = document.createElement(myPrefix + "RelationshipTemplate");
		relation.setAttribute("id", sourceID + "_" + targetID);
		relation.setAttribute("name", sourceID + "_needs_" + targetID);
		if (type.equals(RR_DependsOn.Name)) {
			relation.setAttribute("xmlns:RRrt", RR_DependsOn.Definitions.RelationshipType.targetNamespace);
			relation.setAttribute("type", "RRrt:" + RR_DependsOn.Definitions.RelationshipType.name);
		} else if (type.equals(RR_PreDependsOn.Name)) {
			relation.setAttribute("xmlns:RRrt", RR_PreDependsOn.Definitions.RelationshipType.targetNamespace);
			relation.setAttribute("type", "RRrt:" + RR_PreDependsOn.Definitions.RelationshipType.name);
		}
		topology.appendChild(relation);
		Element sourceElement = document.createElement(myPrefix + "SourceElement");
		sourceElement.setAttribute("ref", sourceID);
		relation.appendChild(sourceElement);
		Element targetElement = document.createElement(myPrefix + "TargetElement");
		targetElement.setAttribute("ref", targetID + Type_glue + sourceID);
		relation.appendChild(targetElement);
	}

	/**
	 * Add my imports to document
	 * 
	 * @param document
	 */
	private void addRRImport_Base(Document document, String packet) { // TODO
		Element tImport;
		Node definitions = document.getFirstChild();
		if (definitions.getAttributes().getNamedItem(ToscaNS) == null) {
			((Element) definitions).setAttribute(ToscaNS, "http://docs.oasis-open.org/tosca/ns/2011/12");

			tImport = document.createElement("RR_tosca_ns:Import");
			tImport.setAttribute("importType", "http://docs.oasis-open.org/tosca/ns/2011/12");
			tImport.setAttribute("location", RR_DependsOn.filename);
			tImport.setAttribute("namespace", RR_DependsOn.Definitions.RelationshipType.targetNamespace);
			definitions.insertBefore(tImport, definitions.getFirstChild());

			tImport = document.createElement("RR_tosca_ns:Import");
			tImport.setAttribute("importType", "http://docs.oasis-open.org/tosca/ns/2011/12");
			tImport.setAttribute("location", RR_PreDependsOn.filename);
			tImport.setAttribute("namespace", RR_PreDependsOn.Definitions.RelationshipType.targetNamespace);
			definitions.insertBefore(tImport, definitions.getFirstChild());

			tImport = document.createElement("RR_tosca_ns:Import");
			tImport.setAttribute("importType", "http://docs.oasis-open.org/tosca/ns/2011/12");
			tImport.setAttribute("location", RR_PackageArtifactType.filename);
			tImport.setAttribute("namespace", RR_PackageArtifactType.Definitions.ArtifactType.targetNamespace);
			definitions.insertBefore(tImport, definitions.getFirstChild());
		}
	}

	private void addRRImport_NT(Document document, String packet) { // TODO
		Element tImport;
		Node definitions = document.getFirstChild();
		addRRImport_Base(document, packet);
		NodeList nodes = document.getElementsByTagName("RR_tosca_ns:Import");
		for (int i = 0; i < nodes.getLength(); i++)
			if (((Element) (nodes.item(i))).getAttribute("location").equals(RR_NodeType.getFileName(packet)))
				return;
		tImport = document.createElement("RR_tosca_ns:Import");
		tImport.setAttribute("importType", "http://docs.oasis-open.org/tosca/ns/2011/12");
		tImport.setAttribute("location", RR_NodeType.getFileName(packet));
		tImport.setAttribute("namespace", RR_NodeType.Definitions.NodeType.targetNamespace);
		definitions.insertBefore(tImport, definitions.getFirstChild());
	}

	private void addRRImport_DA(Document document, String packet) { // TODO
		Element tImport;
		Node definitions = document.getFirstChild();
		addRRImport_Base(document, packet);
		NodeList nodes = document.getElementsByTagName("RR_tosca_ns:Import");
		for (int i = 0; i < nodes.getLength(); i++)
			if (((Element) (nodes.item(i))).getAttribute("location")
					.equals(RR_PackageArtifactTemplate.getFileName(packet)))
				return;
		tImport = document.createElement("RR_tosca_ns:Import");
		tImport.setAttribute("importType", "http://docs.oasis-open.org/tosca/ns/2011/12");
		tImport.setAttribute("location", RR_PackageArtifactTemplate.getFileName(packet));
		tImport.setAttribute("namespace", RR_PackageArtifactTemplate.Definitions.targetNamespace);
		definitions.insertBefore(tImport, definitions.getFirstChild());
	}

	/**
	 * Parse Artifact Templates for creating script position -> ArtifactID reference
	 * 
	 * @param file
	 */
	private void parseArtifacts(File file) {
		// System.out.println("Parse " + file.getName());
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nodes = document.getElementsByTagName("*");
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeName().endsWith(":ArtifactTemplate")
						|| nodes.item(i).getNodeName().equals("ArtifactTemplate")) {
					Element e = (Element) nodes.item(i);
					String ID = e.getAttribute("id");
					NodeList ArtifactReferences = e.getChildNodes();
					for (int k = 0; k < ArtifactReferences.getLength(); k++) {
						if (ArtifactReferences.item(k).getNodeName().endsWith(":ArtifactReferences")
								|| ArtifactReferences.item(k).getNodeName().equals("ArtifactReferences")) {
							if (ArtifactReferences.item(k).getNodeType() == Node.ELEMENT_NODE) {
								Element elem = (Element) ArtifactReferences.item(k);
								NodeList ArtifactReferenceList = elem.getChildNodes();
								for (int j = 0; j < ArtifactReferenceList.getLength(); j++) {
									if (ArtifactReferenceList.item(j).getNodeType() == Node.ELEMENT_NODE) {
										Element ref = (Element) ArtifactReferenceList.item(j);
										String REF = Utils.correctName(
												java.net.URLDecoder.decode(ref.getAttribute("reference"), "UTF-8"));
										if (!RefToArtID.containsKey(REF))
											RefToArtID.put(REF, new LinkedList<String>());
										RefToArtID.get(REF).add(Utils.correctName(ID));
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

	/**
	 * Parse node Containing Implementation Artifact, to create script position ->
	 * NodeType reference
	 * 
	 * @param artifact
	 *            node
	 * @param nodeType
	 */
	private void parseImplementationsArtifact(Node artifact, String nodeType) {
		if (artifact.getNodeName().endsWith(":ImplementationArtifacts")
				|| artifact.getNodeName().equals("ImplementationArtifacts")
				|| artifact.getNodeName().endsWith(":DeploymentArtifacts")
				|| artifact.getNodeName().equals("DeploymentArtifacts")) {
			if (artifact.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) artifact;
				NodeList artifacts = elem.getChildNodes();
				for (int j = 0; j < artifacts.getLength(); j++) {
					if (artifacts.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element artifactImplementation = (Element) artifacts.item(j);
						String artifactRef = artifactImplementation.getAttribute("artifactRef");
						if (artifactRef.contains(":"))
							artifactRef = artifactRef.substring(artifactRef.indexOf(':') + 1, artifactRef.length());
						addNodeTypeRef(nodeType, Utils.correctName(artifactRef));
					}
				}
			}
		}
	}

	public void expandTOSCA_Nodes(List<String> packages, String source) throws IOException, JAXBException {
		source = encode(source);
		if (RefToNodeType.get(source) == null) {
			System.out.println("not found");
			return;
		}

		for (String nodeType : RefToNodeType.get(source)) {
			System.out.println("Found Node Type: " + nodeType);
			for (String serviceTemplate : NodeTypeToServiceTemplate.get(nodeType)) {
				System.out.println("Found Service Template: " + serviceTemplate);
				expandTOSCA_Node(packages, nodeType, serviceTemplate);
			}
		}

	}

	public void expandTOSCA_Node(List<String> packages, String nodeType, String serviceTemplate)
			throws IOException, JAXBException {
		Element deploymentArtifacts = null;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(ch.getFolder() + CSAR_handler.Definitions + serviceTemplate);
			NodeList nodes = document.getElementsByTagName("*");
			for (int i = 0; i < nodes.getLength(); i++)
				if (nodes.item(i).getNodeName().endsWith(":NodeTemplate")
						|| nodes.item(i).getNodeName().equals("NodeTemplate")) {
					String type = ((Element) nodes.item(i)).getAttribute("type");
					if (type.endsWith(":" + nodeType) || type.equals(nodeType)) {
						// right NodeTemplate found
						// need to add deployment artifacts
						Element e = (Element) nodes.item(i);
						NodeList nodeTypeChildren = e.getChildNodes();
						for (int j = 0; j < nodeTypeChildren.getLength(); j++) {
							if (nodeTypeChildren.item(j).getNodeType() == Node.ELEMENT_NODE) {
								if (nodeTypeChildren.item(j).getNodeName().endsWith(":DeploymentArtifacts")
										|| nodeTypeChildren.item(j).getNodeName().equals("DeploymentArtifacts")) {

									deploymentArtifacts = (Element) nodeTypeChildren.item(j);
									NodeList deploymentArtifactsList = deploymentArtifacts.getChildNodes();
									for (int d = 0; d < deploymentArtifactsList.getLength(); d++) {

										if (deploymentArtifactsList.item(d).getNodeType() == Node.ELEMENT_NODE) {
											if (deploymentArtifactsList.item(d).getNodeName()
													.endsWith(":DeploymentArtifact")
													|| deploymentArtifactsList.item(d).getNodeName()
															.equals("DeploymentArtifact")) {
												String depArtID = ((Element) deploymentArtifactsList.item(d))
														.getAttribute("artifactRef");
												if (packages.contains(depArtID)) {
													System.out.println("artifact exists: " + depArtID);
													packages.remove(depArtID);
												}
											}
										}
									}
								}
							}
						}
						if (deploymentArtifacts == null) {
							deploymentArtifacts = document.createElement(myPrefix + "DeploymentArtifacts");
							e.appendChild(deploymentArtifacts);
						}
						for (String packet : packages) {
							Element deploymentArtifact = document.createElement(myPrefix + "DeploymentArtifact");
							deploymentArtifact.setAttribute("xmlns:tbt",
									RR_PackageArtifactType.Definitions.ArtifactType.targetNamespace);
							deploymentArtifact.setAttribute("xmlns:art",
									RR_PackageArtifactTemplate.Definitions.targetNamespace);
							deploymentArtifact.setAttribute("name", packet);
							deploymentArtifact.setAttribute("artifactType",
									"tbt:" + RR_PackageArtifactType.Definitions.ArtifactType.name);
							deploymentArtifact.setAttribute("artifactRef",
									"art:" + RR_PackageArtifactTemplate.getID(packet));
							deploymentArtifacts.appendChild(deploymentArtifact);
						}
					}
				}
			for (String packet : packages) {
				addRRImport_DA(document, packet);
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			Result output = new StreamResult(new File(ch.getFolder() + CSAR_handler.Definitions + serviceTemplate));
			Source input = new DOMSource(document);
			transformer.transform(input, output);

		} catch (ParserConfigurationException | SAXException | IOException | TransformerFactoryConfigurationError
				| TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Parsing Implementation nodes, looking for NodeType
	 * 
	 * @param node
	 */
	private void parseImplementationsNodes(Node node) {
		if (node.getNodeName().endsWith(":NodeTypeImplementation")
				|| node.getNodeName().equals("NodeTypeImplementation")) {
			Element e = (Element) node;
			String nodeType = e.getAttribute("nodeType");
			if (nodeType.contains(":"))
				nodeType = nodeType.substring(nodeType.indexOf(':') + 1, nodeType.length());
			NodeList artifactLists = e.getChildNodes();
			for (int k = 0; k < artifactLists.getLength(); k++) {
				parseImplementationsArtifact(artifactLists.item(k), nodeType);
			}
		}
	}

	/**
	 * Parse Implementations
	 * 
	 * @param file
	 */
	private void parseImplementations(File file) {

		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		// System.out.println("Parse " + file.getName());
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nodes = document.getElementsByTagName("*");
			for (int i = 0; i < nodes.getLength(); i++) {
				parseImplementationsNodes(nodes.item(i));
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * creates script position -> Node Type reference, by given Script position ->
	 * ArtifactID and ArtifactID -> NodeType
	 * 
	 * @param nodeType
	 * @param artifactID
	 */
	private void addNodeTypeRef(String nodeType, String artifactID) {
		for (String key : RefToArtID.keySet()) {
			if (RefToArtID.get(key).contains(artifactID)) {
				if (!RefToNodeType.containsKey(key))
					RefToNodeType.put(key, new LinkedList<String>());
				RefToNodeType.get(key).add(nodeType);
			}
		}
	}

	/**
	 * Parse Service templates, looking for right Node Types
	 * 
	 * @param file
	 */
	private void parseServiceTemplates(File file) {
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		// System.out.println("Parse " + file.getName());
		if (!file.getName().toLowerCase().endsWith(Tosca))
			return;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
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
						nodeType = nodeType.substring(nodeType.indexOf(':') + 1, nodeType.length());
					if (!NodeTypeToServiceTemplate.containsKey(nodeType))
						NodeTypeToServiceTemplate.put(nodeType, new LinkedList<String>());
					if (!NodeTypeToServiceTemplate.get(nodeType).contains(file.getName()))
						NodeTypeToServiceTemplate.get(nodeType).add(file.getName());
				}

			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
