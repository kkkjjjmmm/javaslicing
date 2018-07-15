package kkkjjjmmm.slicer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class OBSVisitor extends ModifierVisitor<List<ExpressionStmt>> {

	
	@Override
	public Node visit(final ExpressionStmt n, final List<ExpressionStmt> arg) {
		List<ExpressionStmt> additionalStatements = new ArrayList<>();
		super.visit(n,additionalStatements);
		if (!additionalStatements.isEmpty()) {
			BlockStmt nestedBlock = new BlockStmt();
			nestedBlock.addStatement(n);
			for (ExpressionStmt stmt : additionalStatements) {
				nestedBlock.addStatement(stmt);
			}
			return nestedBlock;
		}
		return n;
	}
	
	
	public Node visit(final MethodCallExpr n, final List<ExpressionStmt> arg){
		String name = n.getName().asString();
		if (!name.equals("Observe")) {
			return n;
		}
		NodeList<Expression> args = n.getArguments();
		if (args.size() != 1) {
			System.out.println("wrong number of arguments!");
			return n;
		}
		Expression obsArg = args.get(0);
		
		if (obsArg instanceof NameExpr && arg != null) {
			NameExpr variableInsideObs = (NameExpr) obsArg;
			arg.add(new ExpressionStmt(
					new AssignExpr(variableInsideObs, new BooleanLiteralExpr(true), AssignExpr.Operator.ASSIGN)));
		}else if(obsArg instanceof BinaryExpr && arg != null){
			BinaryExpr v = (BinaryExpr) obsArg;
			if(v.getOperator().equals(BinaryExpr.Operator.EQUALS)) {
				arg.add(new ExpressionStmt(
						new AssignExpr(v.getLeft(), v.getRight(), AssignExpr.Operator.ASSIGN)));
			}
		}
		return n;
	}
	
//	public static void main(String[] args) throws FileNotFoundException {
//		FileInputStream in = new FileInputStream("/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/test/Example.java");
//		CompilationUnit cu = JavaParser.parse(in);
//		cu.accept(new OBSVisitor(), null);
//		cu.accept(new SVFVisitor(), null);
//		System.out.println(cu);
//	}
	
	// observe(f == false)
	// observe(f == 1)
	// observe(f == g)
	// observe(f.equals(g))
	// observe(f == false && g == false)
	// observe(f == 1 && g == 1)
	// observe(f == g && f == h)
	// ...

	
}
