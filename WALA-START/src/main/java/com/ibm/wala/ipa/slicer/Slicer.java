/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.ipa.slicer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.dataflow.IFDS.BackwardsSupergraph;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.dataflow.IFDS.IPartiallyBalancedFlowFunctions;
import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.dataflow.IFDS.PartiallyBalancedTabulationProblem;
import com.ibm.wala.dataflow.IFDS.PartiallyBalancedTabulationSolver;
import com.ibm.wala.dataflow.IFDS.PathEdge;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.dataflow.IFDS.UnorderedDomain;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstruction.IVisitor;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.MonitorUtil.IProgressMonitor;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;

/**
 * A demand-driven context-sensitive slicer.
 *
 * This computes a context-sensitive slice, building an SDG and finding realizable paths to a statement using tabulation.
 *
 * This implementation uses a preliminary pointer analysis to compute data dependence between heap locations in the SDG.
 */
public class Slicer {

  public final static boolean DEBUG = false;

  public final static boolean VERBOSE = false;

  /**
   * options to control data dependence edges in the SDG
   */
  public static enum DataDependenceOptions {
    FULL("full", false, false, false, false), NO_BASE_PTRS("no_base_ptrs", true, false, false, false), NO_BASE_NO_HEAP(
        "no_base_no_heap", true, true, false, false), NO_BASE_NO_EXCEPTIONS("no_base_no_exceptions", true, false, false, true), NO_BASE_NO_HEAP_NO_EXCEPTIONS(
        "no_base_no_heap_no_exceptions", true, true, false, true), NO_HEAP("no_heap", false, true, false, false), NO_HEAP_NO_EXCEPTIONS(
        "no_heap_no_exceptions", false, true, false, true), NO_EXCEPTIONS("no_exceptions", false, false, false, true), NONE("none",
        true, true, true, true), REFLECTION("no_base_no_heap_no_cast", true, true, true, true);

    private final String name;

    /**
     * Ignore data dependence edges representing base pointers? e.g for a statement y = x.f, ignore the data dependence edges for x
     */
    private final boolean ignoreBasePtrs;

    /**
     * Ignore all data dependence edges to or from the heap?
     */
    private final boolean ignoreHeap;

    /**
     * Ignore outgoing data dependence edges from a cast statements? [This is a special case option used for reflection processing]
     */
    private final boolean terminateAtCast;

    /**
     * Ignore data dependence manifesting throw exception objects?
     */
    private final boolean ignoreExceptions;

    DataDependenceOptions(String name, boolean ignoreBasePtrs, boolean ignoreHeap, boolean terminateAtCast, boolean ignoreExceptions) {
      this.name = name;
      this.ignoreBasePtrs = ignoreBasePtrs;
      this.ignoreHeap = ignoreHeap;
      this.terminateAtCast = terminateAtCast;
      this.ignoreExceptions = ignoreExceptions;
    }

    public final boolean isIgnoreBasePtrs() {
      return ignoreBasePtrs;
    }

    public final boolean isIgnoreHeap() {
      return ignoreHeap;
    }

    public final boolean isIgnoreExceptions() {
      return ignoreExceptions;
    }

    /**
     * Should data dependence chains terminate at casts? This is used for reflection processing ... we only track flow into casts
     * ... but not out.
     */
    public final boolean isTerminateAtCast() {
      return terminateAtCast;
    }

    public final String getName() {
      return name;
    }
  }

  /**
   * options to control control dependence edges in the sdg
   */
  public static enum ControlDependenceOptions {
    /**
     * track all control dependencies
     */
    FULL("full", false, false),

    /**
     * track no control dependencies
     */
    NONE("none", true, true),

    /**
     * don't track control dependence due to exceptional control flow
     */
    NO_EXCEPTIONAL_EDGES("no_exceptional_edges", true, false),

    /**
     * don't track control dependence from caller to callee
     */
    NO_INTERPROC_EDGES("no_interproc_edges", false, true),

    /**
     * don't track interprocedural or exceptional control dependence
     */
    NO_INTERPROC_NO_EXCEPTION("no_interproc_no_exception", true, true);


    private final String name;

    /**
     * ignore control dependence due to exceptional control flow?
     */
    private final boolean ignoreExceptionalEdges;

    /**
     * ignore interprocedural control dependence, i.e., from caller to callee or the reverse?
     */
    private final boolean ignoreInterprocEdges;


    ControlDependenceOptions(String name, boolean ignoreExceptionalEdges, boolean ignoreInterprocEdges) {
      this.name = name;
      this.ignoreExceptionalEdges = ignoreExceptionalEdges;
      this.ignoreInterprocEdges = ignoreInterprocEdges;
    }

    public final String getName() {
      return name;
    }

    public final boolean isIgnoreExceptions() {
      return ignoreExceptionalEdges;
    }

    public final boolean isIgnoreInterproc() {
      return ignoreInterprocEdges;
    }

  }

