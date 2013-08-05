package genierepair.search.fileserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.jface.preference.IPreferenceStore;

import tmp.FelipeDebug;
import genierepair.preferences.PreferenceConstants;
import genierepair.Activator;

public class FileServerConnector {
	
	public static final int ENTITY = 2;
	private String fileServer;
	private long ID;
	
	public FileServerConnector(long id, int type){
		if(type==ENTITY){
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			fileServer = store.getString(PreferenceConstants.FILE_SERVER)+"/FileServer?entityID=";
		}
		this.ID=id;
	}
	
	
	public byte[] getBytes(){
		URL url;
		long start = System.currentTimeMillis();
		FelipeDebug.debug("[FileServerConnector]: retrieving URL= "+fileServer+ID);
		try {
			url = new URL(fileServer+ID);
			URLConnection conn = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String ret = "";
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				ret+=inputLine+"\n";
			}
			br.close();
			long end = System.currentTimeMillis();
			FelipeDebug.debug("[FileServerConnector]: took "+(end-start)/1000d+" seconds");
			return ret.getBytes();
		} catch (Exception e) {
			return e.getMessage().getBytes();
		}
	}



}
