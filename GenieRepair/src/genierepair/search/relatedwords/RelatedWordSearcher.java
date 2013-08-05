package genierepair.search.relatedwords;

import genierepair.Activator;
import genierepair.preferences.PreferenceConstants;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jface.preference.IPreferenceStore;

import tmp.FelipeDebug;


public class RelatedWordSearcher {

	/**get synonyms for the given @param word*/
	public List<Term> getSynonyms(String word)  {
		RelatedSearchResult result1= getRelatedSearchResult(word);

		if (result1 == null || (result1.getVerbs() == null && result1.getNouns() == null))
			return new ArrayList<Term>();
		
		List<String> verbs = result1.getVerbs();
		List<String> codeRelated = result1.getCodeRelatedSyns();
		List<String> nouns = result1.getNouns();
		List<String> adj = result1.getAdjectives();
		List<Term> synonyms = new ArrayList<Term>();

		for(RelatedSearchResult rsr : relatedFromCamelCase(word)){
			verbs.addAll(rsr.getVerbs());
			codeRelated.addAll(rsr.getCodeRelatedSyns());
			nouns.addAll(rsr.getNouns());
			adj.addAll(rsr.getAdjectives());
		}
		
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


	/**get related words using camel case*/
	private Collection<RelatedSearchResult> relatedFromCamelCase(String word1) {
		SplitCamelCaseIdentifier split = new SplitCamelCaseIdentifier(word1);
		Collection<RelatedSearchResult> ret = new HashSet<RelatedSearchResult>();
		Collection<String> coll = split.split();
		for(String str : coll){
			RelatedSearchResult res = getRelatedSearchResult(str);
			if(res!=null){
				ret.add(res);
			}
		}
		return ret;
	}

	/**get a related search result to the given @param word*/
	private RelatedSearchResult getRelatedSearchResult(String word){
		FelipeDebug.debug("[RelatedWordSearcher]: getting related words for "+word);

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String url = store.getString(PreferenceConstants.RELATED_WORD_SERVER)+"/related-words-service/GetRelated?word=";

		InputStream serverAnswer;
		JAXBContext context;
		Unmarshaller marshaller;

		try {
			serverAnswer = new URL(url+word).openStream();
			context = JAXBContext.newInstance(RelatedSearchResult.class);
			marshaller = context.createUnmarshaller();
			return (RelatedSearchResult) marshaller.unmarshal(serverAnswer);
		} catch (MalformedURLException e) {
			FelipeDebug.errDebug(e);
		} catch (IOException e) {
			FelipeDebug.errDebug(e);
		} catch (JAXBException e) {
			FelipeDebug.errDebug(e);
		}
		return null;
	}
	


	/**
	 * Sliced Document sliced from sourcerer, query: camel case split
	 */
	class SplitCamelCaseIdentifier {

		/* fields */
		private String ident;

		/* constructors */
		public SplitCamelCaseIdentifier(String ident) {
			this.ident = ident;
		}

		/* methods */
		public Collection<String> split() {
			String s = ident;
			Set<String> result = new HashSet<String>();

			while (s.length() > 0) {
				StringBuffer buf = new StringBuffer();

				char first = s.charAt(0);
				buf.append(first);
				int i = 1;

				if (s.length() > 1) {
					boolean camelWord;
					if (Character.isLowerCase(first)) {
						camelWord = true;
					} else {
						char next = s.charAt(i++);
						buf.append(next);
						camelWord = Character.isLowerCase(next);
					}

					while (i < s.length()) {
						char c = s.charAt(i);
						if (Character.isUpperCase(c)) {
							if (camelWord)
								break;
						} else if (!camelWord) {
							break;
						}
						buf.append(c);
						++i;
					}

					if (!camelWord && i < s.length()) {
						buf.deleteCharAt(buf.length() - 1);
						--i;
					}
				}

				result.add(buf.toString().toLowerCase(Locale.US));
				s = s.substring(i);
			}

			return result;
		}
	}

}
