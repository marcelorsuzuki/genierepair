package genierepair.handlers;

import genierepair.Activator;

import javax.swing.JOptionPane;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class ToggleJUnitListening extends AbstractHandler implements IObjectActionDelegate {
	public static ISelection currentSelection;

	/**
	 * The constructor.
	 */
	public ToggleJUnitListening() {
	}

	/**
	 * the command has been executed, so extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator.setListenningToJUnit(!Activator.isListenningToJUnit());
		int ans = JOptionPane.showConfirmDialog(null, "Will "
				+(Activator.isListenningToJUnit()?"":"not ")+
				"listen to JUnit.");
		if(ans==JOptionPane.OK_OPTION){
			return null;
		} else {
			return execute(event);
		}
	}

	@Override
	public void run(IAction arg0) {
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		currentSelection = arg1;
	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
		
	}
}
