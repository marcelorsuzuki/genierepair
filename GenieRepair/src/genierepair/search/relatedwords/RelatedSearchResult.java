package genierepair.search.relatedwords;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="relatedSearch")//each annotation represents a field in the xml file...so this is the root field
public class RelatedSearchResult {

	private List<String> verbs = new ArrayList<String>();
	private List<String> nouns = new ArrayList<String>();
	private List<String> adjectives = new ArrayList<String>();
	private List<String> nounAntonyms = new ArrayList<String>();
	private List<String> verbAntonyms = new ArrayList<String>();
	private List<String> adjectiveAntonyms = new ArrayList<String>();
	private List<String> codeRelatedSyns = new ArrayList<String>();
	private List<String> codeRelatedAntons = new ArrayList<String>();

	@XmlElement(name="verbAntonym")//verbAntonym field
	public List<String> getVerbAntonyms() {
		return verbAntonyms;
	}

	public void setVerbAntonyms(List<String> verbAntonyms) {
		this.verbAntonyms = verbAntonyms;
	}

	@XmlElement(name="nounAntonym")
	public List<String> getNounAntonyms() {
		return nounAntonyms;
	}

	public void setNounAntonyms(List<String> nounAntonyms) {
		this.nounAntonyms = nounAntonyms;
	}

	@XmlElement(name="codeAntonym")
	public List<String> getCodeRelatedAntons() {
		return codeRelatedAntons;
	}

	public void setCodeRelatedAntons(List<String> codeRelatedAntons) {
		this.codeRelatedAntons = codeRelatedAntons;
	}

	@XmlElement(name="codeSynonym")
	public List<String> getCodeRelatedSyns() {
		return codeRelatedSyns;
	}

	public void setCodeRelatedSyns(List<String> codeRelated) {
		this.codeRelatedSyns = codeRelated;
	}

	@XmlElement(name="nounSynonym")
	public List<String> getNouns() {
		return nouns;
	}

	public void setNouns(List<String> nounSynonyms) {
		this.nouns = nounSynonyms;
	}

	@XmlElement(name="verbSynonym")
	public List<String> getVerbs() {
		return verbs;
	}

	public void setVerbs(List<String> verbSynonyms) {
		this.verbs = verbSynonyms;
	}

	@XmlElement(name="adjSynonym")
	public List<String> getAdjectives() {
		return adjectives;
	}

	public void setAdjectives(List<String> adjSynonyms) {
		this.adjectives = adjSynonyms;
	}

	@XmlElement(name="adjAntonyms")
	public List<String> getAdjectiveAntonyms() {
		return adjectiveAntonyms;
	}

	public void setAdjectiveAntonyms(List<String> adjAnt) {
		this.adjectiveAntonyms = adjAnt;
	}
}