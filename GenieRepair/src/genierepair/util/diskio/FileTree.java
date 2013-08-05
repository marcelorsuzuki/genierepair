package genierepair.util.diskio;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tmp.FelipeDebug;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class FileTree{
	
	private File file;
	private Map<String, FileTree> children;
	
	
	//constructs 
	public FileTree(String path){
		this(new File(path));
	}

	public FileTree(File file) {
		FelipeDebug.debug("Creating tree for file: "+file);
		this.file=file;
		children = new HashMap<String, FileTree>();
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				children.put(f.getName(),new FileTree(f));
			}
		}
	}
	
	public boolean contains(String name){
		return children.containsKey(name);
	}
	
	public boolean changeContents(File f){
		try {
			
			Files.copy(f.toPath(), file.toPath(),StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			FelipeDebug.debug(e.getMessage());
			return false;
		}
		return true;
	}
	
	public FileTree get(String name){
		return children.get(name);
	}
	
	public FileTree get(File name){
		return get(name.getName());
	}
	
	
	
	//auto generated delegate methods...

	/**
	 * @return
	 * @see java.io.File#getName()
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * @return
	 * @see java.io.File#getParentFile()
	 */
	public File getParentFile() {
		return file.getParentFile();
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.File#getCanonicalFile()
	 */
	public File getCanonicalFile() throws IOException {
		return file.getCanonicalFile();
	}

	/**
	 * @return
	 * @see java.io.File#exists()
	 */
	public boolean exists() {
		return file.exists();
	}

	/**
	 * @return
	 * @see java.io.File#isDirectory()
	 */
	public boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * @return
	 * @see java.io.File#isFile()
	 */
	public boolean isFile() {
		return file.isFile();
	}

	/**
	 * @return
	 * @see java.io.File#delete()
	 */
	public boolean delete() {
		return file.delete();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.io.File#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return file.equals(obj);
	}

	public static void startRemoving(FileTree from, FileTree contents, FileTree bkp) throws IOException {
		if(bkp==null) 
			throw new IOException("There is no backup files");
		FelipeDebug.debug("Backup folder is: "+bkp.file.getCanonicalPath());
		for(FileTree ft : bkp.children.values()){
			FelipeDebug.debug("\t"+ft.file.getAbsolutePath());
		}
		for(FileTree t : contents.children.values()){//remove sliced files and restore originals
			FileTree src = from.get(t.file);
			FileTree backup = bkp.get(t.file);
			removeCoincident(src,t);
			if(backup == null){
				FelipeDebug.debug("backup is null for "+t.file);
			} else {
				restore(src,backup);
			}
		}
		//replace original files
		for(FileTree t : bkp.children.values()){
			FileTree src = from.get(t.file);
			restore(src,t);
		}
		
	}

	private static void removeCoincident(FileTree src, FileTree slicedsrc) throws IOException {
		File fsrc = src.file;
		File fslcsrc = slicedsrc.file;
		FelipeDebug.debug("Removing coincident files in:");
		FelipeDebug.debug("\tsrc="+fsrc.getCanonicalPath());
		FelipeDebug.debug("\tslc="+fslcsrc.getCanonicalPath());
		boolean directory = fsrc.isDirectory() && fslcsrc.isDirectory();
		boolean file = fsrc.isFile() && fslcsrc.isFile();
		if(directory){
			//call recursion
			for(FileTree ft : slicedsrc.children.values()){
				removeCoincident(src.get(ft.file),ft);
			}
			//if folder is empty, delete folder
			if(fsrc.listFiles().length==0){
				fsrc.delete();
			}
		} else if(file){
			fsrc.delete();
		} else {
			//throw IOException
			throw new IOException("Inconsistent File System");
		}
	}

	private static void restore(FileTree src, FileTree backup) throws IOException {
		File fsrc = src.file;
		File fbkp = backup.file;
		FelipeDebug.debug("Restoring files:");
		FelipeDebug.debug("\tsrc="+fsrc.getCanonicalPath());
		FelipeDebug.debug("\tbkp="+fbkp.getCanonicalPath());
		boolean directory = fbkp.isDirectory();
		boolean file = fbkp.isFile();
		if(directory){
			fsrc.mkdirs();
			for(FileTree ft : backup.children.values()){
				restore(src.get(ft.file),ft);
			}
		} else if(file){
			Files.copy(fbkp.toPath(),fsrc.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} else {
			throw new IOException("Corrupt FIle System");
		}
	}
	



}
