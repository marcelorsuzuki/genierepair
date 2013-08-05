package genierepair.handlers;

import genierepair.search.slicer.SliceFile;
import javax.swing.JOptionPane;
import org.eclipse.jdt.core.IJavaProject;
import tmp.FelipeDebug;


public class SliceRemover{

	long id;
	IJavaProject javap;


	public SliceRemover(long id) {
		this.id=id;
	}

	public boolean remove(){
		if(confirm()){
			FelipeDebug.debug("Removing slice "+id);
			return SliceFile.removeSlice(id);
		}
		return false;
	}

	static boolean confirm(){
		return JOptionPane.YES_OPTION == JOptionPane
				.showConfirmDialog(null, 
						"Are you sure that you want to remove"
								+" the sliced code and restore the"
								+" your previous code?",null, 
								JOptionPane.YES_NO_OPTION);
	}	

}
