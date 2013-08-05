package genierepair.testing;

import tmp.FelipeDebug;

public class InterfaceCreationException extends Exception {
	/**
	 * auto generated serial version
	 */
	private static final long serialVersionUID = -6678820201450013249L;
	private String cause = "Unknown problem.";
	
	public InterfaceCreationException(String cause){
		this.cause=cause;
	}
	
	public void printStacktrace(){
		FelipeDebug.errDebug(cause);
		super.printStackTrace();
	}
	
}
