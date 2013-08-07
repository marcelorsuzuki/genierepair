package genierepair.search.slicer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;

import genierepair.Activator;
import genierepair.pools.FailCasePool;
import genierepair.pools.SlicePool;
import genierepair.pools.SolrResultPool;
import genierepair.preferences.PreferenceConstants;
import genierepair.preferences.constants.Constants;
import genierepair.testing.FailCase;
import genierepair.testing.GenieRepairTestRunner;
import genierepair.testing.MyMethodInterface;
import genierepair.util.diskio.Folders;
import genierepair.util.diskio.Unzip;

import tmp.FelipeDebug;
import tmp.MySingleResult;

public class SliceFile extends AbstractProjectEditor{

	protected byte[] bytes;
	protected Long eid;
	protected MyMethodInterface mi;
	protected String zipFileName;
	protected String projectFolder;
	protected FailCase fc;
	protected String sliceSrcFolder;
	protected String sliceBkpFolder;
	protected Weaver weave;
	protected Unweaver unweave;


	/**Create a slice file ready to be saved, uncompressed, 
	 * Weave and removed if it is the case
	 * @param bytes the byte array returned by the server
	 * @param entityID the entity ID that these bytes represent*/
	public SliceFile(byte[] bytes, long entityID) {
		this.bytes=bytes;
		eid=entityID;
		SlicePool.add(eid, this);
	}


	public void setMethodInterface(MyMethodInterface mi){
		this.mi=mi;
		fc = FailCasePool.get(mi);
		javap = fc.getProject();
		weave = new Weaver(javap,eid);
		unweave = new Unweaver(javap,eid);
		unweave.setMethodInterface(mi);
	}

