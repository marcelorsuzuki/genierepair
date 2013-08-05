package genierepair.testing;

import genierepair.pools.FailCasePool;

import java.util.LinkedList;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.junit.model.ITestCaseElement;

import tmp.FelipeDebug;

public class FailThread extends Thread{

	private LinkedList<ITestCaseElement> toWork;
	private boolean endForAWhile;
	private int timeToSleep = 300;

	public FailThread(){
		toWork= new LinkedList<ITestCaseElement>();
		endForAWhile = true;
	}

	public synchronized void addWork(ITestCaseElement work){
		toWork.addLast(work);
		if(getEnd()){
			setEnd(false);
		}
	}

	private synchronized void setEnd(boolean b) {
		endForAWhile = b;
	}
	private synchronized boolean getEnd() {
		return endForAWhile;
	}
	public synchronized ITestCaseElement getWork(){
		if(!toWork.isEmpty()){
			return toWork.removeFirst();
		}
		return null;
	}

	public void run(){
		while(!toWork.isEmpty()){
			FelipeDebug.debug("Starting work...");
			ITestCaseElement testCase = getWork();
			FelipeDebug.debug((testCase==null?"no job, waiting":"starting..."));
			if(testCase==null){
				try {
					Thread.sleep(timeToSleep);
				} catch (InterruptedException e) {}
				continue;
			}
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
		}
	}

	public synchronized void endAdding(){
		endForAWhile = true;
		start();
	}

	public synchronized boolean isDone(){
		return toWork.isEmpty() && endForAWhile; 
	}

}
