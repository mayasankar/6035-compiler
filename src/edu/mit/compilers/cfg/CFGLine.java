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

// todo list
// multi-line, insert here


public abstract class CFGLine {
    private CFGLine trueBranch;
    private CFGLine falseBranch;

    protected CFGLine(CFGLine trueBranch, CFGLine falseBranch) {
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public CFGLine getTrueBranch() {
        return trueBranch;
    }

    public CFGLine getFalseBranch() {
        return falseBranch;
    }

    public boolean isBranch() {
        throw new RuntimeException("Not implemented yet.");
    }

    public void setNext(CFGLine next) {
        throw new RuntimeException("Not implemented yet.");
    }

    public boolean isEnd() {
        // are branches null?
    }

}
