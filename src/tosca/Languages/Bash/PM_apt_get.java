package tosca.Languages.Bash;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import tosca.Control_references;
import tosca.Utils;
import tosca.Abstract.Language;
import tosca.Abstract.PacketManager;

public final class PM_apt_get extends PacketManager {

	// package manager name
	static public final String Name = "apt-get";

	/**
	 * Constructor
	 */
	public PM_apt_get(Language language) {
		this.language = language;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TOSCA.PacketManager#proceed(java.lang.String,
	 * TOSCA.Control_references)
	 */
	public void proceed(String filename, Control_references cr, String source)
			throws IOException, JAXBException {
		String prefix = "";
		for (int i = 0; i < Utils.getPathLength(filename) - 1; i++)
			prefix = prefix + "../";
		if (cr == null)
			throw new NullPointerException();
		System.out.println(Name + " proceed " + filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		boolean isChanged = false;
		String line = null;
		String newFile = "";
		while ((line = br.readLine()) != null) {
			// split string to words
			String[] words = line.replaceAll("[;&]", "").split("\\s+");
			// skip space at the beginning of string
			int i = 0;
			if (words[i].equals(""))
				i = 1;
			// look for apt-get
				if (words.length >= 1 + i && words[i].equals("apt-get")) {
					// apt-get found
					if (words.length >= 3 + i && words[1 + i].equals("install")) {
						// replace "apt-get install" by "dpkg -i"
						System.out.println("apt-get found:" + line);
						isChanged = true;
						for (int packet = 2 + i; packet < words.length; packet++) {
							System.out.println("packet: " + words[packet]);
//							cr.AddDependenciesScript(source, words[packet]);
							cr.getPacket(language, words[packet], source);
						}
					}
					newFile += "#//References resolver//" + line + '\n';
				} else
					newFile += line + '\n';
		}
		br.close();
		if (isChanged) {
			// references found, need to replace file
			// delete old
			File file = new File(filename);
			file.delete();

			// create new file
			FileWriter wScript = new FileWriter(file);
			wScript.write(newFile, 0, newFile.length());
			wScript.close();
		}
	}

	@Override
	public void proceed(String filename, Control_references cr)
			throws FileNotFoundException, IOException, JAXBException {
		proceed(filename, cr, filename);
	}

}
