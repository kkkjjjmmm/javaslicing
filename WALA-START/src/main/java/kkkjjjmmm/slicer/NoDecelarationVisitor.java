package kkkjjjmmm.slicer;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class NoDecelarationVisitor extends ModifierVisitor<Void>{
	
	private final Set<Integer> statements;

	public NoDecelarationVisitor(Set<Integer> statements) {
		this.statements = statements;
	}

	public Visitable visit(BlockStmt n, Void arg) {
		Iterator<Statement> it = n.getStatements().iterator();
		while (it.hasNext()) {
			Node node = it.next();
			if (!containsSliceStatement(node)) {
					it.remove();
			}
		}
		return super.visit(n, arg);
	}

	@Override
	public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
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
