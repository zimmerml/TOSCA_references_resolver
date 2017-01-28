package TOSCA;

import java.io.File;
import java.io.FileInputStream;
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
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    static public List<String> unZipIt(String zipFile, String outputFolder){
     List<String> fileList = new LinkedList<String>();

     byte[] buffer = new byte[1024];

     try{

    	//create output directory is not exists
    	File folder = new File(outputFolder);
    	if(!folder.exists()){
    		folder.mkdir();
    	}

    	//get the zip file content
    	ZipInputStream zis =
    		new ZipInputStream(new FileInputStream(zipFile));
    	//get the zipped file list entry
    	ZipEntry ze = zis.getNextEntry();

    	while(ze!=null){

    	   String fileName = ze.getName();
           File newFile = new File(outputFolder + File.separator + fileName);

           System.out.println("file unzip : "+ newFile.getAbsoluteFile());
           fileList.add(ze.getName());
            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
       		fos.write(buffer, 0, len);
            }

            fos.close();
            ze = zis.getNextEntry();
    	}

        zis.closeEntry();
    	zis.close();

    	System.out.println("Done");

    }catch(IOException ex){
       ex.printStackTrace();
    }
 	return fileList;
   }


    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    static public void zipIt(String zipFile, String folder, List<String> fileList){

     byte[] buffer = new byte[1024];

     try{

    	FileOutputStream fos = new FileOutputStream(zipFile);
    	ZipOutputStream zos = new ZipOutputStream(fos);

    	System.out.println("Output to Zip : " + zipFile);

    	for(String file : fileList){

    		System.out.println("File Added : " + file);
    		ZipEntry ze= new ZipEntry(file);
        	zos.putNextEntry(ze);

        	FileInputStream in =
                       new FileInputStream(folder + File.separator + file);

        	int len;
        	while ((len = in.read(buffer)) > 0) {
        		zos.write(buffer, 0, len);
        	}

        	in.close();
    	}

    	zos.closeEntry();
    	//remember close it
    	zos.close();

    	System.out.println("Done");
    }catch(IOException ex){
       ex.printStackTrace();
    }
   }



}
