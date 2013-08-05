package genierepair.views;

import genierepair.GRImages;
import genierepair.handlers.SliceRemover;
import genierepair.pools.AddedSlices;
import genierepair.pools.SolrResultPool;
import genierepair.search.slicer.SliceFile;
import genierepair.search.slicer.SlicerConnector;
import genierepair.search.solr.SolrUtil;
import genierepair.testing.MyMethodInterface;
import genierepair.views.tree.TreeMethod;
import genierepair.views.tree.TreeObject;
import genierepair.views.tree.TreeParent;
import genierepair.views.tree.TreeSolrQuery;
import genierepair.views.tree.TreeSolrResult;

import java.io.IOException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import tmp.FelipeDebug;
import tmp.MySingleResult;

public class GenieRepairView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "genierepair.views.GenieRepairView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action addSlice;
	private Action removeSlice;
	private Action refresh;
	private Action viewCode;
	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {}
		public void dispose() {}
		
		public Object[] getElements(Object parent) {
			updateTree();
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		
		private void updateTree() {
			if(invisibleRoot==null)
				return;
			for(TreeObject  child: invisibleRoot.getChildren()){
				invisibleRoot.removeChild(child);
			}
			createMINodes(invisibleRoot);
		}
		
		private void createMINodes(TreeParent parent) {
			for(MyMethodInterface mi : SolrResultPool.getMethodInterfaces()){
				TreeParent child = new TreeMethod(mi);
				parent.addChild(child);
			}
		}
		
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
		
/*
 * We will set up a dummy model to initialize tree heararchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		private void initialize() {
			TreeParent root = new TreeParent("No failing method found");

			invisibleRoot = new TreeParent("Refresh");
			invisibleRoot.addChild(root);
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			if(obj instanceof TreeMethod){
				return GRImages.getImageDescriptor(GRImages.METHOD).createImage();
			} else if(obj instanceof TreeSolrResult){
				return GRImages.getImageDescriptor(GRImages.RESULT).createImage();
			} else if(obj instanceof TreeSolrQuery){
				return GRImages.getImageDescriptor(GRImages.SOLR_QUERY).createImage();
			}
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}


	/**
	 * The constructor.
	 */
	public GenieRepairView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput(getViewSite());
		viewer.addSelectionChangedListener(new MySelectionChanged(viewer));
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "GenieRepair.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.removeAll();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				GenieRepairView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.removeAll();
		manager.add(addSlice);
		manager.add(new Separator());
		manager.add(removeSlice);
		manager.add(new Separator());
		manager.add(refresh);
		manager.add(new Separator());
		manager.add(viewCode);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.removeAll();
		manager.add(addSlice);
		manager.add(removeSlice);
		manager.add(refresh);
		manager.add(viewCode);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		//manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.removeAll();
		manager.add(addSlice);
		manager.add(removeSlice);
		manager.add(refresh);
		manager.add(viewCode);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		addSlice = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				//just if the selected object is a SolrResult
				if(obj instanceof TreeSolrResult){
					//get entity id to merge
					TreeSolrResult leaf = (TreeSolrResult) obj;
					MySingleResult sr = leaf.getResult();
					Long eid = sr.getEntityID();
					//get files to merge
					SlicerConnector scon = new SlicerConnector(eid);
					SliceFile slicerF = scon.getSlice();
					MyMethodInterface mi = ((TreeMethod)leaf.getParent()).getMethodInterface();
					slicerF.setMethodInterface(mi);
					//register
					AddedSlices.put(sr, mi);
					//merge
					try {
						FelipeDebug.debug("[GenieRepairView]: Unzipping "+eid);
						slicerF.unzip();
						FelipeDebug.debug("[GenieRepairView]: Unzipped");
						FelipeDebug.debug("[GenieRepairView]: Merging "+eid);
						slicerF.merge();
						FelipeDebug.debug("[GenieRepairView]: Merged");
						FelipeDebug.debug("[GenieRepairView]: Saving project");
						slicerF.saveAndRebuild();
						FelipeDebug.debug("[GenieRepairView]: Saved");
						FelipeDebug.debug("[GenieRepairView]: Changing method contents for "+mi);
						slicerF.changeMethodContents(eid);
						FelipeDebug.debug("[GenieRepairView]: Changed");
						FelipeDebug.debug("[GenieRepairView]: Saving project");
						slicerF.saveAndRebuild();
						FelipeDebug.debug("[GenieRepairView]: Saved");
						FelipeDebug.debug("[GenieRepairView]: Running tests");
						slicerF.runTests();
						FelipeDebug.debug("[GenieRepairView]: Done!");
					} catch (IOException e1) {
						FelipeDebug.errDebug("[add slice button in view (IO)]"+e1.getMessage());
					} catch (Exception e1) {
						FelipeDebug.errDebug("[add slice button in view (General)]"+e1.getMessage());
					}
				} else {
					showErrorMessage("The selected item is not a valid entity");
				}
			}
		};
		addSlice.setText("Add and test slice");
		addSlice.setToolTipText("Add and test slice");
		addSlice.setImageDescriptor(GRImages.getImageDescriptor(GRImages.ADD_N_TEST));
		//***************************************************************************
		removeSlice = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				//just if the selected object is a SolrResult
				Long id=-1L;
				MySingleResult msr = null;
				if(obj instanceof TreeSolrResult){
					//get entity id to merge
					TreeSolrResult leaf = (TreeSolrResult) obj;
					msr = leaf.getResult();
					id = msr.getEntityID();
				}
				id = SliceRemoveDialog.showDialog(id);
				if(id==-1L){
					return;
				}
				FelipeDebug.debug("[GenieRepairView]: Removing slice "+id);
				SliceRemover sr = new SliceRemover(id);
				AddedSlices.remove(id);
				if(sr.remove()){
					showSuccessMessage("Slice "+id+" removed");
				} else {
					showErrorMessage("Could not remove slice "+id);
				}
				
			}
		};
		removeSlice.setText("Remove slice");
		removeSlice.setToolTipText("Remove slice from project");
		removeSlice.setImageDescriptor(GRImages.getImageDescriptor(GRImages.REMOVE));
		//***************************************************************************
		refresh = new Action(){
			public void run() {
				GenieRepairView.this.viewer.refresh();
			}
		};
		refresh.setText("Refresh list");
		refresh.setToolTipText("Refresh this list");
		refresh.setImageDescriptor(GRImages.getImageDescriptor(GRImages.REFRESH));
		//***************************************************************************
		viewCode = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				//just if the selected object is a SolrResult
				if(obj instanceof TreeSolrResult){
					//set code view contents to this source-code
					TreeSolrResult leaf = (TreeSolrResult) obj;
					FelipeDebug.debug("[GenieRepairView]: Getting source code...");
					String contents=leaf.getCode();
					setCodeViewContent(contents);
				} else if(obj instanceof TreeMethod){
					//set code view contents to this query
					TreeMethod tm = (TreeMethod) obj;
					String contents = tm.getMethodInterface().getSolrQuery();
					setCodeViewContent(SolrUtil.lineSeparation(contents));
				} else {
					showErrorMessage("The selected item is not a valid entity");
				}
			}
		};
		viewCode.setText("View");
		viewCode.setToolTipText("View remote query or source-code");
		viewCode.setImageDescriptor(GRImages.getImageDescriptor(GRImages.VIEW_CODE));
		//***************************************************************************
		doubleClickAction = new Action() {
			public void run() {
				viewCode.run();
			}
		};
	}
	
	private void setCodeViewContent(String contents){
		IWorkbench work = PlatformUI.getWorkbench();
		//bring view to the front
		try {
			work.getActiveWorkbenchWindow()
			.getActivePage()
			.showView(CodeView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		//get code view
		IViewPart codeView = work.getActiveWorkbenchWindow()
				.getActivePage().findView(CodeView.ID);
		//set contents
		((CodeView) codeView).setSourceCode(contents);
	}

	void showErrorMessage(String msg) {
		MessageDialog.openError(getSite().getShell(), "Error!", msg);
	}
	
	void showSuccessMessage(String msg){
		MessageDialog.openInformation(getSite().getShell(), "Success!", msg);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}