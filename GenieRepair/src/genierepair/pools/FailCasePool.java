package genierepair.pools;

import genierepair.testing.FailCase;
import genierepair.testing.MyMethodInterface;

import java.util.HashMap;
import java.util.Map;

import tmp.FelipeDebug;

public class FailCasePool {
	
	private static Map<MyMethodInterface,FailCase> contents = new HashMap<MyMethodInterface,FailCase>();
	
	public static void clear(){
		FelipeDebug.debug("[FailCasePool]: clearing");
		contents.clear();
	}
	
	public static void add(MyMethodInterface mi, FailCase fc){
		if(!containsKey(mi)){
			contents.put(mi, fc);
			FelipeDebug.debug("[FailCasePool]: adding "+mi);
		} else {
			FelipeDebug.debug("[FailCasePool]: not adding 'cause already exists "+mi);
		}
	}
	
	private static boolean containsKey(MyMethodInterface mi) {
		if(contents.size()==0) return false;
		String mis = mi.toString();
		for(MyMethodInterface m : contents.keySet()){
			String ms = m.toString();
			if(ms.equals(mis)) return true;
		}
		return false;
	}

	public static FailCase get(MyMethodInterface wanted){
		return contents.get(wanted);
	}
	

}
