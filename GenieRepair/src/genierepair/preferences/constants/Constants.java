package genierepair.preferences.constants;

import genierepair.Activator;
import genierepair.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

public class Constants {
	
	public static final String SLICEANNOTATION = "@FromSlice(entityID=";
	public static final String ENTITYIDANNOTATION = "ENTITYID = ";
	public static final String OPENCOMMENT = "/**";
	public static final String CLOSECOMMENT = "*/";
	public static final String LINE = "\n";	
	
	public static String getAnnotationFQN(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String pkg = store.getString(PreferenceConstants.ANNOTATIONPACKAGE);
		String clazz = store.getString(PreferenceConstants.ANNOTATIONCLASS);	
		return pkg+"."+clazz;
	}
	
}
