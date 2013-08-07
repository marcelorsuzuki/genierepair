package genierepair.testing;

import edu.uci.ics.sourcerer.services.search.adapter.SingleResult;
import genierepair.search.solr.SolrQueryCreator;
import genierepair.search.solr.SourcererSolrQuery;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;


import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.junit.model.ITestCaseElement;

import genierepair.pools.SolrResultPool;

import tmp.FelipeDebug;
import tmp.MySQLQuery;
import tmp.MySingleResult;

public class FailCase {

	private IJavaProject project;
	private IPackageFragment srcPackage;
	public static ClassVisitor cvisitor=null;
	private ITestCaseElement failTestCase;
	private MyMethodInterface mi;



	public FailCase(IJavaProject project, String className,String methodName, ITestCaseElement testCase) throws Exception{
		//this.methodsToLook = new HashMap<MyMethodInterface,SolrQueryCreator>();
		this.project = project;
		this.failTestCase=testCase;
		srcPackage=null;
		IPackageFragment[] packages = JavaCore.create(project.getProject()).getPackageFragments();
		//get package name
		String packageName = "";
		if(className.contains(".")){
			packageName=className.substring(0,className.lastIndexOf("."));
			className = className.substring(className.lastIndexOf(".")+1);
		}
		//create AST for package
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE &&
					mypackage.getElementName().equals(packageName)) {
				srcPackage=mypackage;
				createAST(srcPackage);
			}
		}
		//now that we can handle each class, try to find the one named className
		TypeDeclaration lookingtype =null;
		lookingtype = cvisitor.getClasses().get(className);
		MethodDeclaration lookingMethod=null;
		if(lookingtype==null){
			FelipeDebug.errDebug("Could not locate class "+className);
			throw new Exception("MethodInterface already in SolrResultPool");
		}
		//now try to find out the fail test
		for(MethodDeclaration md : lookingtype.getMethods()){
			if(md.getName().getIdentifier().equals(methodName)){
				lookingMethod=md;
				break;
			}
		}
		if(lookingMethod==null){
			FelipeDebug.errDebug("Could not find method "+methodName);
			return;
		}

		//get statements stack, create MethodInterface and show it...
		Deque<ASTNode> stack =this.getStack(lookingMethod); 
		MyMethodInterface mi = getMethodInterfaceOnAssert(stack,0);
		FelipeDebug.debug(getClass(),"found method: "+mi);
		if(SolrResultPool.getContents(mi)!=null){//this method interface is already in the pool
			return;
		}
		//create solr query, make the query, store the results and show it
		SolrQueryCreator sqc = new SolrQueryCreator(mi,true);
		String solrQuery = sqc.getSolrQuery();
		mi.setSolrQuery(solrQuery);
		SourcererSolrQuery ssq = new SourcererSolrQuery(solrQuery);
		List<SingleResult> results = ssq.makeQuery();
		//TODO REMOVE
		//update its ID...
		List<MySingleResult> updatedResults = new ArrayList<MySingleResult>();
		for(SingleResult sr : results){
			long neweid = MySQLQuery.query(MySQLQuery.fixSolr(sr.getFqn(), sr.getParams()));
			MySingleResult msr = new MySingleResult(sr,neweid);
			updatedResults.add(msr);
			//FelipeDebug.debug("\t"+msr);
		}
		//add it to SolrPool
		SolrResultPool.add(mi,updatedResults);
		this.mi=mi;
		//add parents info
		IType parent = project.findType(mi.getParentsName());
		mi.setParent(parent);

	}

	/**@param lookingMethod the method that failed in tests*/
	private Deque<ASTNode> getStack(MethodDeclaration lookingMethod) {
		Deque<ASTNode> stack = new ArrayDeque<ASTNode>();
		Iterator<?> it = lookingMethod.getBody().statements().iterator();
		while(it.hasNext()){		//there are two kinds till now...ExpStmnt and VarDecl
			Object o = it.next();
			if(o instanceof ExpressionStatement){
				stack.push(((ExpressionStatement)o).getExpression());//in case of a ascendent search for the variable
			}else if(o instanceof VariableDeclarationStatement){
				stack.push(((VariableDeclarationStatement)o));
				
				FelipeDebug.errDebug( 
						"Sorry this tool doesnt support this sentence yet:\n"+o);
				/*
				VariableDeclarationStatement var = (VariableDeclarationStatement)o;
				Iterator<VariableDeclarationFragment> varFragments = var.fragments().iterator();
				while(varFragments.hasNext()){
					VariableDeclarationFragment eachfrag = varFragments.next();
					if(eachfrag.getInitializer()!=null){
						SimpleName sn = eachfrag.getName();
						Expression init = eachfrag.getInitializer();
						if(init!=null){
							Assignment s = AST.newAST(AST.JLS4).newAssignment();
							s.setLeftHandSide(sn);
							s.setRightHandSide(init);
							FelipeDebug.debug("adding assignment: "+s);
							stack.push(s);
						} else {
							stack.push(sn);
						}
					}
				}
				 */
			} else {
				JOptionPane.showMessageDialog(null, "I was not expecting that: "+o);
				JOptionPane.showMessageDialog(null, o.getClass());
			}
		}
		return stack;
	}

	/**@param stack the instructions stack
	 * @param assertNumber indicates how many assert instructions it should skip*/
	private MyMethodInterface getMethodInterfaceOnAssert(Deque<ASTNode> stack, int assertNumber) throws InterfaceCreationException{
		while(!stack.isEmpty() && assertNumber!=-1){
			ASTNode ex = stack.pop();
			if(ex instanceof MethodInvocation){
				MethodInvocation mi = (MethodInvocation) ex;
				if(mi.getName().getIdentifier().equals("assertEquals") && assertNumber--==0){
					List<?> args = mi.arguments();
					if(args.size()>=2){
						Object secondParam = args.get(1);
						Object firstParam = args.get(0);
						//looking for return type
						ITypeBinding returnType = null;
						if(firstParam instanceof SimpleName){
							returnType = ((SimpleName)firstParam).resolveTypeBinding();
						} else if(firstParam instanceof ClassInstanceCreation){
							returnType = ((ClassInstanceCreation)firstParam).resolveTypeBinding();
						}

						//looking for method and class name
						//take a look at
						//http://help.eclipse.org/galileo/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/ClassInstanceCreation.html
						if(secondParam instanceof ClassInstanceCreation){//constructor
							ClassInstanceCreation cc = (ClassInstanceCreation) secondParam;
							returnType = cc.resolveTypeBinding();
							MyMethodInterface intrfce = getMethodinterface(cc,returnType);
							if(intrfce==null){
								throw new InterfaceCreationException("Could not create MethodInterface from ClassInstanceCreation");
							}
							return intrfce;
						} else if (secondParam instanceof SimpleName){ //variable
							SimpleName sn = (SimpleName) secondParam;
							MyMethodInterface methInter = methodInterfaceFromStack(stack,sn,returnType);
							if(methInter==null) {
								throw new InterfaceCreationException("Could not find previous assignment of "+sn);
							}
							return methInter;
						} else if (secondParam instanceof MethodInvocation){ //method invocation
							MethodInvocation mi2 = (MethodInvocation) secondParam;
							//work on it...
							MyMethodInterface intrfce = getMethodinterface(mi2,returnType);
							if(intrfce==null){
								throw new InterfaceCreationException("Could not find assignment on method stack");
							}
							return intrfce;
						} else {//anything else
							JOptionPane.showMessageDialog(null, "None: "+secondParam);
						}
					}
				}
			}
		}
		return null;
	}

	private MyMethodInterface methodInterfaceFromStack(Deque<ASTNode> stack,SimpleName sn,ITypeBinding rturn) throws InterfaceCreationException {
		while(!stack.isEmpty()){					//start popping the stack in order to find the assignment
			ASTNode previousExp = stack.pop(); 	//previous statement
			if(previousExp instanceof Assignment){					
				Assignment assign = (Assignment)previousExp;
				if(assign.getLeftHandSide() instanceof SimpleName){	//in the left hand it has to be a variable
					SimpleName var = (SimpleName)assign.getLeftHandSide();
					if(var.getIdentifier().equals(sn.getIdentifier())){	//same variable
						Expression rHand = assign.getRightHandSide();	
						if(rHand instanceof MethodInvocation){			//direct method invocation
							MyMethodInterface intrfce = getMethodinterface((MethodInvocation) rHand,rturn);
							return intrfce;
						} else if(rHand instanceof ClassInstanceCreation){
							MyMethodInterface intrfce = getMethodinterface((ClassInstanceCreation) rHand,rturn);
							return intrfce;
						} else{
							//work on it or throw an exception
							JOptionPane.showMessageDialog(null, "This tool doesnt support "+rHand+" yet.");
							throw new InterfaceCreationException("Right hand on assignment is too complicated...");
						}//end rHand instanceof MethodInvocation
					} //end same identifier
				}//end simplename
			} //end assignment
			/*
			else if(previousExp instanceof VariableDeclarationStatement){
				VariableDeclarationStatement varDecl = (VariableDeclarationStatement) previousExp;
				Iterator<VariableDeclarationFragment> varFragments = varDecl.fragments().iterator();
				while(varFragments.hasNext()){
					VariableDeclarationFragment eachfrag = varFragments.next();
					if(eachfrag.getInitializer()!=null){
						SimpleName simpleName = eachfrag.getName();
						Expression init = eachfrag.getInitializer();
						if(init!=null && false){
							//MyMethodInterface intrfce = getMethodinterface(init,rturn);
							//return intrfce;
						} 
					}
				}
			}
			*/
		}//end while
		return null;
	}

	

	private MyMethodInterface getMethodinterface(MethodInvocation mi2,ITypeBinding rturn) throws InterfaceCreationException {
		return new MyMethodInterface(mi2.resolveMethodBinding(),rturn);
	}

	private MyMethodInterface getMethodinterface(ClassInstanceCreation cc, ITypeBinding rturn) throws InterfaceCreationException {
		return new MyMethodInterface(cc.resolveConstructorBinding(),rturn);
	}

	private void createAST(IPackageFragment mypackage) throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			// Now create the AST for the CompilationUnits
			CompilationUnit parse = parse(unit);
			if(cvisitor==null){
				cvisitor = new ClassVisitor();
			}
			parse.accept(cvisitor);
		}
	}

	public IJavaProject getProject(){return project;}
	public IPackageFragment getSourceFolder(){return srcPackage;}
	public ITestCaseElement getFailTestCase(){return this.failTestCase;}

	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}

	public MyMethodInterface getMethodInterface() {
		return mi;
	}
}