package kkkjjjmmm.slicer;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class MethodVisitor extends ModifierVisitor<List<ExpressionStmt>> {
	
	public Node visit(final ClassOrInterfaceDeclaration n, final List<ExpressionStmt> arg){	
		Iterator<BodyDeclaration<?>> it = n.getMembers().iterator();
		while(it.hasNext()) {
			Node node = it.next();
			if(node instanceof MethodDeclaration) {
				String methodName = ((MethodDeclaration)node).getNameAsString();
				if(methodName.equals("write")) {
					int parameterNum = ((MethodDeclaration)node).getParameters().size();
					if(parameterNum == 1) {
						Parameter p = ((MethodDeclaration)node).getParameters().iterator().next();
						if(p.getType() instanceof PrimitiveType) {
							n.remove(node);
						}
					}
				}
			}
		}
		EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);
		addNewMethod(n, Primitive.BOOLEAN, modifiers);
		addNewMethod(n, Primitive.INT, modifiers);
		addNewMethod(n, Primitive.CHAR, modifiers);
		addNewMethod(n, Primitive.DOUBLE, modifiers);
		addNewMethod(n, Primitive.FLOAT, modifiers);
		addNewMethod(n, Primitive.SHORT, modifiers);
		addNewMethod(n, Primitive.BYTE, modifiers);
		addNewMethod(n, Primitive.LONG, modifiers);
		return n;
	} 
	
	public static void addNewMethod(ClassOrInterfaceDeclaration n, Primitive type, EnumSet<Modifier> modifiers) {		
		 MethodDeclaration method = new MethodDeclaration(modifiers, new PrimitiveType(type), "write");
	     modifiers.add(Modifier.STATIC);
	     method.setModifiers(modifiers);
	     n.addMember(method);
	     Parameter param = new Parameter(new PrimitiveType(type), "b");
	     method.addParameter(param);
	     method.setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr("b"))));
	}
}
