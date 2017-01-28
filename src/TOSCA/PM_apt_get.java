package TOSCA;

public final class PM_apt_get extends PacketManager {

	public PM_apt_get(){
		Name = "apt-get";
	}
	
	void proceed(Control_references cr) {
		if(cr == null)
			throw new NullPointerException();
		for(String s:cr.getFiles())
		{
			
		}
	}

}
