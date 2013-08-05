package genierepair.pools;

import java.util.HashMap;
import java.util.Set;

import tmp.FelipeDebug;
import tmp.MySingleResult;
import genierepair.testing.MyMethodInterface;


public class AddedSlices {
	private static HashMap<MySingleResult,MyMethodInterface> contents
		= new HashMap<MySingleResult,MyMethodInterface>();

	public static void put(MySingleResult sr, MyMethodInterface mi) {
		if(contents.containsKey(sr)){
			contents.remove(sr);
		} 
		FelipeDebug.debug("[AddedSlices pool]: Adding ("+sr+","+mi+")");
		contents.put(sr, mi);
	}
	
	public static Set<MySingleResult> getKeys(){
		return contents.keySet();
	}
	
	public static MyMethodInterface get(MySingleResult sr){
		return contents.get(sr);
	}

	public static void remove(MySingleResult msr) {
		FelipeDebug.debug("[AddedSlices pool]: Removing ("+msr+")");
		contents.remove(msr);
	}
	
	public static void remove(Long id){
		Set<MySingleResult> set = contents.keySet();
		for(MySingleResult sr : set){
			if(sr.getEntityID().equals(id)){
				FelipeDebug.debug("[AddedSlices pool]: Removing by id ("+sr+")");
				contents.remove(sr);
				return;
			}
		}
	}
}
