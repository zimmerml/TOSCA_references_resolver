package TOSCA;

import java.io.File;
import java.util.List;

//unpack 
public class Control_references {
	private String CSAR;
	private String folder;
	private List <String> files;
	public Control_references() {
		
	}

	public Control_references(String filename) {
		init(filename);
	}
	
	public void init(String filename) {
		if(filename == null)
			throw new NullPointerException();
		CSAR = filename;
		unpack();
	}
	public List<String>getFiles(){
		return files;
	}
	private void unpack(){
		folder = CSAR+"_temp_references_resolver";
		File folderfile = new File(folder);
		folder = folderfile.getAbsolutePath();
		files = zip.unZipIt(CSAR, folder);
		System.out.println(CSAR + " unpacked ");
		for(String s:files)
			System.out.println(s);
	}
	public void pack(String filename){
		if(filename == null)
			throw new NullPointerException();
		//change CSAR ?
		zip.zipIt(filename,folder, files);
	}
	public String getCSARname(){
		return CSAR;
	}
}
