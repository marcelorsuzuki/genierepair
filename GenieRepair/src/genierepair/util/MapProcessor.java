package genierepair.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

import tmp.FelipeDebug;

public  class MapProcessor<T extends IMember> {
	
	

	private HashMap<String, T> toAdd = new HashMap<String, T>();

	/**if the @param target contains one element in @param source, then renames it*/
	public MapProcessor(T[] source, T[] target){
		HashMap<String, T> tfieldmap = new HashMap<String,T>();
		for(T f : target){
			tfieldmap.put(getName(f), f);
		}
		toAdd = new HashMap<String,T>();
		for(T f : source){
			toAdd.put(getName(f), f);
			if(tfieldmap.containsKey(getName(f))){
				//rename
				T f1 = tfieldmap.get(getName(f));
				equalsAction(f,f1);
			}
		}
	}
	
	public Map<String, T> getToAdd(){return toAdd;}
	
	public String getName(T t){
		return t.getElementName();
	}
	
	public void equalsAction(T src, T target){
		try {
			target.rename("original_copy_"+target.getElementName(), false, null);
		} catch (JavaModelException e) {
			FelipeDebug.errDebug("Could not rename method: "+getName(src));
		}
	}
}
