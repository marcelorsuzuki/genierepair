package genierepair.search.slicer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;

import tmp.FelipeDebug;

import genierepair.Activator;
import genierepair.preferences.PreferenceConstants;

public class SlicerConnector {

	private String slicerServer;
	private long entityID;

	public SlicerConnector(long eid){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		slicerServer = store.getString(PreferenceConstants.SLICE_SERVER)+"/SliceServer?entityID=";
		this.entityID=eid;
	}


	private byte[] getBytes(){
		long start = System.currentTimeMillis();
		URL url;
		try {
			url = new URL(slicerServer+entityID);
			FelipeDebug.debug("[SlicerConnector]: Retrieving url="+url);			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int bytesRead;
			InputStream stream = url.openStream();
			while ((bytesRead = stream.read(chunk)) > 0) {
				outputStream.write(chunk, 0, bytesRead);
			}
			stream.close();
			byte[] bytes = outputStream.toByteArray();
			outputStream.close();
			return bytes;
		}  catch (IOException e) {
			e.printStackTrace();
		}
		long end= System.currentTimeMillis();
		FelipeDebug.debug("[SlicerConnector]: Took "+(end-start)/1000d+" seconds");
		return new byte[0];
	}
	
	public SliceFile getSlice(){
		return new SliceFile(getBytes(),entityID);
	}




}
