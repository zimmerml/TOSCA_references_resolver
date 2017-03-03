package tosca.Abstract;

public enum Resolving {
	UNDEFINED,// 0 
	REPLACEMENT,// 1 
	ADDITION;// 2

	public static Resolving fromString(String input){
		if(input.equals("REPLACEMENT"))
			return REPLACEMENT;
		if(input.equals("ADDITION"))
			return ADDITION;
		return UNDEFINED;
	}
	public static String toString(Resolving res){
		switch(res){
		case REPLACEMENT:
			return "REPLACEMENT";
		case ADDITION:
			return "ADDITION";
		default:
			return "UNDEFINED";
		}
	}

	public static Resolving fromInt(int input){
		if(input == 1)
			return REPLACEMENT;
		if(input == 2)
			return ADDITION;
		return UNDEFINED;
	}
	public static int toInt(Resolving res){
		switch(res){
		case REPLACEMENT:
			return 1;
		case ADDITION:
			return 2;
		default:
			return 0;
		}
	}
}
