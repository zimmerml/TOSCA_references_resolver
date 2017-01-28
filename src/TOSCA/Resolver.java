package TOSCA;

import java.util.LinkedList;
import java.util.List;

public class Resolver {

	
	List <Language> languages;
	public static void main(String[] args) {
		Bash bash = new Bash();
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
		for(Language l:languages)
			l.proceed(cr);
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
	
}
