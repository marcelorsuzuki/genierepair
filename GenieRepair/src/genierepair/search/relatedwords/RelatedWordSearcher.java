package genierepair.search.relatedwords;

import genierepair.Activator;
import genierepair.preferences.PreferenceConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jface.preference.IPreferenceStore;

import tmp.FelipeDebug;


public class RelatedWordSearcher {


	public List<Term> getSynonyms(String word)  {
		FelipeDebug.debug("[RelatedWordSearcher]: getting related words for "+word);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String url = store.getString(PreferenceConstants.RELATED_WORD_SERVER)+"/related-words-service/GetRelated?word=";
		
		InputStream serverAnswer;
		JAXBContext context;
		Unmarshaller marshaller;
		RelatedSearchResult result=null;
		try {
			serverAnswer = new URL(url+word).openStream();
			context = JAXBContext.newInstance(RelatedSearchResult.class);
			marshaller = context.createUnmarshaller();
			result = (RelatedSearchResult) marshaller.unmarshal(serverAnswer);
		} catch (MalformedURLException e) {
			FelipeDebug.errDebug(e);
		} catch (IOException e) {
			FelipeDebug.errDebug(e);
		} catch (JAXBException e) {
			FelipeDebug.errDebug(e);
		}
		 
		 
		
		if (result == null || (result.getVerbs() == null && result.getNouns() == null))
			return new ArrayList<Term>();
		List<String> verbs = result.getVerbs();
		List<String> codeRelated = result.getCodeRelatedSyns();
		List<String> nouns = result.getNouns();
		List<String> adj = result.getAdjectives();
		List<Term> synonyms = new ArrayList<Term>();


		for(String s : verbs)
			synonyms.add(new Term(s, 0d, 0, false));

		for(String s : codeRelated)
			synonyms.add(new Term(s, 0d, 0, false));

		for(String s : nouns)
			synonyms.add(new Term(s, 0d, 0, false));

		for(String s : adj)
			synonyms.add(new Term(s, 0d, 0, false));

		return synonyms;
	}

}
