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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
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
import genierepair.util.Weaver;
import genierepair.util.diskio.FileTree;
import genierepair.util.diskio.Folders;
import genierepair.util.diskio.Unzip;
import genierepair.views.GRProgressMonitor;

import tmp.FelipeDebug;
import tmp.MySingleResult;



public class SliceFile {

	protected byte[] bytes;
	protected Long eid;
	protected MyMethodInterface mi;
	protected String zipFileName;
	protected String folder;
	protected IJavaProject javap;
	protected FailCase fc;
	protected String sliceSrcFolder;
	protected String sliceBkpFolder;

	protected static SliceFile lastSliceFile=null;

	/**Create a slice file ready to be saved, uncompressed, 
	 * Weave and removed if it is the case
	 * @param bytes the byte array returned by the server
	 * @param entityID the entity ID that these bytes represent*/
	public SliceFile(byte[] bytes, long entityID) {
		this.bytes=bytes;
		eid=entityID;
		lastSliceFile = this;
		SlicePool.add(eid, this);
	}


	public void setMethodInterface(MyMethodInterface mi){
		this.mi=mi;
		javap = FailCasePool.get(mi).getProject();
		fc = FailCasePool.get(mi);
	}

	/**unzip Slice into the projects /slices/src/ folder
	 * @throws IOException if there is no zip file to unzip
	 * or if the projects folder could not be reached
	 * */
	public void unzip() throws IOException{
		//get project folder
		this.folder = Folders.getProjectsFolder(javap.getProject());
		File f = new File(folder);
		if(!f.exists())
			throw new IOException("[SliceFile]: Given path ("+folder+") does not exist.");
		if(!f.isDirectory())
			throw new IOException("[SliceFile]: Given path ("+folder+") is not a folder.");

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
		this.sliceSrcFolder = folder+File.separator+Folders.SLICE+File.separator+this.eid+File.separator+Folders.SRC_NAME;
		this.sliceBkpFolder = folder+File.separator+Folders.SLICE+File.separator+this.eid+File.separator+Folders.BKP_NAME;
		Unzip uzip = new Unzip(zipFileName,sliceSrcFolder);
		uzip.unzip();
	}


	/**@throws Exception if there is no zip file to save*/
	private void saveZipFile() throws Exception{
		if(eid==null){
			throw new Exception("There is no zip file to save");
		}
		//give the zipFile a name
		zipFileName = folder+File.separator+Folders.SLICE+File.separator+Folders.ZIP_NAME+File.separator+eid+".zip";
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
		Weaver w = new Weaver(javap,this.eid);
		createAnnotation();
		w.weave();
		int a = 1;
		int b = 2;
		if(a==b-a){
			//throw new RuntimeException("break point");
			return;
		}
		
		File target =  Folders.getSourceFolder(javap);
		File src = new File(this.sliceSrcFolder);
		FelipeDebug.debug("[SliceFile]: merging files:"+
				"\n[SliceFile]:\t "+src+
				"\n[SliceFile]:\t AND\n[SliceFile]: \t"
				+target);
		try {
			includeInBuilding(eid);
		} catch (CoreException e) {
			FelipeDebug.errDebug("[SliceFile]: Could include new src folder in building path...\n[SliceFile]: reason= "+e.getLocalizedMessage());
		}
		Folders.copyFolder(src, target,javap,eid);
		try {
			excludeFromBuilding(eid);
		} catch (CoreException e) {
			FelipeDebug.errDebug("[SliceFile]: Could exclude src folder from building path...\n[SliceFile]: reason= "+e.getLocalizedMessage());
		}
	}


