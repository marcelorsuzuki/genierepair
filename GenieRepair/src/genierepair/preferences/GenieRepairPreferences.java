package genierepair.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import genierepair.Activator;


public class GenieRepairPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public GenieRepairPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Genie Repair preference page");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		//add fields
		//file server
		StringFieldEditor fileServer = 
				new StringFieldEditor(PreferenceConstants.FILE_SERVER,
						"File Server",getFieldEditorParent());
		//related words
		StringFieldEditor relatedWordServer = 
				new StringFieldEditor(PreferenceConstants.RELATED_WORD_SERVER,
						"Related Words Server",getFieldEditorParent());
		//slice server
		StringFieldEditor sliceServer = 
				new StringFieldEditor(PreferenceConstants.SLICE_SERVER,
						"Slice Server",getFieldEditorParent());
		//solr server
		StringFieldEditor solrServer = 
				new StringFieldEditor(PreferenceConstants.SOLR_SERVER,
						"Solr server",getFieldEditorParent());
		//annotation package name
		StringFieldEditor annPack = 
				new StringFieldEditor(PreferenceConstants.ANNOTATIONPACKAGE,
						"Annotation package",getFieldEditorParent());
		//annotation package name
		StringFieldEditor annClass = 
				new StringFieldEditor(PreferenceConstants.ANNOTATIONCLASS,
						"Annotation Class",getFieldEditorParent());
		//listen to junit?
		BooleanFieldEditor listenJUnit = 
				new BooleanFieldEditor(PreferenceConstants.LISTEN_JUNIT,
						"Listen to JUnit",getFieldEditorParent());
		addField(fileServer);
		addField(relatedWordServer);
		addField(sliceServer);
		addField(solrServer);
		addField(annPack);
		addField(annClass);
		addField(listenJUnit);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}