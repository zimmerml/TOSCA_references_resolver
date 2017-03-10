package tosca.xml_definitions;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import tosca.Control_references;


//TODO abstract NodeType? 1 Imlementation  -> n NodeTypes ??

public class Service_Template {
	public static final String Definitions = "Definitions/";
	List<RR_Link> links;
	List<ArtifactsRef> artifactsRefs;
	List<NodeRef> nodeRefs;
	
	public static class RR_Link{
		public String Reference;
		public String NodeTypeName;
		public String ServiceTemplateFile;
		public RR_Link(String ref, String NTN, String STF){
			Reference = ref;
			NodeTypeName = NTN;
			ServiceTemplateFile = STF;
		}
	}
	public static class ArtifactsRef{
		public String Reference;
		public String ArtifactFile;
		public ArtifactsRef(String ref, String NTN, String AF){
			Reference = ref;
			ArtifactFile = AF;
		}
	}
	public static class NodeRef{
		public String Reference;
		public String NodeTypeName;
		public NodeRef(String ref, String NTN){
			Reference = ref;
			NodeTypeName = NTN;
		}
	}
	
	public Service_Template(){
		links = new LinkedList<RR_Link>() ;
		artifactsRefs = new LinkedList<ArtifactsRef>() ;
		nodeRefs = new LinkedList<NodeRef>() ;
	}
	
	public Service_Template(Control_references cr){
		links.clear();
		artifactsRefs.clear();
		nodeRefs.clear();
		init(cr);
	}
	
	
	public void init(Control_references cr){

		File folder = new File("./" + Definitions);
		for (File entry : folder.listFiles()){
			parseArtifacts(entry);
		}
		for (File entry : folder.listFiles()){
			parseImplementations(entry);
		}
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
		
	}
	private void parseImplementations(File file){
		
	}
	private void parseServiceTemplates(File file){
		
	}
	
	
}
