package edu.mit.compilers.cfg;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.math.BigInteger;

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

// todo list
// multi-line, insert here

public static class CFGCreator {

    public static CFGLine makeNoOp() {
        return new CFGNoOp();
    }

    public static CFG destruct(IRNode ir) {

    }

    // destruct should have private overloaded functions for all the types of IRNode

    private static CFGLine shortcircuit(IRExpr expr, CFGLine trueBranch, CFGLine falseBranch) {

    }

    // probably same here
}
