package kkkjjjmmm.slicer;

import java.util.Iterator;
import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import com.google.common.collect.Lists;

public class DeclaratorVisitor extends GenericListVisitorAdapter<Node, Void> {

	public List<Node> visit(NameExpr n, Void arg){
		List<Node> declaredVars = Lists.newArrayList();
		declaredVars.add(n);
		return declaredVars;
	}
	
	
//	public List<Node> visit(NameExpr n, Void arg){
//		List<Node> declaredVars = Lists.newArrayList();
//		Node temp = n;
//		boolean flag = false;
//		while(temp.getParentNode().isPresent()) {
//			temp = temp.getParentNode().get();
//			if(temp instanceof MethodCallExpr) {
//				if(((MethodCallExpr) temp).getScope().isPresent()) {
//					String scope = ((MethodCallExpr) temp).getScope().get().toString();
//					if(!(Character.isLowerCase(scope.charAt(0)))) {
//						flag = true;
//					}
//				}
//				Iterator<Expression> methodArguments = ((MethodCallExpr) temp).getArguments().iterator();
//				while(methodArguments.hasNext()) {
//					Expression argument = methodArguments.next();
//					if(argument.equals(n)) {
//						flag = false;
//						break;
//					}else {
//						flag = true;
//					}
//				}
//			}
//		}
//		if(flag!=true) {
//			declaredVars.add(n);
//			//System.out.println("NameExpr: "+n);
//		}
//		return declaredVars;
//	}
	
	
//	public List<Node> visit(final AssignExpr n, Void arg) {
//
//		if (!n.getOperator().equals(AssignExpr.Operator.ASSIGN)) {
//			return null;
//		}
//		System.out.println(n.getTarget());
//		System.out.println(n.getValue());
//		return null;
//	}
	
	

//	public List<Node> visit(final VariableDeclarationExpr n, Void arg) {
//		List<Node> declaredVars = Lists.newArrayList();
//
//		for (Node declaration : n.getChildNodes()) {
//
//			if (declaration instanceof VariableDeclarator) {
//				VariableDeclarator declarationVariable = (VariableDeclarator) declaration;
//				if (declarationVariable.getInitializer().isPresent()) {
//					final NameExpr targetVariable = new NameExpr((SimpleName) (declarationVariable.getChildNodes().get(1)));
//					declaredVars.add(targetVariable);
//				}
//			}
//		}
//
//		return declaredVars;
//	}
	
//	public List<Node> visit(final IfStmt n, Void arg) {
//		System.out.println(n.getCondition());
//		return null;
//	}
//	
//	
//	
//	public List<Node> visit(final WhileStmt n, Void arg) {
//		System.out.println(n.getCondition());
//		return null;
//	}

//	public List<Node> visit(final MethodCallExpr n, Void arg) {
//		String name = n.getName().asString();
//		if (!name.equals("Observe")) {
//			return null;
//		}
//		NodeList<Expression> args = n.getArguments();
//		if (args.size() != 1) {
//			System.out.println("wrong number of arguments!");
//			return null;
//		}
//		Expression obsArg = args.get(0);
//		if (obsArg instanceof NameExpr) {
//			List<Node> list = new ArrayList<>();
//			Node node = (Node) obsArg;
//			list.add(node);
//			return list;
//		} else if (obsArg instanceof UnaryExpr) {
//			List<Node> list = new ArrayList<>();
//			UnaryExpr uexp = (UnaryExpr) obsArg;
//			if (uexp.getOperator().equals(UnaryExpr.Operator.LOGICAL_COMPLEMENT)) {
//				list.add((Node) uexp.getExpression());
//				return list;
//			}
//		} else if (obsArg instanceof BinaryExpr) {
//			BinaryExpr v = (BinaryExpr) obsArg;
//			List<Node> list = new ArrayList<>();
//			if (v.getOperator().equals(BinaryExpr.Operator.EQUALS)) {
//				list.add((Node) v.getLeft());
//				return list;
//			} else if (v.getOperator().equals(BinaryExpr.Operator.AND)) {
//				Expression left = v.getLeft();
//				Expression right = v.getRight();
//				Set<Expression> expInV = new HashSet<Expression>();
//				while (left instanceof BinaryExpr
//						&& ((BinaryExpr) left).getOperator().equals(BinaryExpr.Operator.AND)) {
//					expInV.add(((BinaryExpr) left).getRight());
//					left = ((BinaryExpr) left).getLeft();
//				}
//				expInV.add(left);
//				expInV.add(right);
//				Iterator<Expression> it = expInV.iterator();
//				while (it.hasNext()) {
//					Expression exp = it.next();
//					if (exp instanceof NameExpr) {
//						Node variableInsideObs = (Node) obsArg;
//						list.add(variableInsideObs);
//					} else if (exp instanceof UnaryExpr) {
//						UnaryExpr uexp = (UnaryExpr) obsArg;
//						if (uexp.getOperator().equals(UnaryExpr.Operator.LOGICAL_COMPLEMENT)) {
//							list.add((Node) uexp.getExpression());
//						}
//					} else if (exp instanceof BinaryExpr) {
//						BinaryExpr bv = (BinaryExpr) obsArg;
//						if (bv.getOperator().equals(BinaryExpr.Operator.EQUALS)) {
//							list.add((Node) bv.getLeft());
//						}
//					}
//				}
//				return list;
//			}
//		}
//		return null;
//	}
}
