package tosca;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

	/**
	 * returns length of path, i.o. number of directories
	 * 
	 * @param file
	 * @return length
	 */
	static public int getPathLength(String file) {

		if (file == null)
			throw new NullPointerException();
		int count = 0;
		String path = new File(file).getParent();
		while (path != null) {
			path = new File(path).getParent();
			count++;
		}
		return count;
	}

	/**	Create file and all parent folders with given content
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	static public void createFile(String filename, String content) throws IOException {
		new File(new File(filename).getParent()).mkdirs();
		new File(filename).delete();
		FileWriter bw = new FileWriter(filename);
		bw.write(content);
		bw.close();
	}

}
