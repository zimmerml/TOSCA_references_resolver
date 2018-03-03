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
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;

import tosca.CSAR_handler;
import tosca.Package_Handler;
import tosca.Resolver;
import tosca.Utils;
import tosca.zip;
import tosca.Abstract.Language;
import tosca.Abstract.PackageManager;
import tosca.xml_definitions.RR_PackageArtifactTemplate;

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
	 * @see TOSCA.PacketManager#proceed(java.lang.String, TOSCA.Control_references)
	 */
	public List<String> proceed(String filename, String source) throws IOException, JAXBException {
		if (ch == null)
			throw new NullPointerException();
		List<String> output = new LinkedList<String>();
		System.out.println(Name + " proceed " + filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		boolean isChanged = false;
		String line = null;
		String newFile = "";
		while ((line = br.readLine()) != null) {
			System.out.println("Checking for apt-get install... : " + line);
			// split string to words
			String[] words = line.replaceAll("[;&]", "").split("\\s+");
			// skip space at the beginning of string
			int i = 0;
			if (words[i].equals(""))
				i = i + 1;
			if (words[i].equals("sudo"))
				i = i + 1;
			if (words[i].equals("-E"))
				i = i + 1;
			// look for apt-get
			if (words.length >= 1 + i && words[i].equals("apt-get")) {
				System.out.println("apt-get found!");
				// apt-get found
				if (words.length >= 3 + i && words[1 + i].equals("install")) {
					System.out.println("apt-get install found!");
					isChanged = true;
					for (int packet = 2 + i; packet < words.length; packet++) {
						if (words[packet].startsWith("-"))
							continue;
						System.out.println("packet: " + words[packet]);
						output = ch.getPacket(language, words[packet], source);
					}
				}
				if (ch.getResolving() == CSAR_handler.Resolving.Expand) {

					newFile += "#//References resolver//" + line + '\n';
					if (output.size() > 0) {

						List<String> templist = new LinkedList<String>();
						for (String temp : output)
							templist.add(Utils.correctName(temp));

						newFile += "dpkg -i ";
						for (String temp : templist)
							newFile += " " + temp + Package_Handler.Extension;
						newFile += "\n";

						for (String packet : templist) {
							RR_PackageArtifactTemplate.createPackageArtifact(ch, packet);
						}
						language.expandTOSCA_Node(templist, source);
					}
				} else if (ch.getResolving() == CSAR_handler.Resolving.Archive) {

					if (line.contains("install")) {

						newFile += "#//References resolver//" + line + '\n';

						// TODO read a resource file to create the script

						newFile += "csarRoot=$(find ~ -maxdepth 1 -path \"*.csar\");";
						newFile += "\n";
						newFile += "IFS=';' read -ra NAMES <<< \"$DAs\";";
						newFile += "\n";
						newFile += "for i in \"${NAMES[@]}\"; do";
						newFile += "\n";
						newFile += "	IFS=',' read -ra PATH <<< \"$i\"; ";
						newFile += "\n";
						newFile += "		dirName=$(/usr/bin/sudo /usr/bin/dirname $csarRoot${PATH[1]})";
						newFile += "\n";
						newFile += "		baseName=$(/usr/bin/sudo /usr/bin/basename $csarRoot${PATH[1]})";
						newFile += "\n";
						newFile += "		filename=\"${baseName%.*}\"";
						newFile += "\n";
						newFile += "	if [[ \"${PATH[1]}\" == *.tar ]];";
						newFile += "\n";
						newFile += "	then";
						newFile += "\n";
						newFile += "	cd $dirName";
						newFile += "\n";
						newFile += "	/usr/bin/sudo mkdir -p $filename";
						newFile += "\n";
						newFile += "	/bin/tar  -xvzf $baseName -C $filename";
						newFile += "\n";
						newFile += "	fi";
						newFile += "\n";
						newFile += "done";
						newFile += "\n";
						newFile += "export DEBIAN_FRONTEND=noninteractive";
						newFile += "\n";
						newFile += "/usr/bin/sudo -E /usr/bin/dpkg -i -R -E -B $filename";
						newFile += "\n";

						String newFileName = ch.service_template.getRefToNodeType().get(Utils.correctName(source))
								.get(0);
						String newFileNameWithExtension = newFileName + "_DA.tar";

						String newFilePath1 = Resolver.folder + newFileNameWithExtension;

						// Creating tar file, since tar is already available on Ubuntu
						zip.createTarFile(newFileNameWithExtension, ch.getFolder() + Resolver.folder, "tar");

						zip.deleteFilesInFolder(new File(ch.getFolder() + Resolver.folder), "tar");

						ch.metaFile.addFileToMeta(newFilePath1, "application/tar");

						RR_PackageArtifactTemplate.createPackageArtifact(ch, newFileName);

						List<String> newDA = new LinkedList<String>();
						newDA.add(newFileName);
						language.expandTOSCA_Node(newDA, source);
					}

				} else {
					newFile += "#//References resolver//" + line + '\n';
				}
			} else
				newFile += line + '\n';
		}
		br.close();
		if (isChanged)
			Utils.createFile(filename, newFile);
		return output;
	}
}
