package genierepair.search.solr;

import genierepair.preferences.constants.Solr;

public class SolrUtil {
	public static String lineSeparation(String solrQuery){
		int ret = solrQuery.indexOf(Solr.returnContents);
		int name = solrQuery.indexOf(Solr.fqnContents,Solr.returnContents.length());
		int param = solrQuery.indexOf(Solr.paramsContents);
		if(param>0){
			solrQuery=solrQuery.substring(ret,name)+"\n\n"
					+solrQuery.substring(name,param)+"\n\n"
					+solrQuery.substring(param);
		} else {
			solrQuery=solrQuery.substring(ret,name)+"\n\n"
					+solrQuery.substring(name);
		}
		return solrQuery;
	}

}
