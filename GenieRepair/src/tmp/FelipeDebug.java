package tmp;

import edu.uci.ics.sourcerer.services.search.adapter.SingleResult;

public class FelipeDebug {
	private static boolean print = true;
	private static boolean errPrint = true;

	public static void debug(String string) {
		if(print) printf(string);
	}
	
	public static void debug(Class<?> o, String string) {
		if(print) printf("["+o.getSimpleName()+"]: "+string);
	}
	
	public static void errDebug(Class<?> o, String string) {
		if(print) errDebug("["+o.getSimpleName()+"]: "+string);
	}
	
	public static void debug(Object[] o){
		for(Object o2:o){
			debug(o2);
		}
	}

	public static void debug(Object o2) {
		debug(o2.toString());
	}

	private static void printf(String string) {
		System.out.println(string);
	}
	
	public static String toString(SingleResult sr){
		String fqn = sr.getFqn();
		String ret = sr.getReturnFqn();
		String params = sr.getParams();
		long eid = sr.getEntityID();
		
		return ret+" "+fqn+"("+params+") ["+eid+"]";
	}

	public static void errDebug(Object o) {
		errDebug(o.toString());
	} 
	
	public static void errDebug(String string) {
		if(errPrint) printErr(string);
	}

	private static void printErr(String string) {
		System.err.println(string);
	}
	
	public static void TODO(){
		throw new RuntimeException("NOT IMPLEMENTED YET");
	}

}
