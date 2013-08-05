package genierepair.util.diskio;

import genierepair.preferences.constants.Constants;
import genierepair.util.MapProcessor;
import genierepair.views.GRProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import javax.swing.JOptionPane;
import tmp.FelipeDebug;

import static java.io.File.separator;


public class Folders {

	/**the name of the subfolder that contains the src code*/
	public static final String SRC_NAME = new String("src");
	/**the name of the subfolder that contains the zip file downloaded from slice server*/
	public static final String ZIP_NAME = new String("zip");
	/**the name of the subfolder that contains the backpup of the original code*/
	public static final String BKP_NAME = new String("backup");
	/**the main folder of the slices*/
	public static final String SLICE = new String("slices");


	public static final int SRC_CODE = 1;
	public static final int ZIP_CODE = 2;
	public static final int BKP_CODE = 4;

	/**@param proj the project which it you get the folder location on disk
	 * @return the String containing the full path to the project*/
	public static String getProjectsFolder(IProject proj){
		IResource res = proj.getWorkspace().getRoot().findMember(separator+proj.getName());
		return res.getLocation().toFile().getAbsolutePath();

	}

	/**@param the IJavaProject which it will get the source folder
	 * @return the source folder of this java project*/
	public static File getSourceFolder(IJavaProject javap){
		try {
			for(IPackageFragmentRoot r :javap.getAllPackageFragmentRoots()){//for each package
				if(r.getKind()==IPackageFragmentRoot.K_SOURCE){//verify if it is a source package
					File projF = new File(getProjectsFolder(javap.getProject()));//create a projects folder
					String src = r.getElementName();//get package name
					return new File(projF,src);//create a new folder
				}
			}
		} catch (JavaModelException e) {
			FelipeDebug.errDebug(e.getLocalizedMessage());
		}
		return null;
	}


	/**creates the slice folder struct inside the given folder
	 * @param folder must be the project folder*/
	public static void createSliceFolders(File folder) {
		File slice = new File(folder.getAbsolutePath()+separator+SLICE);
		if(!slice.exists()) slice.mkdir();
		File zip = new File(slice.getAbsolutePath()+separator+ZIP_NAME);
		if(!zip.exists()) zip.mkdir();
	}

	/**clear the slice folder struct inside the given folder
	 * @param projFolder must be the project folder*/
	public static void clearSliceFolders(File projFolder,int folder,long id) {
		boolean srcf = (folder & SRC_CODE)>0;
		boolean bkpf = (folder & BKP_CODE)>0;
		boolean zipf = (folder & ZIP_CODE)>0;
		if(srcf){
			File src = new File(projFolder.getAbsolutePath()+separator+SLICE+separator+id+separator+SRC_NAME);
			if(src.exists())
				for(File f : src.listFiles()){
					delete_r(f);
				}
		}
		if(zipf){
			File zip = new File(projFolder.getAbsolutePath()+separator+SLICE+separator+ZIP_NAME);
			if(zip.exists())
				for(File f : zip.listFiles()){
					delete_r(f);
				}
		}
		if(bkpf){
			File bkp = new File(projFolder.getAbsolutePath()+separator+SLICE+separator+id+separator+BKP_NAME);
			if(bkp.exists())
				for(File f : bkp.listFiles()){
					delete_r(f);
				}
		}
	}

	/**delete files recursively
	 * @param f the File that will be deleted*/
	private static void delete_r(File f){
		if(f.isDirectory()){
			for(File f2 : f.listFiles()){
				delete_r(f2);
			}
		}
		f.delete();
	}

	/**copy the folder struct (sub folders) from the @param source to the @param target folder*/
	public static boolean copyFolder(File source, File target, IJavaProject javap,long entityID){
		if(source.isDirectory()){//check if the source file is a folder
			if(!target.exists()){//check it the target file exists
				target.mkdir();
			}
			for(File f : source.listFiles()){//for each folder in source 
				if(f.isDirectory()){
					copyFolder(f,new File(target,f.getName()),javap,entityID);//copy subfolder
				} else {
					File targetFile = new File(target,f.getName());
					if(!targetFile.exists()){		//just copy file
						try {
							copyFile(f,targetFile);
						} catch (IOException e) {
							FelipeDebug.errDebug(e.getLocalizedMessage());
						}
					} else {						//merge file
						try {
							mergeFile(f,targetFile,javap,entityID);
						} catch (IOException e) {
							FelipeDebug.errDebug(e.getLocalizedMessage());
						} catch (JavaModelException e){
							FelipeDebug.errDebug(e.getLocalizedMessage());
						}
					}
				}
			}
			return true;
		}
		return false;//return false in the case of the source file is not a folder
	}


