package kkkjjjmmm.slicer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
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
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Slicer {

  private static final DataDependenceOptions D_OPTIONS = DataDependenceOptions.FULL;
  private static final ControlDependenceOptions C_OPTIONS = ControlDependenceOptions.NO_EXCEPTIONAL_EDGES;

  public static List<Integer> computeSliceStatements(String appJar, String mainClass,
      String srcCaller,
      String srcCallee, boolean goBackward)
      throws IllegalArgumentException, CancelException, IOException {
    try {
      // create an analysis scope representing the appJar as a J2SE application
      AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar,
          (new FileProvider()).getFile("Java60RegressionExclusions.txt"));

      // build a class hierarchy, call graph, and system dependence graph
      ClassHierarchy cha = ClassHierarchyFactory.make(scope);
      Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util
          .makeMainEntrypoints(scope, cha,
              mainClass);
      AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
      CallGraphBuilder<InstanceKey> builder = Util
          .makeVanillaZeroOneCFABuilder(options, new AnalysisCacheImpl(),
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

      // compute the slice as a collection of statements
      Collection<Statement> slice = null;
      if (goBackward) {
        final PointerAnalysis<InstanceKey> pointerAnalysis = builder.getPointerAnalysis();
        slice = com.ibm.wala.ipa.slicer.Slicer
            .computeBackwardSlice(s, cg, pointerAnalysis, D_OPTIONS, C_OPTIONS);
      } else {
        // for forward slices ... we actually slice from the return value of
        // calls.
        s = PDFSlice.getReturnStatementForCall(s);
        final PointerAnalysis<InstanceKey> pointerAnalysis = builder.getPointerAnalysis();
        slice = com.ibm.wala.ipa.slicer.Slicer
            .computeForwardSlice(s, cg, pointerAnalysis, D_OPTIONS, C_OPTIONS);
      }
      return PDFSlice.dumpSourceLineNumbers(slice);
    } catch (WalaException e) {
      // something bad happened.
      e.printStackTrace();
      return new ArrayList<>();
    }
  }


  public static void main(String[] args) {
    try {
      Set<Integer> statements = new TreeSet<>(
          computeSliceStatements("example.jar", "LFoo", "main", "foo", true));

      SourceRoot srcRoot = new SourceRoot(Paths.get("example/src"));
      CompilationUnit cu = srcRoot.parse("", "Foo.java");

      ModifierVisitor<Void> mv = new SlicerVisitor(statements);
      Visitable newTree = cu.accept(mv, null);
      System.out.println(newTree);
      System.out.println("-------------- second pass -------------");

      srcRoot = new SourceRoot(Paths.get("example/src"));
      cu = srcRoot.parse("", "Foo.java");

      for (MethodDeclaration newMD : ((Node) newTree).findAll(MethodDeclaration.class)) {
        MethodDeclaration oldMD = cu.findFirst(MethodDeclaration.class,
            md -> md.getNameAsString().equals(newMD.getNameAsString())).get();
        List<String> sliceNameExprs = newMD.findAll(NameExpr.class).stream()
            .map(ne -> ne.getNameAsString())
            .collect(Collectors.toList());
        oldMD.findAll(VariableDeclarationExpr.class).stream()
            .forEach(vd -> {
              for (VariableDeclarator var : vd.getVariables()) {
                if (sliceNameExprs.contains(var.getName().asString())) {
                  statements.add(vd.getBegin().get().line);
                }
              }
            });
      }
      System.out.println(">> " + statements);
      mv = new SlicerVisitor(statements);
      newTree = cu.accept(mv, null);
      System.out.println(newTree);
    } catch (CancelException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
