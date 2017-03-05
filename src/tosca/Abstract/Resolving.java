package tosca.Abstract;

public enum Resolving {
	UNDEFINED,// 0 
	EXPANDING,// 1 
	ADDITION;// 2

	public static Resolving fromString(String input){
		if(input.equals("REPLACEMENT"))
			return EXPANDING;
		if(input.equals("ADDITION"))
			return ADDITION;
		return UNDEFINED;
	}
	public static String toString(Resolving res){
		switch(res){
		case EXPANDING:
			return "REPLACEMENT";
		case ADDITION:
			return "ADDITION";
		default:
			return "UNDEFINED";
		}
	}

	public static Resolving fromInt(int input){
		if(input == 1)
			return EXPANDING;
		if(input == 2)
			return ADDITION;
		return UNDEFINED;
	}
	public static int toInt(Resolving res){
		switch(res){
		case EXPANDING:
			return 1;
		case ADDITION:
			return 2;
		default:
			return 0;
		}
	}
}
