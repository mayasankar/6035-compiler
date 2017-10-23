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


public abstract class CFGLine {
    protected CFGLine trueBranch;
    protected CFGLine falseBranch;
    protected int numParentLines;
    protected CFGBlock correspondingBlock;
    protected CFGEnv.EnvType addEnvType; // null if this isn't the start of a new scope, type of scope if it is
    protected int numEnvsEnded; // 0 if this isn't the end of a scope, number of scopes ended if it is

    protected CFGLine(CFGLine trueBranch, CFGLine falseBranch) {
        this.trueBranch = trueBranch;
        trueBranch.addParentLine();
        this.falseBranch = falseBranch;
        falseBranch.addParentLine();
        this.numParentLines = 0;
        this.correspondingBlock = null;
        this.addEnvType = null;
        this.numEnvsEnded = 0;
    }

    protected CFGLine(CFGLine next) {
        this.trueBranch = next;
        this.falseBranch = next;
        this.numParentLines = 0;
        this.correspondingBlock = null;
        this.addEnvType = null;
        this.numEnvsEnded = 0;
    }

    protected CFGLine() {
        this.trueBranch = null;
        this.falseBranch = null;
        this.numParentLines = 0;
        this.correspondingBlock = null;
        this.addEnvType = null;
        this.numEnvsEnded = 0;
    }

    public void startEnv(CFGEnv.EnvType t) {
        addEnvType = t;
    }

    public void endEnv() {
        numEnvsEnded += 1;
    }

    public CFGEnv.EnvType getEnvType() {
        return addEnvType;
    }

    public int getNumEnvsEnded() {
        return numEnvsEnded;
    }

    public CFGBlock getCorrespondingBlock() {
        return correspondingBlock;
    }

    public void setCorrespondingBlock(CFGBlock block) {
        correspondingBlock = block;
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

    public boolean isMerge() {
        return numParentLines > 1;
    }

    public void setNext(CFGLine next) {
        this.trueBranch = next;
        this.falseBranch = next;
        next.addParentLine();
    }

    public void addParentLine() {
        this.numParentLines += 1;
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
        else if (falseBranch == null) {
            throw new RuntimeException("CFGLine has one null branch and one non-null branch.");
        }
        return false;
    }

    public boolean isNoOp() {
        throw new RuntimeException("Must be overridden by child class of CFGLine.");
    }

    @Override
    public String toString() {
        return stringHelper(0, 20);
    }

    public String stringHelper(int numIndents, int depthLimit) {
        if (depthLimit <= 0) {
            return "";
        }
        String prefix = "";
        for (int i=0; i<numIndents; i++){
            prefix += "-";
        }
        String str = prefix + ownValue() + "\n";
        if (isBranch()) {
            if (trueBranch == null || falseBranch == null) {
                throw new RuntimeException("Branches should not be null: " + this.ownValue());
            }
            str += trueBranch.stringHelper(numIndents+1, depthLimit-1);
            str += falseBranch.stringHelper(numIndents+1, depthLimit-1);
        }
        else if (trueBranch != null) {
            str += trueBranch.stringHelper(numIndents, depthLimit-1);
        }
        return str;
    }

    public String ownValue() {
        return "<CFGLine Object>";
    }

    public String getLabel(){
        // TODO: what should the label of this line in the code be, if needed?
        return new Integer(this.hashCode()).toString();
    }

}
