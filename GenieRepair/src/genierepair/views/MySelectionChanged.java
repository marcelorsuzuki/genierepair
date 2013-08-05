package genierepair.views;

import genierepair.testing.JUnitListener;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;

import tmp.FelipeDebug;

public class MySelectionChanged implements ISelectionChangedListener {

	private TreeViewer viewer;

	public MySelectionChanged(TreeViewer viewer) {
		this.viewer=viewer;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent arg0) {
		if(JUnitListener.hasUpdates()){
			while(!JUnitListener.isReady()){
				FelipeDebug.debug("Sleeping for 300 ms");
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			JUnitListener.setHasUpdates(false);
			viewer.refresh();
		}
	}

}
