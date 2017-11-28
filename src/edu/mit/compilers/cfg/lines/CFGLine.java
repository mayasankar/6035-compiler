package edu.mit.compilers.cfg.lines;

import java.util.List;
import java.util.Arrays;
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
import edu.mit.compilers.cfg.*;


public abstract class CFGLine {
    protected CFGLine trueBranch;
    protected CFGLine falseBranch;
    protected List<CFGLine> parents;
    protected CFGBlock correspondingBlock;

    // for CSE
    protected Map<IRExpression, Set<String>> availableExpressionsIN = new HashMap<>();
    protected Map<IRExpression, Set<String>> availableExpressionsOUT = new HashMap<>();
    // for DCE
    protected Set<String> livenessIN = new HashSet<>();
    protected Set<String> livenessOUT = new HashSet<>();
    // for CP
    protected Map<String, Set<IRExpression>> reachingDefinitionsIN = new HashMap<>();
    protected Map<String, Set<IRExpression>> reachingDefinitionsOUT = new HashMap<>();

    protected CFGLine(CFGLine trueBranch, CFGLine falseBranch) {
        this.trueBranch = trueBranch;
        trueBranch.addParentLine(this);
        this.falseBranch = falseBranch;
        falseBranch.addParentLine(this);
        this.parents = new ArrayList<>();
        this.correspondingBlock = null;
    }

    protected CFGLine(CFGLine next) {
        this.trueBranch = next;
        this.falseBranch = next;
        next.addParentLine(this);
        this.parents = new ArrayList<>();
        this.correspondingBlock = null;
    }

    protected CFGLine() {
        this.trueBranch = null;
        this.falseBranch = null;
        this.parents = new ArrayList<>();
        this.correspondingBlock = null;
    }

    public abstract <R> R accept(CFGVisitor<R> visitor);

    public interface CFGVisitor<R> {
    	public R on(CFGAssignStatement line);
        public R on(CFGBoundsCheck line);
    	public R on(CFGConditional line);
    	public R on(CFGNoOp line);
    	public R on(CFGReturn line);
    	public R on(CFGMethodCall line);
        public R on(CFGBlock line);
    }

    public CFGBlock getCorrespondingBlock() {
        return correspondingBlock;
    }

    public void setCorrespondingBlock(CFGBlock block) {
        this.correspondingBlock = block;
    }

    public List<CFGLine> getParents() {
        return parents;
    }

    public List<CFGLine> getChildren() {
        if (isEnd()) {
            return Arrays.asList();
        } else if (isBranch()) {
            return Arrays.asList(trueBranch, falseBranch);
        } else {
            return Arrays.asList(trueBranch);
        }
    }

    public void removeParent(CFGLine parent) {
        this.parents.remove(parent);
    }

    public Map<IRExpression, Set<String>> getAvailableExpressionsIn() { return this.availableExpressionsIN; }
    public void setAvailableExpressionsIn(Map<IRExpression, Set<String>> newSet) { this.availableExpressionsIN = newSet; }
    public Map<IRExpression, Set<String>> getAvailableExpressionsOut() { return this.availableExpressionsOUT; }
    public void setAvailableExpressionsOut(Map<IRExpression, Set<String>> newSet) { this.availableExpressionsOUT = newSet; }
    public Set<String> getLivenessIn() { return this.livenessIN; }
    public void setLivenessIn(Set<String> newSet) { this.livenessIN = newSet; }
    public Set<String> getLivenessOut() { return this.livenessOUT; }
    public void setLivenessOut(Set<String> newSet) { this.livenessOUT = newSet; }
    public Map<String, Set<IRExpression>> getReachingDefinitionsIn() { return this.reachingDefinitionsIN; }
    public void setReachingDefinitionsIn(Map<String, Set<IRExpression>> newSet) { this.reachingDefinitionsIN = newSet; }
    public Map<String, Set<IRExpression>> getReachingDefinitionsOut() { return this.reachingDefinitionsOUT; }
    public void setReachingDefinitionsOut(Map<String, Set<IRExpression>> newSet) { this.reachingDefinitionsOUT = newSet; }

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

    // the following two functions are for use in CFG.remove(CFGLine).

    // NOTE: we assume the oldChild is getting removed and thus we don't have to go fix its parents list
    public void replaceChildren(CFGLine oldChild, CFGLine newChild){
        if (this == oldChild) {
            // throw an exception? this can happen but it would be nice to know
            return;
        }
        if (this.trueBranch == oldChild) {
            this.trueBranch = newChild;
            newChild.addParentLine(this);
        }
        if (this.falseBranch == oldChild) {
            this.falseBranch = newChild;
            if (this.isBranch()){
                newChild.addParentLine(this);
            }
        }
    }

    // the children of this are set to the children of other, except loops other->other become loops this->this.
    public void stealChildren(CFGLine other) {
        if (other.isEnd()) {
            // these steps are probably unnecessary.
            trueBranch = null;
            falseBranch = null;
        } else if (other.isBranch()) {
            this.trueBranch = other.trueBranch == other ? this : other.trueBranch;
            trueBranch.addParentLine(this);
            this.falseBranch = other.falseBranch == other ? this : other.falseBranch;
            if (trueBranch != falseBranch) {
                falseBranch.addParentLine(this);
            }
        } else {
            setNext(other.trueBranch == other ? this : other.trueBranch);
        }
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
    public abstract boolean isAssign();

    @Override
    public String toString() {
        return stringHelper(0, 40, new HashSet<CFGLine>());
    }

    // NOTE: it is bad at printing things that will infinite loop
    public String stringHelper(int numIndents, int depthLimit, Set<CFGLine> expandedLines) {
        if (depthLimit <= 0) {
            return "";
        }
        String prefix = "";
        for (int i=0; i<numIndents; i++){
            prefix += "-";
        }
        String str = prefix + ownValue() + /*"\n\tIN: " + reachingDefinitionsIN + "\n\tOUT: " + reachingDefinitionsOUT + */"\n";
        if (isBranch()) {
            if (trueBranch == null || falseBranch == null) {
                throw new RuntimeException("Branches should not be null: " + this.ownValue());
            }
            if(!expandedLines.contains(trueBranch)) {
            	expandedLines.add(trueBranch);
            	str += trueBranch.stringHelper(numIndents+1, depthLimit-1, expandedLines);
            } else {
				str += "Tried duplicate here\n";
			}
            if(!expandedLines.contains(falseBranch)) {
            	expandedLines.add(falseBranch);
            	str += falseBranch.stringHelper(numIndents+1, depthLimit-1, expandedLines);
            } else {
				str += "Tried duplicate here\n";
			}
        }
        else if (trueBranch != null) {
            // NOTE if you're getting a stack overflow error, your CFG has an
            // infinite loop and you want to change depthLimit to depthLimit-1
            // in this line for debug output
        	if(!expandedLines.contains(trueBranch)) {
            	expandedLines.add(trueBranch);
            	str += trueBranch.stringHelper(numIndents, depthLimit, expandedLines);
            } else {
				str += "Tried duplicate here\n";
			}
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
