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


public class CFGEnv {

    public enum EnvType {
		LOOP_BODY, // for/while
        BRANCH_BODY, // if/else
        BLOCK // basically all else
	}

    VariableTable variables;
    EnvType type;
    CFGLine startLine;  // where we go to after continue
    CFGLine followingLine;  // where we go to after break

    public CFGEnv(EnvType t) {
        type = t;
        variables = new VariableTable();
        startLine = null;
        followingLine = null;
    }

    public VariableDescriptor get(String name) { return variables.get(name); }
    public void add(VariableDescriptor v) { variables.add(v); }
    public EnvType getType() { return type; }
    public CFGLine getStartLine() { return startLine; }
    public CFGLine getFollowingLine() { return followingLine; }
    public void setStartLine(CFGLine start) { startLine = start; }
    public void setFollowingLine(CFGLine follow) { followingLine = follow; }

}
