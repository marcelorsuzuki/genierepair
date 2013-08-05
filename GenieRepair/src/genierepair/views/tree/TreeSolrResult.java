package genierepair.views.tree;

import genierepair.util.EntitiesUtil;
import tmp.MySingleResult;

public class TreeSolrResult extends TreeParent {

	MySingleResult singleResult;
	String code;
	
	public TreeSolrResult(MySingleResult sr) {
		super(sr.toString());
		singleResult = sr;
	}
	
	public String getCode(){
		if(code==null || code.equals("")){
			code  = EntitiesUtil.getEntitySourceCode(singleResult.getEntityID());
		}
		return code;
	}
	
	public MySingleResult getResult(){
		return singleResult;
	}
	

	
}
