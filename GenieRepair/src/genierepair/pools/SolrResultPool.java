package genierepair.pools;

import genierepair.testing.MyMethodInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tmp.FelipeDebug;
import tmp.MySingleResult;

/**This class contains a map<MethodInterface,List<SingleResult>>, i.e., contains all pairs of (MethodInterface,solr results)*/
public class SolrResultPool{
	
	/**a map to store a list of SolrResults and its MethodInterface key*/
	private static Map<MyMethodInterface, List<MySingleResult>> contents = new HashMap<MyMethodInterface, List<MySingleResult>>();
	
	
	public static Set<MyMethodInterface> getMethodInterfaces(){
		return contents.keySet();
	}
	
	/**adds an array of results @param add to a key @param mi into the main container*/
	public static void add(MyMethodInterface mi, MySingleResult[] add){
		int len = add.length;
		if(!containsKey(mi)){
			contents.put(mi, new ArrayList<MySingleResult>());
		}
		List<MySingleResult> l = contents.get(mi);
		for(int i=0;i<len;++i){
			if(!l.contains(add[i])) l.add(add[i]);
		}
	}
	
	
	/**adds a list of results @param l to a key @param mi into the main container*/
	public static void add(MyMethodInterface mi,List<MySingleResult> l){
		FelipeDebug.debug("[SolrResultPool]: adding "+mi+" with results:");
		if(!containsKey(mi)){
			contents.put(mi, new ArrayList<MySingleResult>());
		}
		List<MySingleResult> l2 = contents.get(mi);
		int len = l.size();
		for(int i=0;i<len;++i){
			if(!l2.contains(l.get(i))) {
				l2.add(l.get(i));
				FelipeDebug.debug(SolrResultPool.class,l.get(i)+"");
			}
		}
	}
	
	
	/**get all the SingleResults founds for the @param mi*/
	public static List<MySingleResult> getContents(MyMethodInterface mi){
		return contents.get(mi);
	}
	
	public static void clear(){
		FelipeDebug.debug("[SolrResultPool]: clearing");
		contents.clear();
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
	
}