  /**
   * @param s a statement of interest
   * @return the backward slice of s.
   * @throws CancelException
   */
  public static <U extends InstanceKey> Collection<Statement> computeBackwardSlice(Statement s, CallGraph cg, PointerAnalysis<U> pa,
      DataDependenceOptions dOptions, ControlDependenceOptions cOptions) throws IllegalArgumentException, CancelException {
    return computeSlice(new SDG<U>(cg, pa, ModRef.<U>make(), dOptions, cOptions), Collections.singleton(s), true);
  }

  /**
   * @param s a statement of interest
   * @return the forward slice of s.
   * @throws CancelException
   */
  public static <U extends InstanceKey> Collection<Statement> computeForwardSlice(Statement s, CallGraph cg,
      PointerAnalysis<U> pa,
      DataDependenceOptions dOptions, ControlDependenceOptions cOptions) throws IllegalArgumentException, CancelException {
    return computeSlice(new SDG<U>(cg, pa, ModRef.<U>make(), dOptions, cOptions), Collections.singleton(s), false);
  }

  /**
   * Use the passed-in SDG
   *
   * @throws CancelException
   */
  public static Collection<Statement> computeBackwardSlice(SDG sdg, Statement s) throws IllegalArgumentException, CancelException {
    return computeSlice(sdg, Collections.singleton(s), true);
  }

  /**
   * Use the passed-in SDG
   *
   * @throws CancelException
   */
  public static Collection<Statement> computeForwardSlice(SDG sdg, Statement s) throws IllegalArgumentException, CancelException {
    return computeSlice(sdg, Collections.singleton(s), false);
  }

  /**
   * Use the passed-in SDG
   *
   * @throws CancelException
   */
  public static Collection<Statement> computeBackwardSlice(SDG sdg, Collection<Statement> ss) throws IllegalArgumentException,
      CancelException {
    return computeSlice(sdg, ss, true);
  }

  /**
   * @param ss a collection of statements of interest
   * @throws CancelException
   */
  protected static Collection<Statement> computeSlice(SDG sdg, Collection<Statement> ss, boolean backward) throws CancelException {
    if (sdg == null) {
      throw new IllegalArgumentException("sdg cannot be null");
    }
    return new Slicer().slice(sdg, ss, backward);
  }

  /**
   * Main driver logic.
   *
   * @param sdg governing system dependence graph
   * @param roots set of roots to slice from
   * @param backward do a backwards slice?
   * @return the {@link Statement}s found by the slicer
   * @throws CancelException
   */
  public Collection<Statement> slice(SDG sdg, Collection<Statement> roots, boolean backward) throws CancelException {
    return slice(sdg, roots, backward, null);
  }

  /**
   * Main driver logic.
   *
   * @param sdg governing system dependence graph
   * @param roots set of roots to slice from
   * @param backward do a backwards slice?
   * @param monitor to cancel analysis if needed
   * @return the {@link Statement}s found by the slicer
   * @throws CancelException
   */
  public Collection<Statement> slice(SDG sdg, Collection<Statement> roots, boolean backward, IProgressMonitor monitor)
      throws CancelException {
    if (sdg == null) {
      throw new IllegalArgumentException("sdg cannot be null");
    }

    SliceProblem p = makeSliceProblem(roots, sdg, backward);

    PartiallyBalancedTabulationSolver<Statement, PDG<?>, Object> solver = PartiallyBalancedTabulationSolver
        .createPartiallyBalancedTabulationSolver(p, monitor);
    TabulationResult<Statement, PDG<?>, Object> tr = solver.solve();

    Collection<Statement> slice = tr.getSupergraphNodesReached();

    if (VERBOSE) {
      System.err.println("Slicer done.");
    }

    return slice;
  }

  /**
   * Return an object which encapsulates the tabulation logic for the slice problem. Subclasses can override this method to
   * implement special semantics.
   */
  protected SliceProblem makeSliceProblem(Collection<Statement> roots, ISDG sdgView, boolean backward) {
    return new SliceProblem(roots, sdgView, backward);
  }

  /**
   * @param s a statement of interest
   * @return the backward slice of s.
   * @throws CancelException
   */
  public static Collection<Statement> computeBackwardSlice(Statement s, CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis)
      throws IllegalArgumentException, CancelException {
    return computeBackwardSlice(s, cg, pointerAnalysis, DataDependenceOptions.FULL, ControlDependenceOptions.FULL);
  }

  /**
   * Tabulation problem representing slicing
   *
   */
  public static class SliceProblem implements PartiallyBalancedTabulationProblem<Statement, PDG<?>, Object> {

    private final Collection<Statement> roots;

    private final ISupergraph<Statement,PDG<? extends InstanceKey>> supergraph;

    private final SliceFunctions f;

    private final boolean backward;

