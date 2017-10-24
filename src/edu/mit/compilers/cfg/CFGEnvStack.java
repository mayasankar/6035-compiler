package edu.mit.compilers.cfg;

import java.io.OutputStream;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.operator.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;


public class CFGEnvStack {

    List<CFGEnv> environments;

    public CFGEnvStack() {
        environments = new ArrayList<>();
    }

    public void removeEnvironment() { environments.remove(environments.size()-1); }
    protected CFGEnv getEnvironment() { return environments.get(environments.size()-1); }
    public void pushEnvironment(CFGEnv.EnvType type) {
        CFGEnv env = new CFGEnv(type);
        pushEnvironment(env);
    }
    protected void pushEnvironment(CFGEnv env) { environments.add(env); }
    public int getSize() { return environments.size(); }

    public void addVariable(VariableDescriptor v) {
        this.getEnvironment().add(v);
    }

    public VariableDescriptor get(String name) {
        return this.getEnvironment().get(name);
    }

    // returns the CFGLine we go to after breaking, and removes broken envs from stack
    public CFGLine handleBreak() {
        while (true) {
            if (environments.isEmpty()) {
                throw new RuntimeException("Can't have called break without a loop to break.");
            }
            if (getEnvironment().getType() == CFGEnv.EnvType.LOOP_BODY) {
                CFGEnv env = getEnvironment();
                removeEnvironment();
                return env.getFollowingLine();
            }
            removeEnvironment();
        }
    }

    // returns the CFGLine we go to after continuing; no modifications to env stack needed
    public CFGLine handleContinue() {
        for (int i = environments.size()-1; i>=0; i++) {
            if (environments.get(i).getType() == CFGEnv.EnvType.LOOP_BODY) {
                return environments.get(i).getStartLine();
            }
        }
        throw new RuntimeException("Can't have called continue without a loop to continue.");
    }

}