	/**tries to merge two files (add contents of source into the target)
	 * @param source the source file
	 * @param target the target file
	 * @param javap the java project in which both of them are located
	 * @throws IOException in case of source or target file dont exist or can not be written or read
	 * @throws JavaModelException in case of some java problem occur during the process*/
	private static void mergeFile(File source, File target, IJavaProject javap, long entityID) throws IOException, JavaModelException {
		saveOriginal(target,javap,entityID);//save original file

		String relativePath = removeSourcePath(target.getCanonicalPath(),javap);//get target relative path
		if(relativePath.contains(separator))
			relativePath = relativePath.replaceAll(separator, ".");				//replace from "/" to "."
		int idx = relativePath.indexOf(".java");								//remove ".java"
		relativePath= relativePath.substring(1,idx);

		IType t = javap.findType(relativePath);									//get target Itype
		IType s = javap.findType(SRC_NAME+"."+relativePath);					//get source Itype
		FelipeDebug.debug("Merging files:");
		FelipeDebug.debug("\ttarget => "+t+"\n\t\tfrom: "+relativePath);
		FelipeDebug.debug("\tsrc    => "+s+"\n\t\tfrom: "+SRC_NAME+"."+relativePath);
		//import annotation
		t.getCompilationUnit().createImport(Constants.getAnnotationFQN(), null, null);

		//add Annotation
		String annotation = Constants.SLICEANNOTATION+entityID+")"+Constants.LINE;
		IInitializer lastInit = null;

		try{//get,process and add Initializers
			IInitializer[] sinit = s.getInitializers();
			//add new initializers
			for(IInitializer o : sinit){
				//cannot put annotation on initializers
				lastInit = t.createInitializer("//"+annotation+o.getSource(), null, null);

			}

		} catch (JavaModelException e){
			FelipeDebug.debug(e.getLocalizedMessage());
		}

		//get fields
		IField[] tfields = t.getFields();									
		IField[] sfields = s.getFields();
		//process fields
		MapProcessor<IField> mpf = new MapProcessor<IField>(sfields,tfields);
		//add fields
		Map<String, IField> toaddF = mpf.getToAdd();
		for(IField m: toaddF.values()){
			String msource = annotation+m.getSource();
			FelipeDebug.debug("Adding field:\n"+msource);
			t.createField(msource, lastInit, false, null);
		}
		//get methods
		IMethod[] tmethods = t.getMethods();
		IMethod[] smethods = s.getMethods();
		//process methods
		MapProcessor<IMethod> mpm = new MapProcessor<IMethod>(smethods,tmethods){
			public String getName(IMethod m){
				String ret=m.getElementName();
				try {
					ret+=" "+m.getSignature();
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				return ret;
			}
		};
		//add methods
		Map<String, IMethod> toaddM = mpm.getToAdd();
		for(IMethod m: toaddM.values()){
			//m.getCompilationUnit().
			String msource = annotation+m.getSource();
			int oidx = m.getSource().indexOf("{");
			String before = msource.substring(0, oidx);
			String after = msource.substring(oidx);
			if(before.contains("private"))
				before = before.replaceAll("private", "public");
			else if(before.contains("protected"))
				before = before.replaceAll("protected", "public");
			msource=before+after;
			FelipeDebug.debug("Adding method:\n"+msource);
			t.createMethod(msource, null, false, null);
		}

		//get types
		IType[] ttypes = t.getTypes();
		IType[] stypes = s.getTypes();
		//process types
		MapProcessor<IType> mpt = new MapProcessor<IType>(stypes,ttypes);
		//add types
		Map<String, IType> toaddT = mpt.getToAdd();
		for(IType m: toaddT.values()){
			String msource = annotation+m.getSource();
			FelipeDebug.debug("Adding type:\n"+msource);
			t.createMethod(msource, null, false, null);
		}





	}


	/**@return a relative path for @param fullpath inside the java project @param javap
	 * @throws IOException if the @param fullpath doesnt belong to this java project*/
	public static String removeSourcePath(String fullpath, IJavaProject javap) throws IOException{
		String sourceFolder = getSourceFolder(javap).getCanonicalPath();
		if(!fullpath.startsWith(sourceFolder)){
			throw new IOException("Given path doesnt seem to be in the same folder as the project\n"
					+"source path: "+fullpath
					+"\nproject: "+sourceFolder);
		}
		int idx = sourceFolder.length();
		String packageAndClass = fullpath.substring(idx);
		return packageAndClass;
	}


	/**save file into the .slices/backup/ folder
	 * @param source the source file to be saved
	 * @param javap the java project in which this source file belongs
	 * @throws IOException*/
	public static void saveOriginal(File source, IJavaProject javap, long eid) throws IOException {
		FelipeDebug.debug("Saving original file: "+source);
		String fullpath = source.getCanonicalPath();
		String sourceFolder = getSourceFolder(javap).getCanonicalPath();
		if(!fullpath.startsWith(sourceFolder)){
			throw new IOException("Source file doesnt seem to be in the same folder as the project\n"
					+"source path: "+fullpath
					+"\nproject: "+sourceFolder);
		}
		int idx = sourceFolder.length();
		String packageAndClass = fullpath.substring(idx);
		String bkpFolder = getProjectsFolder(javap.getProject())
				+separator+SLICE+separator+eid+separator+BKP_NAME+separator;
		File save = new File(bkpFolder+packageAndClass);
		FelipeDebug.debug("creating backup file: "+save);
		if(Files.exists(save.toPath())){
			FelipeDebug.debug("File already exists => "+save);
			int confirm = JOptionPane.showConfirmDialog(null, "File\n"+save+"\nalready exsists. Override file?");
			if(confirm == JOptionPane.OK_OPTION){
				save.delete();
			} else {
				throw new IOException("Backup file "+save+" already exists");
			}
		} else {
			FelipeDebug.debug("File does not exist => "+save);
			ensureExists(save.getParentFile());
		}
		copyFile(source,save);
	}

	private static void ensureExists(File file) {
		if(!file.exists()){
			file.mkdirs();
		}

	}

	/**copy file @param source to the file @param target*/
	private static void copyFile(File source, File target) throws IOException {
		FelipeDebug.debug("Copying from: "+source+" to: "+target);
		Files.copy(source.toPath(), target.toPath());
	}

	public static IMethod changeToPublic(IMethod m,IType t)throws JavaModelException{
		int flags=Flags.AccPublic;
		flags = m.getFlags();
		boolean isPublic = Flags.isPublic(flags);
		boolean isPrivate = Flags.isPrivate(flags);
		boolean isProtected = Flags.isProtected(flags);	
		if(!isPublic){
			String msource = m.getSource();
			GRProgressMonitor pm = new GRProgressMonitor();
			pm=null;
			String javadoc = m.getAttachedJavadoc(pm);
			if(javadoc==null)javadoc="";
			FelipeDebug.debug("waiting monitor1");

			//GRProgressMonitor.waitMonitor(pm);
			String before;
			String after;
			int idx = javadoc.length();
			idx = msource.indexOf("{", idx);
			FelipeDebug.debug("waiting monitor1.1");
			before = msource.substring(javadoc.length(),idx);
			FelipeDebug.debug("waiting monitor1.2");
			after = msource.substring(idx);
			FelipeDebug.debug("waiting monitor1.3");
			if(isPrivate){
				before = before.replace("private", "public");
			} else if(isProtected){
				before = before.replace("protected", "public");
			} else {
				before+="public ";
			}
			msource=javadoc+before+after;
			//pm = new GRProgressMonitor();
			FelipeDebug.debug("waiting monitor1.9");
			m.rename("private_"+m.getElementName(), true, pm);
			FelipeDebug.debug("waiting monitor2");
			//GRProgressMonitor.waitMonitor(pm);
			//pm = new GRProgressMonitor();
			m = t.createMethod(msource, null, true, pm);
			FelipeDebug.debug("waiting monitor3");
			//GRProgressMonitor.waitMonitor(pm);
		}
		return m;
	}

}
