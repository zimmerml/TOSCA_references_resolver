package TOSCA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class PM_apt_get extends PacketManager {
	
	public PM_apt_get(String architecture){
		this.architecture = architecture;
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
					newFile+="dpkg -i "+ getPacket(parts[packet],cr) +"\n";
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
		String packets = "References_resolver/Bash/"+packet+"/"+packet+".deb" + " " ;
		File folder = new File("./");
		Process proc;
		try {
			System.out.println("apt-get download "  + packet+":"+architecture);
			proc = Runtime.getRuntime().exec("apt-get download "  + packet+":"+architecture);
			proc.waitFor();
			for(File entry:folder.listFiles())
				if(entry.getName().endsWith(architecture+".deb") && entry.getName().startsWith(packet))
				{
					File dir = new File(cr.getFolder() + "References_resolver/Bash/"+packet+"/");
					dir.mkdirs();
					entry.renameTo(new File(cr.getFolder()+"References_resolver/Bash/"+packet+"/"+packet+".deb"));
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return packets;
	}
}