	/**unzip Slice into the projects /slices/src/ projectFolder
	 * @throws IOException if there is no zip file to unzip
	 * or if the projects projectFolder could not be reached
	 * */
	public void unzip() throws IOException{
		//get project projectFolder
		this.projectFolder = Folders.getProjectsFolder(javap.getProject());
		File f = new File(projectFolder);
		if(!f.exists())
			throw new IOException("[SliceFile]: Given path ("+projectFolder+") does not exist.");
		if(!f.isDirectory())
			throw new IOException("[SliceFile]: Given path ("+projectFolder+") is not a projectFolder.");

		//make folders
		Folders.createSliceFolders(f);
		//clear folders
		Folders.clearSliceFolders(new File(f.getAbsolutePath()),Folders.SRC_CODE,eid);
		//save zip
		try {
			saveZipFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//unzip
		this.sliceSrcFolder = projectFolder+File.separator+Folders.SLICE+File.separator+this.eid+File.separator+Folders.SRC_NAME;
		this.sliceBkpFolder = projectFolder+File.separator+Folders.SLICE+File.separator+this.eid+File.separator+Folders.BKP_NAME;
		Unzip uzip = new Unzip(zipFileName,sliceSrcFolder);
		uzip.unzip();
	}


	/**@throws Exception if there is no zip file to save*/
	private void saveZipFile() throws Exception{
		if(eid==null){
			throw new Exception("There is no zip file to save");
		}
		//give the zipFile a name
		zipFileName = projectFolder+File.separator+Folders.SLICE+File.separator+Folders.ZIP_NAME+File.separator+eid+".zip";
		//write on disk
		FileOutputStream fos;
		FelipeDebug.debug("[SliceFile]: Saving file \""+zipFileName+"\" with length "+bytes.length);
		try {
			fos = new FileOutputStream(new File(zipFileName));
			fos.write(bytes,0,bytes.length);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**merge this slice with the project
	 * @throws IOException 
	 * @throws CoreException */
	public void merge() throws IOException, CoreException {
		weave.weave();
		cleanFolder();
	}


	private void cleanFolder() {
		try {
			Folders.delete(new File(projectFolder+File.separator
					+Folders.SLICE+File.separator+eid));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	/**change failing method contents to call the new method
	 * @throws Exception*/
	public void changeMethodContents(long eid) throws Exception{
		String annotation = new SliceAddedAnn(eid).toString();
		//find mi in the java project
		IType t = javap.findType(mi.getParentsName());
		t.getCompilationUnit().createImport(Constants.getAnnotationFQN(), null, null);
		Set<IMethod> ms = new HashSet<IMethod>();
		for(IMethod m : t.getMethods()){
			if(m.getElementName().equals(mi.getMethodName())){
				ms.add(m);
			}
		}
		IMethod aux = null;
		IMethod method = null;
		for(Iterator<IMethod> it = ms.iterator();it.hasNext();){
			aux=it.next();
			if(mi.equalsParams(aux.getParameterTypes())){
				FelipeDebug.debug(getClass(),"Method found by comparing params...");
				method = aux;
				break;
			}
		}
		if(method==null){
			method = (IMethod) ms.toArray()[0];	
		}
		//mi found
		String originalSource = method.getSource();			//save original source
		//rename method and refresh project
		mi.setParent(t);
		method.rename("replaced"+method.getElementName(), false, null);	
		mi.setMethod(method);
		javap.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);

		//create new method with same signature...
		int openBraceidx = originalSource.indexOf('{');
		int closeBraceidx = originalSource.lastIndexOf('}');
		if(!(openBraceidx<closeBraceidx && openBraceidx>0)){
			throw new Exception("Could not change source code from method: "+mi.toString());
		}
		String sourceCode = originalSource.substring(0,openBraceidx+1)
				+Constants.LINE;//signature

		//get the woven method
		FelipeDebug.debug(getClass(),"looking for the entity result method...");
		List<MySingleResult> results = SolrResultPool.getContents(mi);
		MySingleResult r = null;
		for(MySingleResult tmp : results){
			if(tmp.getEntityID().equals(eid)){
				r=tmp;
				FelipeDebug.debug("Found!");
				break;
			}
		}
		//get its type in the project
		String fqn = r.getFqn();
		String methodName = fqn.substring(fqn.lastIndexOf(".")+1);
		fqn = fqn.substring(0,fqn.lastIndexOf("."));
		FelipeDebug.debug(getClass(),"Looking for type: "+fqn);
		IType target = javap.findType(fqn);
		
		
		FelipeDebug.debug(getClass(),"looking for the method to be called... "+target.getElementName()+"."+methodName);

		//find the method that will be replaced
		FelipeDebug.debug(getClass(),"trying to find the method that will be replaced...("+methodName+")");
		FelipeDebug.debug(getClass(),"target is: "+target);		
		IMethod[] allmethods = target.getMethods();
		Set<IMethod> sameNameMethods = new HashSet<IMethod>();
		for(IMethod m: allmethods){
			FelipeDebug.debug(getClass(),m.getElementName()+"=="+methodName);
			if(m.getElementName().equals(methodName)){
				sameNameMethods.add(m);
			}
		}
		IMethod wanted = (IMethod) sameNameMethods.toArray()[0];

		FelipeDebug.debug(getClass(),"Done!");
		int flags = wanted.getFlags();
		boolean isstatic = Flags.isStatic(flags);


		FelipeDebug.debug(getClass(),"is static?: "+isstatic);
		String toAdd = "";
		boolean isvoid = sourceCode.contains(" void ");
		if(!Flags.isPublic(wanted.getFlags())){
			wanted = Folders.changeToPublic(wanted, target);
		}
		if(isstatic){//just call it
			if(!isvoid){
				toAdd+="return ";
			}
			toAdd+=target.getFullyQualifiedName()+"."+wanted.getElementName()+"(";
		} else {	//create new instance and then call it
			toAdd+=target.getFullyQualifiedName()+" "+"slicedObject = new "
					+target.getFullyQualifiedName()+"();\n"+(isvoid?"":"return ")+"slicedObject."
					+wanted.getElementName()+"(";
		}

		//adjust params
		FelipeDebug.debug(getClass(),"adding params...");
		int op = sourceCode.lastIndexOf('(')+1;
		int cp = sourceCode.lastIndexOf(')');
		String param = "";
		if(op<cp){
			param=sourceCode.substring(op,cp);
		}
		StringTokenizer tok = new StringTokenizer(param,",");//separate at ','
		while(tok.hasMoreTokens()){
			String p = tok.nextToken();
			p=p.trim();										//remove spaces beetwen comas
			toAdd+=p.split(" ")[1];							//first is the type, and second the name
			if(tok.hasMoreElements()){
				toAdd+=", ";
			}
		}
		toAdd+=");\n}";
		sourceCode+=toAdd;
		sourceCode = annotation+sourceCode;
		FelipeDebug.debug(getClass(),"Done!\n"+sourceCode);
		t.createMethod(sourceCode, null, true, null);
		FelipeDebug.debug(getClass(),"Method Created!");
	}

	public void runTests() {
		FelipeDebug.debug(getClass(),"Running tests...");
		GenieRepairTestRunner testRunner = new GenieRepairTestRunner(fc);
		try {
			testRunner.runTest();
		} catch (JavaModelException e) {
			FelipeDebug.errDebug(e.getMessage());
		}
	}

	public static boolean removeSlice(long id){
		SliceFile slice = SlicePool.get(id);
		try {
			slice.remove();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean remove() throws IOException {
		try {
			FelipeDebug.debug(getClass(),"unweaving...");
			unweave.unweave(mi);
			saveAndRebuild();
			FelipeDebug.debug(getClass(),"restoring previous code...");
			mi.restore();
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		saveAndRebuild();
		runTests();
		return true;
	}
	
	protected void createAnnotation() throws IOException{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String pkgName = store.getString(PreferenceConstants.ANNOTATIONPACKAGE);
		String annName = store.getString(PreferenceConstants.ANNOTATIONCLASS);
		String sourceCode = "package "+pkgName+";\n";
		sourceCode+="public @interface "+annName+" {\n\t";
		sourceCode+="long entityID();\n}\n";
		File src = Folders.getSourceFolder(javap);
		File pack = null;
		if(pkgName.contains(".")){
			pack = src;
			StringTokenizer tok = new StringTokenizer(pkgName,".");
			while(tok.hasMoreTokens()){
				pack = new File(pack,tok.nextToken());
			}
		} else {
			pack = new File(src,pkgName);
		}

		if(!pack.exists()){
			pack.mkdir();
		}
		File javaFile = new File(pack,annName+".java");
		if(!javaFile.exists()){
			javaFile.createNewFile();
		}
		OpenOption options= StandardOpenOption.WRITE;
		Files.write(javaFile.toPath(), sourceCode.getBytes(), options);
	}
}
