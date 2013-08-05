package genierepair.pools;

import java.util.HashMap;

import tmp.FelipeDebug;

import genierepair.search.slicer.SliceFile;

public class SlicePool {

	
	private static HashMap<Long, SliceFile> contents = new HashMap<Long, SliceFile>();
	
	public static SliceFile get(Long id) {
		return contents.get(id);
	}
	
	public static void add(Long id, SliceFile sf){
		if(contents.containsKey(id)){
			contents.remove(id);
		}
		FelipeDebug.debug("[SlicePool]: adding "+id);
		//FelipeDebug.debug("Registering "+id+" into SlicePool");
		contents.put(id, sf);
	}
	

}
