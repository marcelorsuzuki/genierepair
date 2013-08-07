package tmp;

import edu.uci.ics.sourcerer.services.search.adapter.SingleResult;

public class MySingleResult{

	private SingleResult sr;
	private long newID;

	public MySingleResult(SingleResult sr, long newID){
		this.sr=sr;
		this.newID=newID;
	}

	public int getRank() {
		return sr.getRank();
	}

	public float getScore() {
		return sr.getScore();
	}

	public Long getEntityID() {
		return newID;
	}

	public String getFqn() {
		return sr.getFqn();
	}

	public int hashCode() {
		return sr.hashCode();
	}

	public int getParamCount() {
		return sr.getParamCount();
	}

	public String getParams() {
		return sr.getParams();
	}

	public String getReturnFqn() {
		return sr.getReturnFqn();
	}

	public boolean equals(Object obj) {
		return (obj instanceof MySingleResult) && newID==((MySingleResult)obj).getEntityID();
	}

	public String toString() {
		String fqn = sr.getFqn();
		String ret = sr.getReturnFqn();
		String params = sr.getParams();
		long eid = newID;
		
		return ret+" "+fqn+params+ "["+eid+"]";
	}

	public SingleResult getSingleResult() {
		return sr;
	}



}
