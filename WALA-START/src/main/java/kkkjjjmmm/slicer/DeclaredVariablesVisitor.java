package kkkjjjmmm.slicer;

import java.util.Iterator;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import com.google.common.collect.Lists;

public class DeclaredVariablesVisitor extends GenericListVisitorAdapter<Node, Void>{
	
	public List<Node> visit(VariableDeclarationExpr n, Void arg){
		List<Node> declaredVars = Lists.newArrayList();
		Iterator<VariableDeclarator> allVariables = n.getVariables().iterator();
		while(allVariables.hasNext()) {
			NameExpr name = new NameExpr(allVariables.next().getName());
			//System.out.println(name);
			declaredVars.add(name);
		}
		return declaredVars;
	}

}
