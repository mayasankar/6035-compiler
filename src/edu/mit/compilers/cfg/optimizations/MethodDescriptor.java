package edu.mit.compilers.cfg.optimizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.ir.USEVisitor;

public class MethodDescriptor {
    String methodName;
    Set<String> methodsCalled = new HashSet<>(); // might include methodName
    boolean isRecursive = false; // equivalent to whether it calls itself
    Set<String> globalsUsed = new HashSet<>();
    //Set<String> globalsPossiblyAssigned; // TODO

    private MethodDescriptor(String name) {
        this.methodName = name;
    }

    public Set<String> getGlobalsUsed() { return globalsUsed; }

    public static Map<String, MethodDescriptor> calculateMethodDescriptors(CFGProgram cfgProgram) {
        Map<String, MethodDescriptor> descriptors = new HashMap<>();
        Set<String> globals = cfgProgram.getGlobalNames();
        for (String methodName : cfgProgram.getMethodNames()) {
            CFG cfg = cfgProgram.getMethodToCFGMap().get(methodName);
            MethodDescriptor desc = new MethodDescriptor(methodName);
            MDCreator creator = new MDCreator(desc);
            for (CFGLine line : cfg.getAllLines()) {
                line.accept(creator);
            }
            desc.globalsUsed.retainAll(globals);
            desc.isRecursive = desc.methodsCalled.contains(methodName); // remove instead of contains also works
            descriptors.put(methodName, desc);
            for (String nestedMethod : desc.methodsCalled) {
                desc.globalsUsed.addAll(descriptors.get(nestedMethod).globalsUsed);
            }
        }
        return descriptors;
    }

    // called in order of method declarations
    // calculates methodsCalled and starts calculation of globalsUsed
    // always returns true
    private static class MDCreator implements CFGLine.CFGVisitor<Boolean> {
        MethodDescriptor desc;
        CfgUseVisitor USE = new CfgUseVisitor();

        public MDCreator(MethodDescriptor desc) { this.desc = desc; }

        private Boolean onAllLines(CFGLine line) {
            desc.globalsUsed.addAll(line.accept(USE)); // at the end, we will intersect this set with globals.
            return true;
        }

        public Boolean on(CFGAssignStatement line) { return onAllLines(line); }
        public Boolean on(CFGBoundsCheck line) { return onAllLines(line); }
        public Boolean on(CFGConditional line) { return onAllLines(line); }
        public Boolean on(CFGNoOp line) { return onAllLines(line); }
        public Boolean on(CFGReturn line) { return onAllLines(line); }
        public Boolean on(CFGMethodCall line) {
            desc.methodsCalled.add(line.getExpression().getName());
            return onAllLines(line);
        }
        public Boolean on(CFGBlock line) { return onAllLines(line); }
    }
}