    public SliceProblem(Collection<Statement> roots, ISDG sdg, boolean backward) {
      this.roots = roots;
      this.backward = backward;

      // TODO add dependencies to PDG
      // TODO handle programs with multiple functions
      Statement returnStmt = null;
      Set<Statement> defObserveVariables = new HashSet<>();
      
      // see section 4.1 (influencers) for more details
      Iterator<Statement> rootIt = roots.iterator();
      while (rootIt.hasNext()) {

        Statement statement = rootIt.next();
        IR ir = statement.getNode().getIR();
        DefUse df = new DefUse(ir);
        Iterator<SSAInstruction> instructionIt = ir.iterateAllInstructions();

        // compute return/observe variables
        while (instructionIt.hasNext()) {
          SSAInstruction instruction = instructionIt.next();
          if (instruction instanceof SSAInvokeInstruction) {
            String signature = ((SSAInvokeInstruction) instruction)
                .getDeclaredTarget()
                .getSignature();
            if (signature.contains("observe")) { // TODO use the proper signature
              assert instruction.getNumberOfUses() > 0;
              assert ((SSAInvokeInstruction) instruction).getNumberOfParameters() > 0;
              int argValue = instruction.getUse(0); // observe parameter
              SSAInstruction parameterDefinition = df.getDef(argValue);

              // see https://github.com/wala/WALA/wiki/Slicer#api for details
              Statement parameterDefStatement = new NormalStatement(statement.getNode(),
                  parameterDefinition.iindex);
              defObserveVariables.add(parameterDefStatement);
            } else if (signature.contains("fake")) { // TODO also use the proper signature here
              assert returnStmt == null; // only want to write it once
              assert instruction.getNumberOfDefs() > 0;
              assert ((SSAInvokeInstruction) instruction).getNumberOfParameters() > 0;

              // not the real "return" from the method, just our slicing criteria
              returnStmt = new NormalStatement(statement.getNode(),
                  instruction.iindex);
            }
          }
        }
      }
      assert returnStmt != null;
      // compute INF(O,G), populate pdg
      for (Statement defObserveVar : defObserveVariables) {
        sdg.addEdge(defObserveVar, returnStmt);
      }
      
      SDGSupergraph forwards = new SDGSupergraph(sdg, backward);
      if (backward)
        this.supergraph = BackwardsSupergraph.make(forwards);
      else
        this.supergraph = forwards;
      this.f = new SliceFunctions();
    }
	
	public static synchronized Statement ssaInstruction2Statement(CGNode node, SSAInstruction s,
		      Integer instructionIndices, IR ir) {
		    if (node == null) {
		      throw new IllegalArgumentException("null node");
		    }
		    if (s == null) {
		      throw new IllegalArgumentException("null s");
		    }
		    if (s instanceof SSAPhiInstruction) {
		      SSAPhiInstruction phi = (SSAPhiInstruction) s;
		      return new PhiStatement(node, phi);
		    } else if (s instanceof SSAPiInstruction) {
		      SSAPiInstruction pi = (SSAPiInstruction) s;
		      return new PiStatement(node, pi);
		    } else if (s instanceof SSAGetCaughtExceptionInstruction) {
		      return new GetCaughtExceptionStatement(node, ((SSAGetCaughtExceptionInstruction) s));
		    } else {
		      if (instructionIndices == null) {
		        Assertions.UNREACHABLE(s.toString() + "\nnot found in map of\n" + ir);
		      }
		      return new NormalStatement(node, instructionIndices.intValue());
		    }
		  }

    /*
     * @see com.ibm.wala.dataflow.IFDS.TabulationProblem#getDomain()
     */
    @Override
    public TabulationDomain<Object, Statement> getDomain() {
      // a dummy
      return new UnorderedDomain<Object, Statement>();
    }

    /*
     * @see com.ibm.wala.dataflow.IFDS.TabulationProblem#getFunctionMap()
     */
    @Override
    public IPartiallyBalancedFlowFunctions<Statement> getFunctionMap() {
      return f;
    }

    /*
     * @see com.ibm.wala.dataflow.IFDS.TabulationProblem#getMergeFunction()
     */
    @Override
    public IMergeFunction getMergeFunction() {
      return null;
    }

    /*
     * @see com.ibm.wala.dataflow.IFDS.TabulationProblem#getSupergraph()
     */
    @Override
    public ISupergraph<Statement, PDG<?>> getSupergraph() {
      return supergraph;
    }

    @Override
    public Collection<PathEdge<Statement>> initialSeeds() {
      if (backward) {
        Collection<PathEdge<Statement>> result = HashSetFactory.make();
        for (Statement st : roots) {
          PathEdge<Statement> seed = PathEdge.createPathEdge(new MethodExitStatement(st.getNode()), 0, st, 0);
          result.add(seed);
        }
        return result;
      } else {
        Collection<PathEdge<Statement>> result = HashSetFactory.make();
        for (Statement st : roots) {
          PathEdge<Statement> seed = PathEdge.createPathEdge(new MethodEntryStatement(st.getNode()), 0, st, 0);
          result.add(seed);
        }
        return result;
      }
    }

    @Override
    public Statement getFakeEntry(Statement node) {
      return backward ? new MethodExitStatement(node.getNode()) : new MethodEntryStatement(node.getNode());
    }

  }

}
