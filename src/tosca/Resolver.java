package tosca;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Abstract.Language;
import tosca.Languages.Ansible.Ansible;
import tosca.Languages.Bash.Bash;
import tosca.xml_definitions.RR_DependsOn;
import tosca.xml_definitions.RR_NodeType;
import tosca.xml_definitions.RR_PackageArtifactType;
import tosca.xml_definitions.RR_ScriptArtifactType;

/**
 * @author jery
 *
 */
public class Resolver {

	// Folder Name, used by this framework
	static public final String folder = "References_resolver" + File.separator;

	// Active languages
	List<Language> languages;

	/**
	 * Test, creates resolver with Bash language
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String source, target;
		Bash bash = new Bash();
		Ansible ansible = new Ansible();
		Resolver resolver = new Resolver(bash);
		resolver.addLanguage(ansible);
		if (args.length >= 1)
			source = args[0];
		else {
			System.out.print("input:");
			source = new Scanner(System.in).nextLine();
			if (source.equals(""))
				source = "example.csar";
		}
		if (args.length >= 2)
			target = args[1];
		else {
			System.out.print("output:");
			target = new Scanner(System.in).nextLine();
			if (target.equals(""))
				target = "newexample.csar";
		}
		System.out.println("source: " + source);
		System.out.println("target: " + target);
		resolver.proceedCSAR(source, target);
	}

	/**
	 * Constructor
	 */
	public Resolver() {
	}

	/**
	 * Construct resolver and assign one language
	 * 
	 * @param newLanguage
	 */
	public Resolver(Language newLanguage) {
		setLanguage(newLanguage);
	}

	/**
	 * COnstruct resolver and assign list with languages
	 * 
	 * @param newLanguages
	 *            , list with languages
	 */
	public Resolver(List<Language> newLanguages) {
		setLanguages(newLanguages);
	}

	/**
	 * proceed CSAR
	 * 
	 * @param filename
	 *            input CSAR name
	 * @param output
	 *            output CSAR name
	 * @throws IOException
	 */
	public void proceedCSAR(String filename, String output) throws IOException {
		if (filename == null || output == null)
			throw new NullPointerException();

		System.out.println("Proceeding file " + filename);
		Control_references cr;
		try {
			// create CSAR manager and unpack archive
			cr = new Control_references(filename);
			/*
			 * TODO
			 * Node type. done
			 * Artifact Type for package. done
			 * Relationship Type for dependencies 
			 * change service template
			 */
			new File(cr.getFolder() +  Control_references.Definitions).mkdirs();
			RR_NodeType.init(cr);
			RR_PackageArtifactType.init(cr);
			RR_ScriptArtifactType.init(cr);
			RR_DependsOn.init(cr);
			
		} catch (FileNotFoundException e) {
			System.out.println("Error by unpacking " + filename
					+ ", file not found");
			return;
		} catch (JAXBException e) {
			System.out
					.println("Unable to create a XML packagetype description");
			e.printStackTrace();
			return;
		}
		// proceed all extracted files using each available language
		try {
			for (Language l : languages)
				l.proceed(cr);
		} catch (JAXBException e1) {
			System.out.println("Unable to create xml annotation to package");
			e1.printStackTrace();
		}
		// pack CSAR
		try {
			cr.pack(output);
			System.out.println("packed to: " + output);
		} catch (FileNotFoundException e) {
			System.out.println("File: not found during packing to: " + output);
			return;
		}
	}

	/**
	 * Set new Languages
	 * 
	 * @param newLanguages
	 */
	public void setLanguages(List<Language> newLanguages) {
		if (newLanguages == null)
			throw new NullPointerException();
		for (Language l : newLanguages)
			System.out
					.println("Language " + l.getName() + " added to resolver");
		languages = newLanguages;
	}

	/**
	 * Add new Languages
	 * 
	 * @param newLanguages
	 */
	public void addLanguages(List<Language> newLanguages) {
		if (newLanguages == null)
			throw new NullPointerException();
		for (Language l : newLanguages)
			System.out
					.println("Language " + l.getName() + " added to resolver");
		languages.addAll(newLanguages);
	}

	/**
	 * Set new Language
	 * 
	 * @param newLanguage
	 */
	public void setLanguage(Language newLanguage) {
		if (newLanguage == null)
			throw new NullPointerException();
		languages = new LinkedList<Language>();
		languages.add(newLanguage);
		System.out.println("Language " + newLanguage.getName()
				+ " added to resolver");
	}

	/**
	 * Add new Language
	 * 
	 * @param newLanguage
	 */
	public void addLanguage(Language newLanguage) {
		if (newLanguage == null)
			throw new NullPointerException();
		languages.add(newLanguage);
		System.out.println("Language " + newLanguage.getName()
				+ " added to resolver");
	}
}
