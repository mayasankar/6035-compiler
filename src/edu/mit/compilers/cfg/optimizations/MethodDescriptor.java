package edu.mit.compilers.cfg.optimizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.mit.compilers.cfg.*;
import edu.mit.compilers.cfg.lines.*;
import edu.mit.compilers.ir.USEVisitor;
import edu.mit.compilers.ir.decl.IRMethodDecl;

public class MethodDescriptor {
    final String methodName;
    final boolean isImport;
    Set<String> methodsCalled = new HashSet<>(); // might include methodName
    boolean isRecursive; // equivalent to whether it calls itself
    Set<String> globalsUsed = new HashSet<>();
    Set<String> globalsPossiblyAssigned = new HashSet<>();

    private MethodDescriptor(String name) {
        this.methodName = name;
        this.isImport = false;
    }

    private MethodDescriptor(String name, boolean isImport) {
        this.methodName = name;
        this.isImport = isImport;
        if (isImport) {
            isRecursive = false;
        }
    }

    public Set<String> getGlobalsUsed() { return globalsUsed; }
    public Set<String> getGlobalsAssigned(boolean includeArrays) { return globalsPossiblyAssigned; }

    public static Map<String, MethodDescriptor> calculateMethodDescriptors(CFGProgram cfgProgram) {
        Map<String, MethodDescriptor> descriptors = new HashMap<>();
        Set<String> globals = cfgProgram.getGlobalNames();
        for (IRMethodDecl method : cfgProgram.getMethodList()) {
            String methodName = method.getName();
            MethodDescriptor desc = new MethodDescriptor(methodName);
            if (! method.isImport()) {
                CFG cfg = cfgProgram.getMethodToCFGMap().get(methodName);
                MDCreator creator = new MDCreator(desc);
                for (CFGLine line : cfg.getAllLines()) {
                    line.accept(creator);
                }
                desc.globalsUsed.retainAll(globals);
            }
            desc.isRecursive = desc.methodsCalled.contains(methodName);
            descriptors.put(methodName, desc);
            for (String nestedMethod : desc.methodsCalled) {
                desc.globalsUsed.addAll(descriptors.get(nestedMethod).globalsUsed);
                desc.globalsPossiblyAssigned.addAll(descriptors.get(nestedMethod).globalsPossiblyAssigned);
            }
        }
        return descriptors;
    }

    // called in order of method declarations
    // calculates methodsCalled and starts calculations of globalsUsed and globalsPossiblyAssigned
    // always returns true
    private static class MDCreator implements CFGLine.CFGVisitor<Boolean> {
        MethodDescriptor desc;
        CfgUseVisitor USE = new CfgUseVisitor();
        CfgAssignVisitor ASSIGN = new CfgAssignVisitor();

        public MDCreator(MethodDescriptor desc) { this.desc = desc; }

        private Boolean onAllLines(CFGLine line) {
            // at the end, we will intersect these sets with globals.
            desc.globalsUsed.addAll(line.accept(USE));
            desc.globalsPossiblyAssigned.addAll(line.accept(ASSIGN));
            return true;
        }

        public Boolean on(CFGAssignStatement line) { return onAllLines(line); }
        public Boolean on(CFGBoundsCheck line) { return onAllLines(line); }
        public Boolean on(CFGConditional line) { return onAllLines(line); }
        public Boolean on(CFGNoOp line) { return onAllLines(line); }
        public Boolean on(CFGNoReturnError line) { return true; }
        public Boolean on(CFGReturn line) { return onAllLines(line); }
        public Boolean on(CFGMethodCall line) {
            desc.methodsCalled.add(line.getExpression().getName());
            return onAllLines(line);
        }
        public Boolean on(CFGBlock line) { return onAllLines(line); }
    }
}
