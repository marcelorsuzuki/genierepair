package genierepair.views.tree;

import org.eclipse.core.runtime.IAdaptable;

public abstract class TreeObject implements IAdaptable {

	
	private String name;
	private TreeParent parent;
	
	public TreeObject(String name) {
		this.name = name;
	}
	

	
	public String getName() {
		return name;
	}
	
	
	public void setParent(TreeParent parent) {
		this.parent = parent;
	}
	public TreeParent getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}
	
	
	
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class key) {
		return null;
	}
}