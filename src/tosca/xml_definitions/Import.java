package tosca.xml_definitions;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author jery Class for simple creating TOSCA imports
 */
public class Import {

	@XmlAttribute(name = "namespace", required = true)
	public String namespace;
	@XmlAttribute(name = "location", required = true)
	public String location;
	@XmlAttribute(name = "importType", required = true)
	public String importType;

	/**
	 * Constructor for Import
	 * 
	 * @param ns
	 *            namespace
	 * @param l
	 *            location
	 * @param it
	 *            import type
	 */
	Import(String ns, String l, String it) {
		namespace = ns;
		location = l;
		importType = it;
	}
}
