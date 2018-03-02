package tosca;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

	/**
	 * Create file and all parent folders with given content
	 * 
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	static public void createFile(String filename, String content) throws IOException {
		if (new File(filename).getParent() != null)
			new File(new File(filename).getParent()).mkdirs();
		new File(filename).delete();
		FileWriter bw = new FileWriter(filename);
		bw.write(content);
		bw.close();
	}

	public static String correctName(String name) {
		// return name;
		return name.replace('%', 'P').replace(':', '_').replace('+', 'p').replace('/', '_').replace('.', '_');
	}

	public static String readAllBytesJava7(String filePath) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
}
