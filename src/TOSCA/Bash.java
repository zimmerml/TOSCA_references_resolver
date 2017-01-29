package TOSCA;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public final class Bash extends Language {

	private String script;
	private static String scriptPath = "References_resolver/Bash/script.sh";
	static public String getScriptPath(){
		return scriptPath;
	}
	
	public Bash(){
		Name = "Bash";
		
		extensions = new LinkedList<String>();
		extensions.add(".sh");
		extensions.add(".bash");

		packetManagers = new LinkedList<PacketManager>();
		packetManagers.add(new PM_apt_get());
		
		script = "#!/bin/sh\n" +
				"if [$# -lt 2]; then \n" +
				"echo \"not enough arguments\"\n" + 
				"exit \n"+
				"fi \n"+
				"ARCHITECTURE = 'uname -m'\n" + 
				"case \"${ARCHITECTURE}\" in\n" +
				"	(\"x86_64\"	FOLDER = \"amd64\\\";;\n" + 
				"	(\"i386\"	FOLDER = \"i386\\\";;\n" + 
				"esac\n"  + 
				"case \"$1\" in\n" +
				"	(\"apt-get\"	COMMAND = \"dpkg \";;\n" + 
				"	(\"aptitude\"	COMMAND = \"i386\";;\n" + 
				"esac\n"  + 
				"cd $FOLDER\n"+
				"$COMMAND $2";
	}
	protected void Init(Control_references cr) throws IOException
	{
		String path = cr.getFolder()+scriptPath;
		File fScript = new File(path);
		if(fScript.exists()) {
			fScript.delete();
			fScript = new File(path);
		}
		fScript.getParentFile().mkdirs();
		fScript.createNewFile();
		FileWriter wScript = new FileWriter(fScript);
		wScript.write(script, 0, script.length());
		wScript.close();
		for(String arch:Resolver.getArchitectures()){
			File dir = new File(cr.getFolder()+"References_resolver/Bash/" + arch+"/");
			dir.mkdir();
		}
	}
}
