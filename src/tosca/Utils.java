package tosca;

import java.io.File;

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

}
