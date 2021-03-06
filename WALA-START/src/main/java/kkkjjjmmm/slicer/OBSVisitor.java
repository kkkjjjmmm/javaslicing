package kkkjjjmmm.slicer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class OBSVisitor extends ModifierVisitor<List<ExpressionStmt>> {
	
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
					new AssignExpr(variableInsideObs,new MethodCallExpr("write",new BooleanLiteralExpr(true)), 
					AssignExpr.Operator.ASSIGN)));
		}else if(obsArg instanceof UnaryExpr && arg != null) {
			UnaryExpr uexp = (UnaryExpr) obsArg;
			if(uexp.getOperator().equals(UnaryExpr.Operator.LOGICAL_COMPLEMENT)) {	
				arg.add(new ExpressionStmt(
						new AssignExpr(uexp.getExpression(), new MethodCallExpr("write",new BooleanLiteralExpr(false)), 
						AssignExpr.Operator.ASSIGN)));
			}
		}else if(obsArg instanceof BinaryExpr && arg != null){
			BinaryExpr v = (BinaryExpr) obsArg;
			if(v.getOperator().equals(BinaryExpr.Operator.EQUALS)) {
				if (v.getRight().isNameExpr()){
					arg.add(new ExpressionStmt(
							new AssignExpr(v.getLeft(), new MethodCallExpr("write",v.getRight()),
									       AssignExpr.Operator.ASSIGN)));
					arg.add(new ExpressionStmt(
							new AssignExpr(v.getRight(),new MethodCallExpr("write",v.getLeft()), 
								           AssignExpr.Operator.ASSIGN)));
				}else {
					arg.add(new ExpressionStmt(
							new AssignExpr(v.getLeft(), new MethodCallExpr("write",v.getRight()), 
									       AssignExpr.Operator.ASSIGN)));
				} 
				
			}else if(v.getOperator().equals(BinaryExpr.Operator.AND)) {
				Expression left = v.getLeft();
				Expression right = v.getRight();
				Set<Expression> expInV = new HashSet<Expression>();			
				while (left instanceof BinaryExpr && 
					((BinaryExpr) left).getOperator().equals(BinaryExpr.Operator.AND)) {
					expInV.add(((BinaryExpr) left).getRight());
					left = ((BinaryExpr) left).getLeft();
				}
				expInV.add(left);
				expInV.add(right);
				Iterator<Expression> it = expInV.iterator();
				while(it.hasNext()) {
					Expression exp = it.next();
					if(exp instanceof NameExpr) {
						NameExpr nameExp = (NameExpr) exp;
						arg.add(new ExpressionStmt(
								new AssignExpr(nameExp, new MethodCallExpr("write",new BooleanLiteralExpr(true)), 
								AssignExpr.Operator.ASSIGN)));
					}else if(exp instanceof UnaryExpr) {
						UnaryExpr uexp = (UnaryExpr) exp;
						if(uexp.getOperator().equals(UnaryExpr.Operator.LOGICAL_COMPLEMENT)) {	
							arg.add(new ExpressionStmt(
									new AssignExpr(uexp.getExpression(), new MethodCallExpr("write",new BooleanLiteralExpr(false)), 
									AssignExpr.Operator.ASSIGN)));
						}
					}else if(exp instanceof BinaryExpr) {
						BinaryExpr binaryExp = (BinaryExpr) exp;
						if(binaryExp.getOperator().equals(BinaryExpr.Operator.EQUALS)) {
							if(binaryExp.getRight().isBooleanLiteralExpr()) {
								arg.add(new ExpressionStmt(
										new AssignExpr(binaryExp.getLeft(), 
												new MethodCallExpr("write",v.getRight()), 
										AssignExpr.Operator.ASSIGN)));
							}else if (binaryExp.getRight().isNameExpr()){
								arg.add(new ExpressionStmt(
										new AssignExpr(binaryExp.getLeft(),new MethodCallExpr("write",v.getRight()),
										AssignExpr.Operator.ASSIGN)));
								arg.add(new ExpressionStmt(
										new AssignExpr(binaryExp.getRight(), new MethodCallExpr("write",v.getLeft()), 
										AssignExpr.Operator.ASSIGN)));
							}
						} 
					}
				}
			}
		}
		return n;
	}
		
	
	public static void main(String[] args) throws FileNotFoundException {
		FileInputStream in = new FileInputStream("/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/test/burglarAlarm.java");
		CompilationUnit cu = JavaParser.parse(in);
		cu.accept(new OBSVisitor(), null);
		System.out.println(cu);
		cu.accept(new SVFVisitor(), null);
		System.out.println(cu);
		cu.accept(new MethodVisitor(), null);
		System.out.println(cu);
	}
	
	// observe(f == false)
	// observe(f == 1)
	// observe(f == g)
	// observe(f.equals(g))
	// observe(f == false && g == false)
	// observe(f == 1 && g == 1)
	// observe(f == g && f == h)
	// ...

	
}
