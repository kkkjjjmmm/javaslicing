package kkkjjjmmm.slicer;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.google.common.collect.Sets;

public class DeclarationPruningVisitor extends ModifierVisitor<List<Node>> {

	public Visitable visit(BlockStmt n, List<Node> arg) {
		Iterator<Statement> it = n.getStatements().iterator();
		while (it.hasNext()) {
			Node node = it.next();

			if (((Statement) node).isExpressionStmt()) {
				Expression exp = ((Statement) node).asExpressionStmt().getExpression();
				if (exp.isVariableDeclarationExpr()) {

					if (arg != null) {
						Set<VariableDeclarator> declarationsToBeRemoved = Sets.newHashSet();
						final Set<SimpleName> variablesIdentifiersInTheSlice = arg.stream()
								.map(varId -> ((NameExpr) varId).getName()).collect(Collectors.toSet());
						for (VariableDeclarator declaredVariable : exp.asVariableDeclarationExpr().getVariables()) {
							if (!variablesIdentifiersInTheSlice.contains(declaredVariable.getName())) {
								declarationsToBeRemoved.add(declaredVariable);
							}
						}

						declarationsToBeRemoved.forEach(v -> {
							//System.out.println("Pruning variable: " + v);
							exp.asVariableDeclarationExpr().getVariables().remove(v);
						});

						if (exp.asVariableDeclarationExpr().getVariables().isEmpty()) {
							it.remove();
						}

					}

				}
			}
		}

		return super.visit(n, arg);
	}

}
