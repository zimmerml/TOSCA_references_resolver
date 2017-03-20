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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class zip {

	/**
	 * Unzip it
	 * 
	 * @param zipFile
	 *            input zip file
	 * @param outputFolder
	 *            unpacked files output folder
	 * @throws FileNotFoundException
	 *             , IOException
	 */
	static public List<String> unZipIt(String zipFile, String outputFolder)
			throws FileNotFoundException, IOException {
		if (!(new File(zipFile).exists()))
			throw new FileNotFoundException(zipFile + "not found!");
		// unpacked files
		List<String> fileList = new LinkedList<String>();
		// buffer
		byte[] buffer = new byte[1024];

		// create output directory if not exists
		File folder = new File(outputFolder);
		// if (folder.exists()) {
		// delete(folder);
		// }
		folder.mkdir();

		// get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));

		// get the zipped file list entry
		ZipEntry ze;
		ze = zis.getNextEntry();

		while (ze != null) {

			String fileName = ze.getName();
			File newFile = new File(outputFolder + fileName);
			if (!ze.isDirectory()) {
				fileList.add(ze.getName());

				// create all non exists folders
				new File(newFile.getParent()).mkdirs();

				// fill file
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
			} else
				newFile.mkdirs();

			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();

		return fileList;
	}

	/**
	 * Generate list with every file in the folder recursively
	 * 
	 * @param node
	 *            current folder
	 * @param fileList
	 *            list of files in folder
	 * @param folder
	 *            original folder
	 * @return
	 */
	public static List<String> generateFileList(File node,
			List<String> fileList, String folder) {

		// add file only
		if (node.isFile()) {
			String file = node.toString();
			fileList.add(file.substring(folder.length(), file.length()));

		}

		// recursive call
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				fileList = generateFileList(new File(node, filename), fileList,
						folder);
			}
		}
		return fileList;
	}

	/**
	 * Zip all files in folder
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 * @param folder
	 *            , containing files to zip
	 * @throws FileNotFoundException
	 *             , IOException
	 */
	static public void zipIt(String zipFile, String folder)
			throws FileNotFoundException, IOException {

		List<String> fileList = new LinkedList<String>();
		fileList = generateFileList(new File(folder), fileList, folder);
		byte[] buffer = new byte[1024];

		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);

		for (String file : fileList) {
			ZipEntry ze = new ZipEntry(file);
			zos.putNextEntry(ze);
			FileInputStream in = new FileInputStream(folder + file);

			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}

			in.close();
		}

		zos.closeEntry();
		zos.close();
	}

	/**
	 * recursively delete files, folders and all subfolders
	 * 
	 * @param f
	 * @throws IOException
	 */
	static public void delete(File f) throws IOException {
		if (!f.exists())
			return;
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: " + f);
	}
}
