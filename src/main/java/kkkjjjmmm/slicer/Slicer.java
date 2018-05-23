package kkkjjjmmm.slicer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.TreeVisitor;
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
import java.util.Collection;
import java.util.List;

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
      return null;
    }
  }


  public static void main(String[] args) {
    try {
      List<Integer> statements = computeSliceStatements("example.jar", "LFoo", "main", "foo", true);

      SourceRoot srcRoot = new SourceRoot(Paths.get("example/src"));
      CompilationUnit cu = srcRoot.parse("", "Foo.java");

      TreeVisitor visitor = new TreeVisitor() {
        @Override
        public void process(Node node) {
          if (node instanceof com.github.javaparser.ast.stmt.Statement) {
            // Figure out what to get and what to cast simply by looking at the AST in a debugger!

            boolean hasBeginning = ((com.github.javaparser.ast.stmt.Statement) node).getBegin()
                .filter(pos -> statements.contains(pos.line)).isPresent();
            boolean hasEnd = ((com.github.javaparser.ast.stmt.Statement) node).getEnd()
                .filter(pos -> statements.contains(pos.line)).isPresent();

            if (hasBeginning || hasEnd) {
              System.out.println((com.github.javaparser.ast.stmt.Statement) node);
            }

//          return super.visit(stmt, arg);
          }
        }
      };
      visitor.visitPreOrder(cu.findRootNode());

      System.out.println(statements);
    } catch (CancelException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
