package tosca.Abstract;

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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import tosca.Control_references;

/**
 * Package manager used by language
 * 
 * @author jery
 *
 */
public abstract class PacketManager {

	// Name of manager
	static public String Name;

	protected Language language;
	
	protected Control_references cr;
	/**
	 * Proceed given file
	 * 
	 * @param filename
	 *            file to proceed
	 * @param cr
	 *            CSAR manager
	 * @throws FileNotFoundException
	 * @throws IOExceptions
	 * @throws JAXBException
	 */
	public abstract void proceed(String filename, Control_references cr)
			throws FileNotFoundException, IOException, JAXBException;

	/**
	 * Proceed given file with different source (like archive)
	 * 
	 * @param filename
	 * @param cr
	 * @param source
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public abstract void proceed(String filename, Control_references cr,
			String source) throws FileNotFoundException, IOException,
			JAXBException;
}
