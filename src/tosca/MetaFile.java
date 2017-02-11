package tosca;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MetaFile {
	
	//Metadata file location
	public static final String filename = "TOSCA-Metadata/TOSCA.meta";

	//Metadata header
	private String head;

	//container for Metadate elements
	public static class MetaEntry {
		public String name;
		public String type;

		public MetaEntry() {
		}

		public MetaEntry(String newName, String newType) {
			name = newName;
			type = newType;
		}
	}

	//Metadata elements
	public List<MetaEntry> meta;

	/**
	 * Basic constructor
	 */
	public MetaFile() {
		meta = new LinkedList<MetaEntry>();
	}

	/**	Read Metadata from CSAR unpacked to folder
	 * @param folder with CSAR content
	 * @throws IOException
	 */
	public void init(String folder) throws IOException {
		head = "";
		meta.clear();
		BufferedReader br = new BufferedReader(
				new FileReader(folder + filename));
		String line = null;
		boolean isHead = true;
		MetaEntry entry = new MetaEntry();
		while ((line = br.readLine()) != null) {
			// read header
			if (isHead)
				if (!line.startsWith("Name:")) {
					head += line + "\n";
					continue;
				} else
					isHead = false;
			// read elements
			if (line.startsWith("Name:")) {
				String[] words = line.split("\\s+");
				entry.name = words[1];
				if ((line = br.readLine()) != null
						&& line.startsWith("Content-Type:")) {
					words = line.split("\\s+");
					entry.type = words[1];
					meta.add(entry);
					entry = new MetaEntry();
				}
			}
		}
		br.close();
	}

	/**	Add new element to Metadata
	 * @param path to new file
	 * @param type of new file
	 */
	public void addFileToMeta(String path, String type) {
		meta.add(new MetaEntry(path, type));
	}

	/** Pack Data back to file
	 * @param folder 
	 * @throws IOException
	 */
	public void pack(String folder) throws IOException {
		FileWriter bw = new FileWriter(folder + filename);
		bw.write(head);
		for (MetaEntry entry : meta)
			bw.write("Name: " + entry.name + "\nContent-Type: " + entry.type
					+ "\n\n");
		bw.flush();
		bw.close();
	}

}
