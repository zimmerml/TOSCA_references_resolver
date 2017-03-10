package tosca.xml_definitions;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tosca.Control_references;


//TODO abstract NodeType? 1 Imlementation  -> n NodeTypes ??

public class Service_Template {
	
	public static final String Tosca = ".tosca";
	
	public static final String Definitions = "Definitions/";
	List<RR_Link> links;
	List<ArtifactsRef> artifactsRefs;
	List<NodeRef> nodeRefs;
	
	public static class RR_Link{
		public String Reference;
		public List<String> NodeTypeName;
		public List<String> ServiceTemplateFile;
		public RR_Link(String ref, String NTN, String STF){
			NodeTypeName = new LinkedList<String>();
			ServiceTemplateFile = new LinkedList<String>();
			Reference = ref;
			NodeTypeName.add(NTN);
			ServiceTemplateFile.add(STF);
		}
	}
	public static class ArtifactsRef{
		public String reference;
		public String artifactID;
		public ArtifactsRef(String ref, String AID){
			reference = ref;
			artifactID = AID;
		}
		public boolean equals(Object o ){
			return ((ArtifactsRef)o).reference.equals(reference) && ((ArtifactsRef)o).artifactID.equals(artifactID);
		}
	}
	public static class NodeRef{
		public String Reference;
		public List<String> NodeTypeName;
		public NodeRef(String ref, String NTN){
			NodeTypeName = new LinkedList<String>();
			Reference = ref;
			NodeTypeName.add(NTN);
		}
	}
	
	public Service_Template(){
		links = new LinkedList<RR_Link>() ;
		artifactsRefs = new LinkedList<ArtifactsRef>() ;
		nodeRefs = new LinkedList<NodeRef>() ;
	}
	
	public Service_Template(Control_references cr){
		links = new LinkedList<RR_Link>() ;
		artifactsRefs = new LinkedList<ArtifactsRef>() ;
		nodeRefs = new LinkedList<NodeRef>() ;
		init(cr);
	}
	
	
	public void init(Control_references cr){

		links.clear();
		artifactsRefs.clear();
		nodeRefs.clear();
		
		File folder = new File(cr.getFolder() +  Definitions);
		if(!folder.exists())
			return;
		System.out.println("Parse Artifacts");
		for (File entry : folder.listFiles()){
			parseArtifacts(entry);
		}
		for (ArtifactsRef entry : artifactsRefs){
			System.out.println(entry.artifactID + " : " + entry.reference);
		}
		System.out.println("Parse Implementations");
		for (File entry : folder.listFiles()){
			parseImplementations(entry);
		}
		System.out.println("Parse ServiceTemplates");
		for (File entry : folder.listFiles()){
			parseServiceTemplates(entry);
		}
	}
	

	//adds Templates and Relation
	public static void addDependencyToPacket(Control_references cr, String source_packet, String target_packet){
		
	}

	public static void addDependencyToScript(Control_references cr, String script_filename, String target_packet){
		
	}
	private void parseArtifacts(File file){
		System.out.println("Parse " + file.getName());
		if(!file.getName().toLowerCase().endsWith(Tosca))
			return;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nodes = document.getElementsByTagName("*");
			for(int i = 0; i < nodes.getLength(); i++){
				if(nodes.item(i).getNodeName().endsWith(":ArtifactTemplate") || nodes.item(i).getNodeName().equals("ArtifactTemplate")){
					Element e = (Element)nodes.item(i);
					String ID = e.getAttribute("id");
					NodeList ArtifactReferences = e.getChildNodes();
					for(int k = 0; k < ArtifactReferences.getLength(); k++){
						if(ArtifactReferences.item(k).getNodeName().endsWith(":ArtifactReferences") ||ArtifactReferences.item(k).getNodeName().equals("ArtifactReferences") ){
							if(ArtifactReferences.item(k).getNodeType() == Node.ELEMENT_NODE){
								Element elem = (Element)ArtifactReferences.item(k);
								NodeList ArtifactReferenceList = elem.getChildNodes();
								for(int j = 0; j < ArtifactReferenceList.getLength(); j++){
									if(ArtifactReferenceList.item(j).getNodeType() == Node.ELEMENT_NODE){
										Element ref = (Element)ArtifactReferenceList.item(j);
										String REF = ref.getAttribute("reference");
										if(!artifactsRefs.contains(new ArtifactsRef(REF,ID)))
											artifactsRefs.add(new ArtifactsRef(REF,ID));
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
	private void parseImplementations(File file){

		if(!file.getName().toLowerCase().endsWith(Tosca))
			return;
	}
	private void parseServiceTemplates(File file){

		if(!file.getName().toLowerCase().endsWith(Tosca))
			return;
	}
	
	
}
