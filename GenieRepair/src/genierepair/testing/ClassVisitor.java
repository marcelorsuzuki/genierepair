package genierepair.testing;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import tmp.FelipeDebug;

public class ClassVisitor extends ASTVisitor {
	private Map<String,TypeDeclaration> classes=new HashMap<String,TypeDeclaration>();

	@Override
	public boolean visit(TypeDeclaration node) {
		FelipeDebug.debug("adding: "+node.getName().getFullyQualifiedName());
		classes.put(node.getName().getFullyQualifiedName(), node);
		return super.visit(node);
	}

	public Map<String,TypeDeclaration> getClasses() {
		return classes;
	}

}
