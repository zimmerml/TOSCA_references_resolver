package TOSCA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Resolver {
	static public String folder = "References_resolver";

	
	List <Language> languages;
	public static void main(String[] args) throws IOException {
		String architecture = getArchitecture();
		Bash bash = new Bash(architecture);
		Resolver resolver = new Resolver(bash);
		resolver.proceedCSAR("example.csar");
	}
	public Resolver() {
	}
	public Resolver(Language newLanguage) {
		setLanguages(newLanguage);
	}
	public Resolver(List <Language> newLanguages) {
		setLanguages(newLanguages);
	}
	public void proceedCSAR(String filename) {
		if(filename == null)
			throw new NullPointerException();

		System.out.println("Proceeding file " + filename);
		Control_references cr = new  Control_references (filename);
		try {
		for(Language l:languages)
			l.proceed(cr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cr.pack("newexample.csar");
	}
	public void setLanguages(List <Language> newLanguages) {
		if(newLanguages == null)
			throw new NullPointerException();
		for(Language l:newLanguages)
			System.out.println("Language " + l.getName() + " added to resolver");
		languages = newLanguages;
	}
	public void setLanguages(Language newLanguage) {
		if(newLanguage == null)
			throw new NullPointerException();
		languages = new LinkedList<Language>();
		languages.add(newLanguage);
		System.out.println("Language " + newLanguage.getName() + " added to resolver");
	}
	
	public static String getArchitecture()throws IOException{ 
		String architecture = "i386";
		File arch = new File(folder+"/arch");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(arch));
			String line = br.readLine();
			br.close();
			if(!line.equals(""))
				return line;
			else
			{
				new File(folder+"/arch").delete();
				throw new FileNotFoundException();
			}
				
		} catch (FileNotFoundException e) {
			new File(folder).mkdir();
				FileWriter bw = new FileWriter(arch);
				bw.write(architecture);
				bw.close();
			return architecture;
		}
	}
	public void setArchitecture(String arch) throws IOException
	{
		if(arch == null)
			throw new NullPointerException();
		File fArch = new File(folder+"/arch");
		fArch.delete();

		FileWriter bw = new FileWriter(fArch);
		bw.write(arch);
		bw.close();
	}
}
