package edu.mit.compilers.cfg;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.BitSet;
import java.math.BigInteger;

import antlr.Token;
import edu.mit.compilers.ir.*;
import edu.mit.compilers.ir.decl.*;
import edu.mit.compilers.ir.expression.*;
import edu.mit.compilers.ir.expression.literal.*;
import edu.mit.compilers.ir.statement.*;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.trees.EnvStack;


public abstract class CFGLine {
    protected CFGLine trueBranch;
    protected CFGLine falseBranch;
    protected List<CFGLine> parents;
    protected CFGBlock correspondingBlock;
    protected Set<String> setDCE;

    protected CFGLine(CFGLine trueBranch, CFGLine falseBranch) {
        this.trueBranch = trueBranch;
        trueBranch.addParentLine(this);
        this.falseBranch = falseBranch;
        falseBranch.addParentLine(this);
        this.parents = new ArrayList<>();
        this.correspondingBlock = null;
        this.setDCE = new HashSet<>();
    }

    protected CFGLine(CFGLine next) {
        this.trueBranch = next;
        this.falseBranch = next;
        next.addParentLine(this);
        this.parents = new ArrayList<>();
        this.correspondingBlock = null;
        this.setDCE = new HashSet<>();
    }

    protected CFGLine() {
        this.trueBranch = null;
        this.falseBranch = null;
        this.parents = new ArrayList<>();
        this.correspondingBlock = null;
        this.setDCE = new HashSet<>();
    }

    public abstract <R> R accept(CFGBitSetVisitor<R> visitor, Set<String> parentSet);

    public interface CFGBitSetVisitor<R> {
		public R on(CFGBlock line, Set<String> parentSet);
		public R on(CFGStatement line, Set<String> parentSet);
		public R on(CFGExpression line, Set<String> parentSet);
		public R on(CFGDecl line, Set<String> parentSet);
		public R on(CFGMethodDecl line, Set<String> parentSet);
		public R on(CFGNoOp line, Set<String> parentSet);
		public R on(CFGAssignStatement line, Set<String> parentSet);
	}

    public CFGBlock getCorrespondingBlock() {
        return correspondingBlock;
    }

    public void setCorrespondingBlock(CFGBlock block) {
        correspondingBlock = block;
    }

    public List<CFGLine> getParents() {
        return parents;
    }

    public Set getSetDCE() {
        return setDCE;
    }

    public void setSetDCE(Set newSet) {
        setDCE = newSet;
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
        return parents.size() > 1;
    }

    public void setNext(CFGLine next) {
        this.trueBranch = next;
        this.falseBranch = next;
        next.addParentLine(this);
    }

    public void addParentLine(CFGLine parent) {
        this.parents.add(parent);
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
