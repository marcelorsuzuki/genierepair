package genierepair.search.solr;

import genierepair.search.relatedwords.Term;
import genierepair.testing.MyMethodInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tmp.FelipeDebug;

import genierepair.pools.RelatedWordPool;
import genierepair.preferences.constants.Solr;

public class SolrQueryCreator {
	private Set<String> returnType;//the return type, for example "String" or "void" or "MyClass1"
	private Set<String> fqnContentsClass;
	private Set<String> fqnContentsMethod;
	private Map<String,Set<String>> fqnParamsContents;

	private static final boolean relatedReturnType = true;
	private static final boolean relatedParams = true;
	private static final boolean relatedClasses = true;
	private static final boolean relatedMethods = true;


	/**Constructs a Solr Query based on @param mi*/
	public SolrQueryCreator(MyMethodInterface mi){
		//get key words...return type, clazz name, method name and params types
		String ret = mi.getReturnType().getName();
		String clazz = mi.getParentsName();
		if(clazz.contains(".")){
			int idx = clazz.lastIndexOf(".");
			clazz=clazz.substring(idx+1);
		}
		String methodName = mi.getID();
		List<String> params = mi.getParamsClasses();

		//put them into the list
		returnType = new HashSet<String>();
		returnType.add(ret);
		fqnContentsClass = new HashSet<String>();
		fqnContentsClass.add(clazz);
		fqnContentsMethod = new HashSet<String>();
		fqnContentsMethod.add(methodName);
		fqnParamsContents = new HashMap<String,Set<String>>();
		for(String s : params){
			fqnParamsContents.put(s, new HashSet<String>());
			fqnParamsContents.get(s).add(s);
		}

	}

	/**Constructs a Solr Query based on @param mi, and it's related words if @param relatedWords is true*/
	public SolrQueryCreator(MyMethodInterface mi, boolean relatedWords) {
		this(mi);
		if(relatedWords){
			putRelatedWords();
		}
	}


	private void putRelatedWords() {
		//all the sets in this class have one item.
		String retType = returnType.iterator().next();
		String claz = fqnContentsClass.iterator().next();
		String method = fqnContentsMethod.iterator().next();
		Set<String> parms = fqnParamsContents.keySet();
		//put all related words in its proper set
		//returnType related Words
		Set<Term> term = null;
		if(SolrQueryCreator.relatedReturnType){
			term = RelatedWordPool.getTerms(retType);
			for(Term t : term){
				returnType.add(t.getTerm());
			}
		}
		//class related words
		if(SolrQueryCreator.relatedClasses){
			term = RelatedWordPool.getTerms(claz);
			for(Term t : term){
				fqnContentsClass.add(t.getTerm());
			}
		}
		//method related words
		if(SolrQueryCreator.relatedMethods){
			term = RelatedWordPool.getTerms(method);
			for(Term t : term){
				fqnContentsMethod.add(t.getTerm());
			}
		}
		//params related Words
		if(SolrQueryCreator.relatedParams){
			for(String par:parms){//for each param
				term = RelatedWordPool.getTerms(par);//get its related words
				for(Term t : term){
					fqnParamsContents.get(par).add(t.getTerm());
				}
			}
		}
		
	}

	public void setFqnContentsMethod(Set<String> fqnContentsMethod) {
		this.fqnContentsMethod = fqnContentsMethod;
	}

	public String getReturnType(){
		StringBuilder ret = new StringBuilder("(");
		for (Iterator<String> it = returnType.iterator();it.hasNext();){
			String retType = it.next();
			ret.append(retType);
			if(it.hasNext()){
				ret.append(Solr.OR);
			} else {
				ret.append(") ");
			}
		}
		String contents = ret.toString();
		String retret = "("+Solr.returnContents+":"+contents+" "+Solr.OR+" "+Solr.returnFqn+":"+contents+")";
		FelipeDebug.debug("[SolrQueryCreator]: return condition is:\n\t"+retret);
		return retret;

	}

	public void setReturnType(Set<String> rt){
		this.returnType=rt;
	}

	public void setFqnClass(Set<String> fqn){
		fqnContentsClass = fqn;
	}

	public String getFqn(){
		StringBuilder ret = new StringBuilder("((");
		//put all the classes names
		for (Iterator<String> it = fqnContentsClass.iterator();it.hasNext();){
			String retType = it.next();
			ret.append(retType);
			if(it.hasNext()){
				ret.append(Solr.OR);
			} else {
				ret.append(") AND (");
			}
		}
		//put all the methods names
		for (Iterator<String> it = fqnContentsMethod.iterator();it.hasNext();){
			String retType = it.next();
			ret.append(retType);
			if(it.hasNext()){
				ret.append(Solr.OR);
			} else {
				ret.append(")) ");
			}
		}
		String contents = ret.toString();
		String retret ="("+Solr.fqnContents     +": "+contents+Solr.OR
						+" "+Solr.fqnFrags      +": "+contents+" "+Solr.OR
						+" "+Solr.snameContents +": "+contents+" "+Solr.OR
						+" "+Solr.sname         +": "+contents+" )"; 
		FelipeDebug.debug("[SolrQueryCreator]: fqn condition is:\n\t"+retret);
		return retret;
	
	}

	public String getParams(){
		if(fqnParamsContents!=null && fqnParamsContents.size()>0){
			StringBuilder ret = new StringBuilder("(");
			for(Iterator<Set<String>> outSet = fqnParamsContents.values().iterator();outSet.hasNext(); ){
				Set<String> set = outSet.next();
				ret.append(" ( ");
				for (Iterator<String> it = set.iterator();it.hasNext();){
					String retType = it.next();
					ret.append(retType);
					if(it.hasNext()){
						ret.append(Solr.OR);
					} 
				}
				ret.append(" ) ");
				if(outSet.hasNext()){
					ret.append(Solr.AND);
				}
			}
			ret.append(")");
			String contents = ret.toString();
			String retret  = "("+Solr.paramsContents+":"+contents+" "+Solr.OR+" "+Solr.paramFrags+":"+contents+")";
			FelipeDebug.debug("[SolrQueryCreator]: params condition is:\n\t"+retret);
			return retret;
		}
		return "";
	}

	public String getSolrQuery(){
		String params = this.getParams();
		if(params!=null){
			params=Solr.AND+params;
		} else {
			params="";	
		}
		String s =  this.getReturnType()+Solr.AND+this.getFqn()+params;
		if(s.contains("[")) s = s.replace("[", "\\[");
		if(s.contains("]")) s = s.replace("]", "\\]");
		return s;
	}




}
