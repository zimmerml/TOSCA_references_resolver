package TOSCA;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * @author jery
 *
 */
public class Resolver {
	//Folder Name, used by this framework
	static public final String folder = "References_resolver/";
	
	//Active langages
	List <Language> languages;
	
	
	/** Test, creates resolver with Bash language
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String source,target;
		Bash bash = new Bash();
		Resolver resolver = new Resolver(bash);
		if(args.length >=1)
			source = args[0];
		else{
			System.out.print("input:");
			source = new Scanner(System.in).nextLine();
			if(source.equals(""))
				source = "example.csar";
		}
		if(args.length >=2)
			target = args[1];
		else{
			System.out.print("output:");
			target = new Scanner(System.in).nextLine();
			if(target.equals(""))
				target = "newexample.csar";
		}
		System.out.println("source: "+ source);
		System.out.println("target: "+ target);
		resolver.proceedCSAR(source, target );
	}
	
	/**
	 * Constructor
	 */
	public Resolver() {
	}
	
	/** Construct resolver and assign one language
	 * @param newLanguage
	 */
	public Resolver(Language newLanguage) {
		setLanguages(newLanguage);
	}
	
	/** COnstruct resolver and assign list with languages
	 * @param newLanguages, list with languages
	 */
	public Resolver(List <Language> newLanguages) {
		setLanguages(newLanguages);
	}
	
	/** proceed CSAR
	 * @param filename input CSAR name 
	 * @param output output CSAR name
	 * @throws IOException
	 */
	public void proceedCSAR(String filename, String output) throws IOException {
		if(filename == null || output == null)
			throw new NullPointerException();

		System.out.println("Proceeding file " + filename);
		Control_references cr;
		try{
			//create CSAR manager and unpack archive
		cr = new  Control_references (filename);
		}
		catch(FileNotFoundException e){
			System.out.println("File: " + filename+" not found");
			return;
		}
		//proceed all extracted files using every language
		for(Language l:languages)
			l.proceed(cr);
		//pack CSAR
		try {
			cr.pack(output);
		} catch (FileNotFoundException e) {
			System.out.println("File: " + output+" not found");
			return;
		}
	}
	
	/** Set new Languages
	 * @param newLanguages
	 */
	public void setLanguages(List <Language> newLanguages) {
		if(newLanguages == null)
			throw new NullPointerException();
		for(Language l:newLanguages)
			System.out.println("Language " + l.Name + " added to resolver");
		languages = newLanguages;
	}
	
	/** Set new Language
	 * @param newLanguage
	 */
	public void setLanguages(Language newLanguage) {
		if(newLanguage == null)
			throw new NullPointerException();
		languages = new LinkedList<Language>();
		languages.add(newLanguage);
		System.out.println("Language " + newLanguage.Name + " added to resolver");
	}
	

}
