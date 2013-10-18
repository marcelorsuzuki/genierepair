package genierepair.testing;

import java.util.ArrayList;
import java.util.List;

import genierepair.Activator;

import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.jdt.junit.model.ITestRunSession;

import genierepair.pools.FailCasePool;
import genierepair.pools.SolrResultPool;

public class JUnitListener extends TestRunListener {

	public static List<String> classes = new ArrayList<String>();
	private static boolean hasUpdates=false;
	private static FailThread ft = new FailThread(); 
	/*
	public FailCase getTestCaseSuite(ITestCaseElement testCase) throws JavaModelException{
		IJavaProject project = testCase.getTestRunSession().getLaunchedProject();
		//resolve class, method and package names
		String tcClass = testCase.getTestClassName();
		String tcMethod = testCase.getTestMethodName();
		FelipeDebug.debug("looking for: "+tcClass+"."+"tcMethod");
		FailCase fail = null;
		try {
			
			fail = new FailCase(project,tcClass,tcMethod,testCase);
			FailCasePool.add(fail.getMethodInterface(), fail);
		} catch (Exception e) {
			FelipeDebug.errDebug(e.getMessage());
		}
		if(fail==null){
			FelipeDebug.debug("Could not create FailCase object");
		}
		return fail;
		
    }
	*/
	@Override
	public void sessionFinished(ITestRunSession session) {
		if(Activator.isListenningToJUnit()){
			super.sessionFinished(session);
			ft.endAdding();
		}
	}

	@Override
	public void sessionLaunched(ITestRunSession session) {
		if(Activator.isListenningToJUnit()){
			super.sessionLaunched(session);
		}
	}

	@Override
	public void sessionStarted(ITestRunSession session) {
		if(Activator.isListenningToJUnit()){
			super.sessionStarted(session);
			classes.clear();
			SolrResultPool.clear();
			FailCasePool.clear();
			ft = new FailThread();
		}
	}

	@Override
	public void testCaseFinished(ITestCaseElement testCaseElement) {
		if(Activator.isListenningToJUnit()){
			super.testCaseFinished(testCaseElement);
			if(!classes.contains(testCaseElement.getTestClassName())){
				classes.add(testCaseElement.getTestClassName());
			}
			Result result = testCaseElement.getTestResult(true);
			if (result == Result.FAILURE){
				ft.addWork(testCaseElement);
				hasUpdates = true;
			}
		}			
	}

	@Override
	public void testCaseStarted(ITestCaseElement testCaseElement) {
		if(Activator.isListenningToJUnit()){
			super.testCaseStarted(testCaseElement);
			
		}
	}


	public static boolean hasUpdates() {
		return hasUpdates;
	}
	public static void setHasUpdates(boolean b){
		hasUpdates=b;
	}
	
	public static boolean isReady(){
		return true;
	}
	
	

}
