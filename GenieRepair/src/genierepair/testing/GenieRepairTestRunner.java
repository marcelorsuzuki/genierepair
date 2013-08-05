package genierepair.testing;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.viewers.StructuredSelection;


public class GenieRepairTestRunner {

	private IJavaProject javap;
	private String failClassName;

	public GenieRepairTestRunner(FailCase fc) {
		this.javap = fc.getProject();
		this.failClassName = fc.getFailTestCase().getTestClassName();
	}
	
	public void runTest() throws JavaModelException{
		 IType test = javap.findType(failClassName);
		 IJavaElement jtest = test.getPrimaryElement();
		 StructuredSelection iss = new StructuredSelection(jtest);
		 JUnitLaunchShortcut js = new JUnitLaunchShortcut();
		 js.launch(iss, "run");
	}
	
}