	/**change failing method contents to call the new method
	 * @throws Exception*/
	public void changeMethodContents(long eid) throws Exception{
		FelipeDebug.debug("[SliceFile]: ");
		String annotation = new SliceAddedAnn(eid).toString();//Constants.SLICEANNOTATION+eid+")"+Constants.LINE;
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
				FelipeDebug.debug("Method found by comparing params...");
				method = aux;
				break;
			}
		}


		if(method==null){
			method = (IMethod) ms.toArray()[0];	
		}
		//mi found
		String originalSource = method.getSource();			//save original source
		File srcfolder = Folders.getSourceFolder(javap);	// getSource folder
		String path = mi.getParentsName();//+".java";		//append packages and class name
		path = path.replace(".", File.separator)+".java";	//append .java
		File clazzFile = new File(srcfolder,path);			//open file
		Folders.saveOriginal(clazzFile, javap,eid);				//save

		//rename method and refresh project
		method.rename("replaced"+method.getElementName(), false, null);	
		javap.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);

		//create new method with same signature...
		int openBraceidx = originalSource.indexOf('{');
		int closeBraceidx = originalSource.lastIndexOf('}');
		if(!(openBraceidx<closeBraceidx && openBraceidx>0)){
			throw new Exception("Could not change source code from method: "+mi.toString());
		}
		String sourceCode = originalSource.substring(0,openBraceidx+1)
				+Constants.LINE;//signature

		//get  the weaven method
		FelipeDebug.debug("looking for the entity result method...");
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
		String methodName = fqn.substring(fqn.lastIndexOf('.')+1);
		fqn = fqn.substring(0,fqn.lastIndexOf("."));
		FelipeDebug.debug("Looking for type: "+fqn);
		IType target = javap.findType(fqn);
		FelipeDebug.debug("looking for the method to be called... "+target.getElementName()+"."+methodName);

		//find the method that will be replaced
		FelipeDebug.debug(getClass(),"trying to find the method that will be replaced...("+methodName+")");
		FelipeDebug.debug(getClass(),"target is: "+target);		
		IMethod[] allmethods = target.getMethods();
		Set<IMethod> sameNameMethods = new HashSet<IMethod>();
		for(IMethod m: allmethods){
			FelipeDebug.debug(m.getElementName()+"=="+methodName);
			if(m.getElementName().equals(methodName)){
				sameNameMethods.add(m);
				//taking care of the params...
				
				FelipeDebug.debug("Looking params...1");
				String[] paramsv = m.getParameterTypes();
				FelipeDebug.debug("Looking params...2");
				String paramsa = r.getParams();
				FelipeDebug.debug("Looking params...3");
				paramsa = paramsa.substring(1,paramsa.length()-1);
				FelipeDebug.debug("Looking params...4");
				String[] params  = paramsa.split(",");
				FelipeDebug.debug("Looking params...5");
				if(params.length==paramsv.length){
					for(int i=0;i<paramsv.length;i++){
						//paramsv[i] = Signature.toString(paramsv[i]);
						//paramsv[i] = Signature.toString(paramsv[i]);
						
						FelipeDebug.debug("params: "+paramsv[i]+"=="+params[i]);
					}
				}
				
			}
		}
		//String params = r.getParams();
		IMethod wanted = (IMethod) sameNameMethods.toArray()[0];	//TODO if there are more then one method with same name

		FelipeDebug.debug("Done!");
		int flags = wanted.getFlags();
		boolean isstatic = Flags.isStatic(flags);


		FelipeDebug.debug("is static?: "+isstatic);
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
		FelipeDebug.debug("adding params...");
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
		FelipeDebug.debug("Done!\n"+sourceCode);
		t.createMethod(sourceCode, null, true, null);
		FelipeDebug.debug("Method Created!");
	}



	protected void excludeFromBuilding(long eid) throws CoreException {
		IProject myPrj = javap.getProject();
		IFolder mySlicedFolder = myPrj.getFolder(File.separator+Folders.SLICE+File.separator+eid);
		IClasspathEntry srcEntry = JavaCore.newSourceEntry(mySlicedFolder.getFullPath());
		IClasspathEntry[] cpe = new IClasspathEntry[javap.getRawClasspath().length-1];
		IClasspathEntry[] all = javap.getRawClasspath();
		int count = 0;
		for(int i=0;i<all.length;i++){
			if(!all[i].getPath().equals(srcEntry.getPath())){
				cpe[count]=all[i];
				count++;
			}
		}
		javap.setRawClasspath(cpe,null);
		mySlicedFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
		saveAndRebuild();
	}

	protected void includeInBuilding(long id) throws CoreException {
		IProject myPrj = javap.getProject();
		IFolder mySlicedFolder = myPrj.getFolder(File.separator+Folders.SLICE+File.separator+id);
		IClasspathEntry srcEntry = JavaCore.newSourceEntry(mySlicedFolder.getFullPath());
		IClasspathEntry[] cpe = new IClasspathEntry[javap.getRawClasspath().length+1];
		for(int i = 0; i < javap.getRawClasspath().length; i++)
			cpe[i] = javap.getRawClasspath()[i];
		cpe[javap.getRawClasspath().length] = srcEntry;
		javap.setRawClasspath(cpe, null);
		mySlicedFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
		saveAndRebuild();
	}

	public void saveAndRebuild() {
		try {
			GRProgressMonitor monitor = new GRProgressMonitor();
			javap.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
			javap.save(null, true);
			javap.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
			javap.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			javap.getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
			GRProgressMonitor.waitMonitor(monitor);
		} catch (Exception e) {
			FelipeDebug.debug(e.getLocalizedMessage());
		}

	}

	public void runTests() {
		FelipeDebug.debug("Running tests...");
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
		//get relative locations
		String projectSourceFolder = Folders.getSourceFolder(javap).getCanonicalPath();
		//build directory tree
		FileTree bkp = new FileTree(sliceBkpFolder);
		FileTree src = new FileTree(projectSourceFolder);
		FileTree slicesrc = new FileTree(sliceSrcFolder);
		//remove and restore data
		FileTree.startRemoving(src,slicesrc,bkp);
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
	


	public void cleanFolder() throws IOException {
		Folders.removeSourcePath(sliceSrcFolder, javap);
		Folders.removeSourcePath(sliceBkpFolder, javap);
		
	}

}
