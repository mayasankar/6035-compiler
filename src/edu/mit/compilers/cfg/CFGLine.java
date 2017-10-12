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

    protected CFGLine(CFGLine next) {
        this.trueBranch = next;
        this.falseBranch = next;
    }

    protected CFGLine() {
        this.trueBranch = null;
        this.falseBranch = null;
    }

    public CFGLine getTrueBranch() {
        return trueBranch;
    }

    public CFGLine getFalseBranch() {
        return falseBranch;
    }

    public boolean isBranch() {
        return (trueBranch != falseBranch);
    }

    public void setNext(CFGLine next) {
        this.trueBranch = next;
        this.falseBranch = next;
    }

    public boolean isEnd() {
        if (trueBranch == null) {
            if (falseBranch != null) {
                throw new RuntimeException("CFGLine has one null branch and one non-null branch.");
            }
            else {
                return true;
            }
        }
        elif (falseBranch == null) {
            throw new RuntimeException("CFGLine has one null branch and one non-null branch.");
        }
        return false;
    }

    public boolean isNoOp() {
        throw new RuntimeException("Must be overridden by child class of CFGLine.");
    }

}
