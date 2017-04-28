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

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author jery Class for simple creating TOSCA imports
 */
public class RR_Import {

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
	RR_Import(String ns, String l, String it) {
		namespace = ns;
		location = l;
		importType = it;
	}
}
