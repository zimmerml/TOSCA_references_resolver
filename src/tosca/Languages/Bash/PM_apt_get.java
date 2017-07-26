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
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.CSAR_handler;
import tosca.Utils;
import tosca.Abstract.Language;
import tosca.Abstract.PackageManager;

public final class PM_apt_get extends PackageManager {

	// package manager name
	static public final String Name = "apt-get";

	/**
	 * Constructor
	 */
	public PM_apt_get(Language language, CSAR_handler new_ch) {
		this.language = language;
		ch = new_ch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TOSCA.PacketManager#proceed(java.lang.String,
	 * TOSCA.Control_references)
	 */
	public List<String> proceed(String filename, String source)
			throws IOException, JAXBException {
		if (ch == null)
			throw new NullPointerException();
		List<String> output = new LinkedList<String>();
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
					System.out.println("apt-get found:" + line);
					isChanged = true;
					for (int packet = 2 + i; packet < words.length; packet++) {
						System.out.println("packet: " + words[packet]);
						output = ch.getPacket(language, words[packet], source);
					}
				}
				newFile += "#//References resolver//" + line + '\n';
			} else
				newFile += line + '\n';
		}
		br.close();
		if (isChanged)
			Utils.createFile(filename,newFile);
		return output;
	}
}
