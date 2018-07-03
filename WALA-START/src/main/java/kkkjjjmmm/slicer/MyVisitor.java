package kkkjjjmmm.slicer;

import java.util.Iterator;
import java.util.Optional;

import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;

public class MyVisitor extends ModifierVisitor<Void>{
	
	private final String srcCaller;
	
	public MyVisitor(String srcCaller) {
		this.srcCaller = srcCaller;
	}

	@Override
	  public Visitable visit(MethodDeclaration n, Void arg) {
		if(n.getNameAsString().equals(srcCaller)) {			
			Optional<BlockStmt> bs = n.getBody();
			BlockStmt block = bs.get();
			BlockStmt newblock = new BlockStmt();
			Iterator<Statement> it = block.getStatements().iterator();
			String name = "variable";
			int i = 1;
			while (it.hasNext()) {
			      Statement s = it.next();		
			      
			      if(s.isIfStmt()) {			    	  
					IfStmt ifstmt = s.asIfStmt();
					newblock.addStatement(new ExpressionStmt(
							new AssignExpr(new VariableDeclarationExpr(PrimitiveType.booleanType(), name + i),
									new NameExpr(ifstmt.getCondition().toString()), AssignExpr.Operator.ASSIGN)));
					ifstmt.setCondition(new NameExpr(name + i));
					i++;
					try {
						Statement ss = ifstmt.getElseStmt().get();
						while (ss != null) {		
							if (ss.isIfStmt()) {
								IfStmt ifstmt1 = ss.asIfStmt();
								newblock.addStatement(new ExpressionStmt(new AssignExpr(
										new VariableDeclarationExpr(PrimitiveType.booleanType(), name + i),
										new NameExpr(ifstmt1.getCondition().toString()), AssignExpr.Operator.ASSIGN)));
								ifstmt1.setCondition(new NameExpr(name + i));
								ss = ifstmt1.getElseStmt().get();
								i++;
							} else {
								break;
							}
						}	  
					}catch(Exception e) {
						System.out.println("No else statement.");
					}
					
			      }
			      
			      if(s.isWhileStmt()) {
			    	  WhileStmt whilestmt = s.asWhileStmt();			    	 
			    	  newblock.addStatement(new ExpressionStmt(new AssignExpr(new VariableDeclarationExpr(PrimitiveType.booleanType(),name+i),
		    				  new NameExpr(whilestmt.getCondition().toString()) ,AssignExpr.Operator.ASSIGN)));
			    	  BlockStmt newb = new BlockStmt();
			    	  Statement whilebodyst = new ExpressionStmt(new AssignExpr(new NameExpr(name+i),
		    				  new NameExpr(whilestmt.getCondition().toString()) ,AssignExpr.Operator.ASSIGN));	    	 
			    	  Statement whilebody = whilestmt.getBody();
			    	  newb.addStatement(whilebody);
			    	  newb.addStatement(whilebodyst);
			    	  whilestmt.setCondition(new NameExpr(name+i));
			    	  whilestmt.setBody(newb);
			    	  i++;			    	  
			      }		      
			      
			      String a = null;
			      
			      if (s.isExpressionStmt()) {
			    	  Expression exp = s.asExpressionStmt().getExpression();
			        if(exp.isMethodCallExpr()) {
			        	MethodCallExpr mce = exp.asMethodCallExpr();
			        	String methodname = mce.getNameAsString();
			        	if(methodname.equals("Observe")) {
			        		Expression e = mce.getArgument(0);
			        		a = e.toString();
			        		newblock.addStatement(new ExpressionStmt(new AssignExpr(new VariableDeclarationExpr(PrimitiveType.booleanType(),name+i),
				    				  new NameExpr(a) ,AssignExpr.Operator.ASSIGN)));
			        		mce.setArgument(0, new NameExpr(name+i));			        		
			        		i++;
			        	}
			        }
			      }
			      
			      newblock.addStatement(s);	
			      
			      if(a!=null) {
			    	  newblock.addStatement(new ExpressionStmt(new AssignExpr(new NameExpr(a),
		        				new BooleanLiteralExpr(true) ,AssignExpr.Operator.ASSIGN)));
			      }	
			      
			    }
			n.setBody(newblock);
			System.out.println(newblock.toString());
		}
		return n;	
	}
}
