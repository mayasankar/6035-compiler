package edu.mit.compilers.cfg.optimizations;

import java.util.*;

import edu.mit.compilers.cfg.CFG;
import edu.mit.compilers.cfg.CFGProgram;
import edu.mit.compilers.cfg.lines.CFGLine;
import edu.mit.compilers.cfg.lines.CFGLine.CFGVisitor;

/**
 * Abstract class for dataflow optimizations that optimize each method separately.
 *
 */
public abstract class AbstractDataflowOptimization<P> implements Optimization {
	protected boolean forwards;
	protected NodeTransferFunction func;
	
	protected final Map<CFGLine, P> nodeValuesIn = new HashMap<>();
	protected final Map<CFGLine, P> nodeValuesOut = new HashMap<>();
	
	public abstract P getBot();
	
	public abstract P getInput();
	
	public abstract P join(P p1, P p2);
	
	public void optimize(CFGProgram program) {
		for(String methodName: program.getMethodNames()) {
			CFG methodCFG = program.getMethodCFG(methodName);
			analyzeCode(methodCFG);
			modifyCode(methodCFG);
		}
	}

	private void analyzeCode(CFG method) {
		Map<CFGLine, P> inputMap = (forwards)? nodeValuesIn: nodeValuesOut;
		Map<CFGLine, P> outputMap = (forwards)? nodeValuesOut: nodeValuesIn;
		List<CFGLine> worklist = new LinkedList<>();
		
		CFGLine firstNode = forwards? method.getStart(): method.getEnd();
		inputMap.put(firstNode, getInput());

		worklist.add(firstNode);
		while(!worklist.isEmpty()) {
			CFGLine nextLine = worklist.remove(0);
			
			P input = inputMap.get(nextLine);
			P output = func.transfer(nextLine, input);
			
			outputMap.put(nextLine, output);
			
			
		}
	}
	
	private void modifyCode(CFG method) {
		
	}
	
	
	abstract class NodeTransferFunction implements CFGVisitor<P> {
		P input;
		
		public P transfer(CFGLine line, P input) {
			this.input = input;
			return line.accept(this);
		}
	}
}
