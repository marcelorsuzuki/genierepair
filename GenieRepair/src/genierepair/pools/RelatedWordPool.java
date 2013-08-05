package genierepair.pools;

import genierepair.search.relatedwords.RelatedWordSearcher;
import genierepair.search.relatedwords.Term;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import tmp.FelipeDebug;

/**This class contains all the pairs (String,Set<Term>) where the string is the key word and the set is a set of synonyms*/
public class RelatedWordPool {

	/**the pool that stores the key and the synonyms*/
	private static Map<String,Set<Term>> pool= new HashMap<String,Set<Term>>();


	/**retuns a set with related terms to the given @param word*/
	public static Set<Term> getTerms(String word){
		if(!pool.containsKey(word)){
			FelipeDebug.debug("[RelatedWordPool]: adding "+word);
			newPool(word);
		}
		return pool.get(word);
	}

	/**retuns an Array with related terms to the given @param word*/
	public Term[] toArray(String word){
		Set<Term> set = getTerms(word);
		Term[] array = new Term[set.size()];
		Iterator<Term> it = set.iterator();
		for(int i=0;it.hasNext();++i){
			array[i]=it.next();
		}
		return  array;
	}

	private static void newPool(String newWord) {
		RelatedWordSearcher rws = new RelatedWordSearcher();
		HashSet<Term> hash = new HashSet<Term>(rws.getSynonyms(newWord));
		pool.put(newWord, hash);
	}





}
