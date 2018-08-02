package kkkjjjmmm.slicer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.ibm.wala.examples.drivers.PDFSlice;
import com.ibm.wala.examples.drivers.SlicerTest;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.CommandLine;
import com.ibm.wala.util.io.FileProvider;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class DoSlice {

	private static final DataDependenceOptions D_OPTIONS = DataDependenceOptions.NO_EXCEPTIONS;
	private static final ControlDependenceOptions C_OPTIONS = ControlDependenceOptions.NO_EXCEPTIONAL_EDGES;//ControlDependenceOptions.NO_EXCEPTIONAL_EDGES;

	public static Table<String, String, Set<Integer>> computeSliceStatements(String appJar, String mainClass,
			String srcCaller, String srcCallee, boolean goBackward)
			throws IllegalArgumentException, CancelException, IOException {
		try {
			// create an analysis scope representing the appJar as a J2SE application
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar,
					(new FileProvider()).getFile("Java60RegressionExclusions.txt"));

			// build a class hierarchy, call graph, and system dependence graph
			ClassHierarchy cha = ClassHierarchyFactory.make(scope);
			Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha,
					mainClass);
			AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
			CallGraphBuilder<InstanceKey> builder = Util.makeVanillaZeroOneCFABuilder(options, new AnalysisCacheImpl(),
					cha, scope);
			// CallGraphBuilder builder = Util.makeZeroOneCFABuilder(options, new
			// AnalysisCache(), cha, scope);
			CallGraph cg = builder.makeCallGraph(options, null);

			// last two arguments control how the slicer handles data/control dependencies
			SDG<InstanceKey> sdg = new SDG<>(cg, builder.getPointerAnalysis(), D_OPTIONS, C_OPTIONS);

			// find the call statement of interest
			CGNode callerNode = SlicerTest.findMethod(cg, srcCaller);
			Statement s = SlicerTest.findCallTo(callerNode, srcCallee);
			System.err.println("Statement: " + s);
//			
//			IR ir = callerNode.getIR();
//			Iterator<SSAInstruction> it = ir.iterateAllInstructions();
//			while(it.hasNext()) {
//				SSAInstruction ins = it.next();
//				if(ins.toString().contains("Observe")) {
//					ISSABasicBlock block = ir.getBasicBlockForInstruction(ins);
//					System.out.println(block.getNumber());
//				}
//			}
			// compute the slice as a collection of statements
			Collection<Statement> slice = null;
			if (goBackward) {
				final PointerAnalysis<InstanceKey> pointerAnalysis = builder.getPointerAnalysis();
				slice = com.ibm.wala.ipa.slicer.Slicer.computeBackwardSlice(s, cg, pointerAnalysis, D_OPTIONS,
						C_OPTIONS);
			} else {
				// for forward slices ... we actually slice from the return value of
				// calls.
				s = PDFSlice.getReturnStatementForCall(s);
				final PointerAnalysis<InstanceKey> pointerAnalysis = builder.getPointerAnalysis();
				slice = com.ibm.wala.ipa.slicer.Slicer.computeForwardSlice(s, cg, pointerAnalysis, D_OPTIONS,
						C_OPTIONS);
			}
			return PDFSlice.dumpSourceLineNumbers(slice);
		} catch (WalaException e) {
			// something bad happened.
			e.printStackTrace();
			return HashBasedTable.create();
		}
	}

	public static void main(String[] args) {
		try {
			Properties p = CommandLine.parse(args);
			Table<String, String, Set<Integer>> statements = computeSliceStatements(p.getProperty("appJar"),
					p.getProperty("mainClass"), p.getProperty("srcCaller"), p.getProperty("srcCallee"), goBackward(p));
			
			for (String fullClassName : statements.rowKeySet()) {
				int lastSlash = fullClassName.lastIndexOf('/');
				String packageName = fullClassName.substring(1, lastSlash);
				String className = fullClassName.substring(lastSlash + 1);
				System.out.println(packageName);
				System.out.println(className);
				CompilationUnit cu = null;
				
				Set<Integer> sliceStatements = new TreeSet<>();
				for (Map.Entry<String, Set<Integer>> methodAndLines : statements.row(fullClassName).entrySet()) {
					String methodName = methodAndLines.getKey();
					Set<Integer> diffStatements = methodAndLines.getValue();
					sliceStatements.addAll(diffStatements);
					ModifierVisitor<Void> mv = new SlicerVisitor(diffStatements);

					SourceRoot srcRoot = new SourceRoot(Paths.get(p.getProperty("path")));
					cu = srcRoot.parse(packageName, className + ".java");
					
					Visitable newTree = cu.accept(mv, null);

					srcRoot = new SourceRoot(Paths.get(p.getProperty("path")));
					cu = srcRoot.parse(packageName, className + ".java");

					for (MethodDeclaration newMD : ((Node) newTree).findAll(MethodDeclaration.class)) {
						MethodDeclaration oldMD = cu.findFirst(MethodDeclaration.class,
								md -> md.getNameAsString().equals(newMD.getNameAsString())).get();
						List<String> sliceNameExprs = newMD.findAll(NameExpr.class).stream()
								.map(ne -> ne.getNameAsString()).collect(Collectors.toList());
						oldMD.findAll(VariableDeclarationExpr.class).stream().forEach(vd -> {
							for (VariableDeclarator var : vd.getVariables()) {
								if (sliceNameExprs.contains(var.getName().asString())) {
									sliceStatements.add(vd.getBegin().get().line);
								}
							}
						});
					}
				}
				System.out.println(">> " + statements);
				ModifierVisitor<Void> mv = new SlicerVisitor(sliceStatements);
				Visitable newTree = cu.accept(mv, null);
				System.out.println(newTree);
			}
		} catch (CancelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean goBackward(Properties p) {
		return !p.getProperty("dir", "backward").equals("forward");
	}

}