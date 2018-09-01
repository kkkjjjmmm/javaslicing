package kkkjjjmmm.slicer;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SlicerVisitor extends ModifierVisitor<List<Node>> {

	private final Set<Integer> statements;

	public SlicerVisitor(Set<Integer> statements) {
		this.statements = statements;
	}

	public Visitable visit(BlockStmt n, List<Node> arg) {
		// Figure out what to get and what to cast simply by looking at the AST in a
		// debugger!
		Iterator<Statement> it = n.getStatements().iterator();
		while (it.hasNext()) {
			Node node = it.next();
			if (!containsSliceStatement(node)) {
				if (((Statement) node).isExpressionStmt()) {
					Expression exp = ((Statement) node).asExpressionStmt().getExpression();
					if (exp.isVariableDeclarationExpr()) {
					
					} else {
						it.remove();
					}
				} else {
					it.remove();
				}
			}

		}
		return super.visit(n, arg);
	}

	@Override
	public Visitable visit(ClassOrInterfaceDeclaration n, List<Node> arg) {
		// Figure out what to get and what to cast simply by looking at the AST in a
		// debugger!
		Iterator<BodyDeclaration<?>> it = n.getMembers().iterator();
		while (it.hasNext()) {
			Node node = it.next();
			if (!containsSliceStatement(node)) {
				it.remove();
			}
		}
		return super.visit(n, arg);
	}

	private boolean containsSliceStatement(Node node) {
		Position beginning = node.getBegin().get();
		Position end = node.getEnd().get();
		return statements.stream().anyMatch(line -> beginning.line <= line && end.line >= line);

	}
}
