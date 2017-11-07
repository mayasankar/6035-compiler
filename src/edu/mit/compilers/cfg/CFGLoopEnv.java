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
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;
import edu.mit.compilers.cfg.*;


public class CFGLoopEnv {

    CFGLine startLine;  // where we go to after continue
    CFGLine followingLine;  // where we go to after break

    public CFGLoopEnv(CFGLine start, CFGLine end) {
        startLine = start;
        followingLine = end;
    }

    public CFGLine getStartLine() { return startLine; }
    public CFGLine getFollowingLine() { return followingLine; }
    public void setStartLine(CFGLine start) { startLine = start; }
    public void setFollowingLine(CFGLine follow) { followingLine = follow; }

}
