package tosca.Languages.Bash;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.CSAR_handler;
import tosca.Utils;
import tosca.Abstract.Language;
import tosca.Abstract.PackageManager;

public final class PM_aptitude extends PackageManager {

	// package manager name
	static public final String Name = "aptitude";

	/**
	 * Constructor
	 */
	public PM_aptitude(Language language, CSAR_handler new_ch) {
		this.language = language;
		this.ch = new_ch;
	}
	
	@Override
	public List<String> proceed(String filename, String source)
			throws FileNotFoundException, IOException, JAXBException {
		if (ch == null)
			throw new NullPointerException();
		List<String> output = new LinkedList<String>();
		System.out.println(Name + " proceed " + filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		boolean isChanged = false;
		String line = null;
		String newFile = "";
		while ((line = br.readLine()) != null) {
			String[] words = line.replaceAll("[;&]", "").split("\\s+");
			// skip space at the beginning of string
			int i = 0;
			if (words[i].equals(""))
				i = 1;
			// look for apt-get
			if (words.length >= 1 + i && words[i].equals("aptitude")) {
				// apt-get found
				if (words.length >= 3 + i && words[1 + i].equals("install")) {
					System.out.println("aptitude found:" + line);
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
