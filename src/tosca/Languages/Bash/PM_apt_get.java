package tosca.Languages.Bash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import tosca.Control_references;
import tosca.Utils;
import tosca.Abstract.PacketManager;

public final class PM_apt_get extends PacketManager {

	// package manager name
	static public final String Name = "apt-get";
	/**
	 * Constructor
	 */
	public PM_apt_get() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TOSCA.PacketManager#proceed(java.lang.String,
	 * TOSCA.Control_references)
	 */
	public void proceed(String filename, Control_references cr)
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
			switch(cr.getResolving()){
			case EXPANDING:
				if (words.length >= 1 + i && words[i].equals("apt-get")) {
					// apt-get found
					if (words.length >= 3 + i && words[1 + i].equals("install")) {
						// replace "apt-get install" by "dpkg -i"
						System.out.println("apt-get found:" + line);
						isChanged = true;
						for (int packet = 2 + i; packet < words.length; packet++) {
							System.out.println("packet: " + words[packet]);
							newFile += "dpkg -i ";
							for (String p : cr.getPacket(words[packet]).split("\\s+"))
								newFile += prefix + p + " ";
							newFile += "\n";
						}
					} else {
						// comment apt-get
						newFile += "#//References resolver//" + line + '\n';
					}
				} else
					newFile += line + '\n';
				break;
			case ADDITION:
				if (words.length >= 1 + i && words[i].equals("apt-get")) {
					// apt-get found
					if (words.length >= 3 + i && words[1 + i].equals("install")) {
						// replace "apt-get install" by "dpkg -i"
						System.out.println("apt-get found:" + line);
						isChanged = true;
						for (int packet = 2 + i; packet < words.length; packet++) {
							System.out.println("packet: " + words[packet]);
							cr.getPacket(words[packet]);
						}
					} 
					newFile += "#//References resolver//" + line + '\n';
				} else
					newFile += line + '\n';
				break;
			default:
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
	}

}
