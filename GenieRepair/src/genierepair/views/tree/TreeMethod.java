package genierepair.views.tree;

import tmp.MySingleResult;
import genierepair.pools.SolrResultPool;
import genierepair.testing.MyMethodInterface;

public class TreeMethod extends TreeParent {

	private MyMethodInterface mInterface;
	public static final int TYPE = 1;
	
	public TreeMethod(MyMethodInterface mi) {
		super(mi.toString());
		this.mInterface=mi;
		for(MySingleResult sr : SolrResultPool.getContents(mInterface)){
			TreeSolrResult tsr = new TreeSolrResult(sr);
			tsr.setParent(this);
			addChild(tsr);
		}
	}
	
	public MyMethodInterface getMethodInterface(){return mInterface;}
	public String getQuery(){
		return mInterface.getSolrQuery();
	}
}
