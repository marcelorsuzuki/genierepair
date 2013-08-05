package genierepair.search.solr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import edu.uci.ics.sourcerer.services.search.adapter.SearchAdapter;
import edu.uci.ics.sourcerer.services.search.adapter.SearchResult;
import edu.uci.ics.sourcerer.services.search.adapter.SingleResult;
import genierepair.Activator;
import genierepair.preferences.PreferenceConstants;

public class SourcererSolrQuery {
	
	private String query;
	private String server;

	public SourcererSolrQuery(String query){
		this.query=query;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		this.server = store.getString(PreferenceConstants.SOLR_SERVER);
	}
	
	public SourcererSolrQuery(String query, String server){
		this.query=query;
		this.server = server;
	}
	
	public  List<SingleResult> makeQuery(){
		SearchAdapter s = SearchAdapter.create(server);
	    SearchResult srcResult = s.search(query);
	    int numFound = srcResult.getNumFound();
	    if(numFound>0){
	    	return srcResult.getResults(0, numFound);
	    }
	    return new ArrayList<SingleResult>();
	}

}
