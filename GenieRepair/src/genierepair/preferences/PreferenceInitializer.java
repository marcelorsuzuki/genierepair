package genierepair.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import genierepair.Activator;

import static genierepair.preferences.PreferenceConstants.*;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(FILE_SERVER, "http://127.0.0.1:8080");
		store.setDefault(LISTEN_JUNIT, true);
		store.setDefault(RELATED_WORD_SERVER, "http://snake.ics.uci.edu:8080");
		store.setDefault(SLICE_SERVER, "http://127.0.0.1:8080");
		store.setDefault(SOLR_SERVER, "http://127.0.0.1:8080");
		store.setDefault(ANNOTATIONPACKAGE, "sliceannotation");
		store.setDefault(ANNOTATIONCLASS, "FromSlice");
	}

}
