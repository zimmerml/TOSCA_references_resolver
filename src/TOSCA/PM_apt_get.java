package TOSCA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class PM_apt_get extends PacketManager {
	
	public PM_apt_get(){
		Name = "apt-get";
	}
	
	void proceed(String filename, Control_references cr) throws IOException {
		if(cr == null)
			throw new NullPointerException();
		System.out.println("proceed " + filename);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		boolean isChanged = false;
		String line = null;
		String newFile = "";
		while ((line = br.readLine()) != null) {
			if(line.startsWith("apt-get install"))
			{
				System.out.println("apt-get found:" +line);
				isChanged = true;
				String[] parts = line.replaceAll("[;&]","").split(" ");
				for(int packet = 2; packet < parts.length; packet++) {
					System.out.println("packet: " + parts[packet]);
					newFile+=Bash.getScriptPath() + " apt-get \"" + parts[packet]+ " " + getPacket(parts[packet],cr) +"\"\n";
				}
			}
			else
				newFile+=line + '\n';
		}
		br.close();
		if(isChanged)
		{
			System.out.println("new file: "+newFile);
			File file = new File(filename);
			file.delete();

			FileWriter wScript = new FileWriter(file);
			wScript.write(newFile, 0, newFile.length());
			wScript.close();
		}
	}
	private String getPacket(String packet, Control_references cr)
	{
		String dependency = "";
		File folder = new File("./");
		for(String arch:Resolver.getArchitectures())
		{
			Process proc;
			try {
				System.out.println("apt-get download "  + packet+":"+arch);
				proc = Runtime.getRuntime().exec("apt-get download "  + packet+":"+arch);
				proc.waitFor();
				for(File entry:folder.listFiles())
					if(entry.getName().endsWith(arch+".deb") && entry.getName().startsWith(packet))
					{
						proc = Runtime.getRuntime().exec("mv "+entry.getName()+" "+cr.getFolder()+"References_resolver/Bash/"+arch+"/"+packet+"_"+arch+".deb");
						proc.waitFor();
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    
		}
		return dependency;
	}
}
