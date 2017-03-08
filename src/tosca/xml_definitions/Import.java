package tosca.xml_definitions;

import javax.xml.bind.annotation.XmlAttribute;

public class Import {

	@XmlAttribute(name = "namespace", required = true)
	public String namespace;
	@XmlAttribute(name = "location", required = true)
	public String location; 
	@XmlAttribute(name = "importType", required = true)
	public String importType; 
	Import(String ns, String l, String it) {
		namespace = ns;
		location = l;
		importType = it;
	}
}
