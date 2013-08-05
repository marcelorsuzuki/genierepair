package genierepair.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.*;
import org.eclipse.swt.SWT;



public class CodeView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "genierepair.views.CodeView";

	private Text text;


	/**
	 * The constructor.
	 */
	public CodeView() {
		super();
	}

	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.V_SCROLL|SWT.MULTI|SWT.WRAP);
		text.setEditable(false);
		text.setText("No remote source-code to show.");
		text.setBackground(
				Display.getDefault().getSystemColor(SWT.COLOR_WHITE)
				);
	}

	public void setFocus() {
		text.setFocus();
	}

	public void setSourceCode(String str) {
		text.setText(str);
	}


}