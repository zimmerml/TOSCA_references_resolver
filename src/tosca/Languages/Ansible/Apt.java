package tosca.Languages.Ansible;

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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import tosca.CSAR_handler;
import tosca.Package_Handler;
import tosca.Utils;
import tosca.Abstract.Language;
import tosca.Abstract.PackageManager;
import tosca.xml_definitions.RR_PackageArtifactTemplate;

public class Apt extends PackageManager {

	// package manager name
	static public final String Name = "apt";

	public Apt(Language language, CSAR_handler new_ch) {
		this.language = language;
		this.ch = new_ch;
	}

	/*
	 * Ansible reader (non-Javadoc)
	 * 
	 * @see tosca.Abstract.PacketManager#proceed(java.lang.String,
	 * tosca.Control_references, java.lang.String)
	 */
	@Override
	public List<String> proceed(String filename, String source)
			throws FileNotFoundException, IOException, JAXBException {
		if (ch == null)
			throw new NullPointerException();
		List<String> output = new LinkedList<String>();
		String prefix = "    - ";
		for (int i = 0; i < Utils.getPathLength(filename) - 1; i++)
			prefix = prefix + "../";
		System.out.println(Name + " proceed " + filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		boolean isChanged = false;
		String line = null;
		String newFile = "";
		int State = 0;
			while ((line = br.readLine()) != null) {
				switch (State) {
				case 0:
					// Search for task name = start of tast
					if (line.matches("-\\s*name.*"))
						State = 1;
					newFile += line + '\n';
					break;
				case 1:
					// task name found,
					// search for different possibilities of apt
					if (line.matches("\\s*apt:.*pkg\\s*=\\s*\\{.*item.*\\}.*")) {
						// apt = { item }
						State = 2;
						newFile += "#//References resolver//" + line + '\n';
					} else if (line.matches("\\s*apt:.* pkg=.*")) {
						Pattern p = Pattern
								.compile("(apt:.*pkg=)\\s*(\\w*)\\s*.*");
						Matcher m = p.matcher(line);
						if (m.find()) {
							System.out.println("Found packet: " + m.group(2));
							newFile += "#//References resolver//" + line + '\n';
							output = ch.getPacket(language, m.group(2), source);
							if(ch.getResolving() == CSAR_handler.Resolving.Expand && output.size() > 0){
								List<String> templist = new LinkedList<String>();
								for(String temp:output)
									templist.add(Utils.correctName(temp));
								
								newFile +=  "dpkg -i ";
								for(String temp:templist)
									newFile +=" "+ temp + Package_Handler.Extension;
								newFile += "\n";
								
								for(String packet:templist){
									RR_PackageArtifactTemplate.createPackageArtifact(ch, packet);
								}
								language.expandTOSCA_Node(templist, source);
							}
							isChanged = true;
							State = 0;
						}
						break;
					} else if (line.matches("\\s*apt:.*")) {
						State = 4;
					} else
						newFile += line + '\n';
					break;
				case 2:
					if (line.matches("-\\s*name.*")) {
						// next task
						State = 1;
						newFile += line + '\n';
					} else if (line.matches("\\s*with_items:\\s*")) {
						State = 3;
						newFile += "#//References resolver//" + line + '\n';
					}
					break;
				case 3:
					if (line.matches("-\\s*name.*")) {
						// next task
						State = 1;
						newFile += line + '\n';
					} else {
						String[] words = line.split("\\s+");
						int i = 0;
						if (words.length > 0) {
							if (words[0].equals(""))
								i = 1;
							if (words.length == 2 + i && words[i].equals("-")) {
								output = ch.getPacket(language, words[i + 1], source);
								newFile += "#//References resolver//" + line
										+ '\n';
								if(ch.getResolving() == CSAR_handler.Resolving.Expand && output.size() > 0){
									List<String> templist = new LinkedList<String>();
									for(String temp:output)
										templist.add(Utils.correctName(temp));
									
									newFile +=  "dpkg -i ";
									for(String temp:templist)
										newFile +=" "+ temp + Package_Handler.Extension;
									newFile += "\n";
									
									for(String packet:templist){
										RR_PackageArtifactTemplate.createPackageArtifact(ch, packet);
									}
									language.expandTOSCA_Node(templist, source);
								}
								isChanged = true;
							} else
								newFile += line + '\n';
						} else
							newFile += line + '\n';
					}
					break;
				case 4:
					if (line.matches("-\\s*name.*")) {
						// next task
						State = 1;
						newFile += line + '\n';
					} else if (line.matches("\\s*pkg:.*")) {
						Pattern p = Pattern.compile("(\\s*pkg:)\\s*(\\w*)\\s*");
						Matcher m = p.matcher(line);
						if (m.find()) {
							System.out.println("Found packet: " + m.group(2));
							output = ch.getPacket(language, m.group(2), source);
							newFile += "#//References resolver//" + line + '\n';
							if(ch.getResolving() == CSAR_handler.Resolving.Expand && output.size() > 0){
								List<String> templist = new LinkedList<String>();
								for(String temp:output)
									templist.add(Utils.correctName(temp));
								
								newFile +=  "dpkg -i ";
								for(String temp:templist)
									newFile +=" "+ temp + Package_Handler.Extension;
								newFile += "\n";
								
								for(String packet:templist){
									RR_PackageArtifactTemplate.createPackageArtifact(ch, packet);
								}
								language.expandTOSCA_Node(templist, source);
							}
							isChanged = true;
							State = 0;
						}
					} else if (line.matches("\\s*deb:.*")) {
						newFile += "  apt:\n" + line + '\n';
					}
					break;
				}

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
		return output;
	}
}
