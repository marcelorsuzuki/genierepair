package genierepair.search.slicer;

import javax.swing.JOptionPane;

import genierepair.testing.MyMethodInterface;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import tmp.FelipeDebug;

public class Unweaver {
	
	
	
	IJavaProject javap;
	Long sliceID;
	IPackageFragmentRoot source;
	MyMethodInterface mi;

	public Unweaver(IJavaProject javap, Long sliceID){
		this.javap = javap;
		this.sliceID =sliceID;
		this.resolveSourceFolder();
	}
	
	public void setMethodInterface(MyMethodInterface mi){
		this.mi=mi;
	}
	
	private void resolveSourceFolder(){
		try {
			for(IPackageFragmentRoot r :javap.getAllPackageFragmentRoots()){//for each package
				if(r.getKind()==IPackageFragmentRoot.K_SOURCE){//verify if it is a source package
					this.source = r;
					break;
				}
			}
		} catch (JavaModelException e) {
			FelipeDebug.errDebug(e.getLocalizedMessage());
		}
	}
	
	public boolean unweave(MyMethodInterface mi) throws CoreException{
		this.setMethodInterface(mi);
		IJavaElement[] srcElements = source.getChildren();
		for(int i=0;i<srcElements.length;++i){
			String parts[] = srcElements[i].getResource().getFullPath().toPortableString().replace(IPath.SEPARATOR, ':').split(":");
			int idx = parts[0].trim().equalsIgnoreCase("")?3:2;
			//parts[0] is unknown
			//parts[1] is project name
			//parts[2] is src folder
			for(int j=idx;j<parts.length;++j){
				//TODO
			}
		}
		
		
		
		return true;
	}
	
}
