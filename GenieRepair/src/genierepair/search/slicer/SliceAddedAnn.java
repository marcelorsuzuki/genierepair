package genierepair.search.slicer;

import genierepair.Activator;
import genierepair.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

public class SliceAddedAnn {

	Long id;
	
	public SliceAddedAnn(Long sliceID) {
		id=sliceID;
	}
	
	public String toString(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String clazz = store.getString(PreferenceConstants.ANNOTATIONCLASS);
		String ret = "@"+clazz+"(entityID="+id+")"+System.lineSeparator();
		return ret;
	}
	
	public String fqn(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String pck = store.getString(PreferenceConstants.ANNOTATIONPACKAGE);
		String clazz = store.getString(PreferenceConstants.ANNOTATIONCLASS);
		return pck+"."+clazz;
	}
	
	public String getImport(){
		return "import "+fqn()+";"+System.lineSeparator();
	}

	
	
}
