package genierepair;


import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class GRImages {
	
	public static final String REFRESH = "refresh.png";
	public static final String REMOVE = "remove.png";
	public static final String ADD_N_TEST = "insert.png";
	public static final String GENIEREPAIR = "tool.png";
	public static final String METHOD = "fail.png";
	public static final String RESULT = "result.png";
	public static final String VIEW_CODE = "viewcode.png";
	public static final String SOLR_QUERY = "remotequery.png";
	
	public static ImageDescriptor getImageDescriptor(String key){
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fullPathString = BundleUtility.find(bundle, "icons"
				+File.separator+"16x16"
				+File.separator+key);
		return ImageDescriptor.createFromURL(fullPathString);
	}
	
}
