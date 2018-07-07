package kkkjjjmmm.slicer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
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
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.CommandLine;
import com.ibm.wala.util.io.FileProvider;

public class PSlicer {
	private static final DataDependenceOptions D_OPTIONS = DataDependenceOptions.FULL;
	  private static final ControlDependenceOptions C_OPTIONS = ControlDependenceOptions.NO_EXCEPTIONAL_EDGES;

	  public static Table<String, String,Set<Integer>> computeSliceStatements(String appJar, String mainClass,
	      String srcCaller,
	      String srcCallee, boolean goBackward, String fileName)
	      throws IllegalArgumentException, CancelException, IOException {
	    try {
	      FileInputStream in = new FileInputStream(fileName);
		  CompilationUnit cu = JavaParser.parse(in);
	      cu.accept(new OBSVisitor(), null);
	      cu.accept(new SVFVisitor(), null);
	      
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
	      return HashBasedTable.create();
	    }
	  }
	  
	  public static void main(String[] args) {
	  
	  try {
	      Properties p = CommandLine.parse(args);
	      Table<String, String,Set<Integer>> statements = 
	          computeSliceStatements(p.getProperty("appJar"), p.getProperty("mainClass"), p.getProperty("srcCaller"),
	  				p.getProperty("srcCallee"), goBackward(p), "/home/jiaming/WALA/WALA-START/src/main/java/kkkjjjmmm/slicer/Example.java");
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
