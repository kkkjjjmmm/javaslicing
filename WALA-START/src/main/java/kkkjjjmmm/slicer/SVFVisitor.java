package kkkjjjmmm.slicer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class SVFVisitor extends ModifierVisitor<Map<Statement, Statement>> {

	String name = "variable";
	int i = 1;
	List<ExpressionStmt> whileToInsert = new ArrayList<>();
	List<ExpressionStmt> ifToInsert = new ArrayList<>();
	List<ExpressionStmt> observeToInsert = new ArrayList<>();

	// @Override
	// public Node visit(BlockStmt n, final Map<Statement,Statement> arg) {
	// BlockStmt blockstmt = new BlockStmt();
	// if(whileToInsert.size()!=0||ifToInsert.size()!=0||observeToInsert.size()!=0)
	// {
	// Iterator<Statement> it = n.getStatements().iterator();
	// while(it.hasNext()) {
	// Statement s = it.next();
	// if(s.isWhileStmt()) {
	// blockstmt.addStatement(whileToInsert.get(i-1));
	// WhileStmt ws = s.asWhileStmt();
	// Node node = visit(ws,null);
	// blockstmt.addStatement((Statement) node);
	// }else if(s.isIfStmt()){
	// blockstmt.addStatement(ifToInsert.get(i-1));
	// IfStmt is = s.asIfStmt();
	// Node node = visit(is,null);
	// blockstmt.addStatement((Statement) node);
	// }else if(s.isExpressionStmt()){
	// Expression exp = s.asExpressionStmt().getExpression();
	// if(exp.isMethodCallExpr()) {
	// MethodCallExpr mce = exp.asMethodCallExpr();
	// Node node = visit(mce,null);
	// blockstmt.addStatement(observeToInsert.get(i-1));
	// blockstmt.addStatement((Statement) node);
	// }
	// }else {
	// blockstmt.addStatement(s);
	// }
	// }
	// }
	// whileToInsert.clear();
	// ifToInsert.clear();
	// observeToInsert.clear();
	// return blockstmt;
	// }

	@Override
	public Node visit(WhileStmt n, final Map<Statement, Statement> arg) {
		whileToInsert.add(
				new ExpressionStmt(new AssignExpr(new VariableDeclarationExpr(PrimitiveType.booleanType(), name + i),
						new NameExpr(n.getCondition().toString()), AssignExpr.Operator.ASSIGN)));
		BlockStmt newb = new BlockStmt();
		Statement whilebodyst = new ExpressionStmt(new AssignExpr(new NameExpr(name + i),
				new NameExpr(n.getCondition().toString()), AssignExpr.Operator.ASSIGN));
		Statement whilebody = n.getBody();
		newb.addStatement(whilebody);
		newb.addStatement(whilebodyst);
		n.setCondition(new NameExpr(name + i));
		n.setBody(newb);
		i++;
		return n;
	}

	@Override
	public Node visit(BlockStmt n, final Map<Statement, Statement> arg) {
		Map<Statement, Statement> extractedConditions = new HashMap<>();
		super.visit(n, extractedConditions);

		if (!extractedConditions.isEmpty()) {
			List<Statement> blockStatements = n.getStatements();
			NodeList<Statement> updatedSVFStatements = new NodeList<>();

			for (Statement stmt : blockStatements) {
				//TODO the hashes of identical nodes do not match!
//				Statement extracted = extractedConditions.get(stmt);
				Optional<Map.Entry<Statement,Statement>> extracted = extractedConditions.entrySet().stream()
				.filter(entry -> entry.getKey().toString().equals(stmt.toString()))
				.findFirst();
				
				if (extracted.isPresent()) {
					updatedSVFStatements.add(extracted.get().getValue());
				}
				updatedSVFStatements.add(stmt);
			}
			return new BlockStmt(updatedSVFStatements);
		}
		return n;
	}

	// Map<Node,Node> if() -> q1 = !i && !d
	@Override
	public Node visit(IfStmt n, final Map<Statement,Statement> arg) {
		super.visit(n, arg);
		Expression condition = n.getCondition();
		
		if (!condition.isNameExpr()) { //TODO double-check this condition
			String newVarName = name + i++;
			ExpressionStmt extractedCondition = new ExpressionStmt(
					new AssignExpr(
							new VariableDeclarationExpr(PrimitiveType.booleanType(), newVarName),
							condition, AssignExpr.Operator.ASSIGN));
			IfStmt cloned = n.clone();
			arg.put(cloned, extractedCondition);
			cloned.setCondition(new NameExpr(newVarName));
			return cloned;
		}
		
		return n;
	}

	@Override
	public Node visit(MethodCallExpr n, final Map<Statement, Statement> arg) {
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
		if (obsArg instanceof NameExpr) {
			NameExpr variableInsideObs = (NameExpr) obsArg;
			observeToInsert.add(new ExpressionStmt(
					new AssignExpr(new VariableDeclarationExpr(PrimitiveType.booleanType(), name + i),
							variableInsideObs, AssignExpr.Operator.ASSIGN)));
			n.setArgument(0, new NameExpr(name + i));
			i++;
		}
		return n;
	}

	public static void main(String[] args) throws FileNotFoundException {
		FileInputStream in = new FileInputStream(
				"/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/slicer/Example.java");
		CompilationUnit cu = JavaParser.parse(in);
		cu.accept(new SVFVisitor(), null);
		System.out.println(cu);
	}
}
