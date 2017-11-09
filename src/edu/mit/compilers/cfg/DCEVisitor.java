package edu.mit.compilers.cfg;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
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

public class DCEVisitor implements CFGLine.CFGBitSetVisitor<Boolean> {
    // boolean returns true if we changed something, false if not

	@Override
    public Boolean on(CFGBlock line, BitSet parentBitVector){
        // TODO should this do anything other than nothing? I think we never call it on this
        return false;
    }

    @Override
    public Boolean on(CFGStatement line, BitSet parentBitVector){
        // USE + (U parent - DEF)
        BitSet original = line.getBitvectorDCE();
        BitSet bitvector = original.get(0, original.size());
        bitvector.or(parentBitVector);
        IRStatement statement = line.getStatement();
        // TODO get USE and DEF out of the statement
        line.setBitvectorDCE(bitvector);
        return !bitvector.equals(original);
    }

    @Override
    public Boolean on(CFGExpression line, BitSet parentBitVector){
        throw new RuntimeException("Unimplemented.");
    }

    @Override
    public Boolean on(CFGDecl line, BitSet parentBitVector){
        throw new RuntimeException("Unimplemented.");
    }

    @Override
    public Boolean on(CFGMethodDecl line, BitSet parentBitVector){
        throw new RuntimeException("Unimplemented.");
    }

    @Override
    public Boolean on(CFGNoOp line, BitSet parentBitVector){
        BitSet original = line.getBitvectorDCE();
        BitSet bitvector = original.get(0, original.size()); // make a copy
        bitvector.or(parentBitVector);
        line.setBitvectorDCE(bitvector);
        return !bitvector.equals(original);
    }

    @Override
    public Boolean on(CFGAssignStatement line, BitSet parentBitVector){
        throw new RuntimeException("Unimplemented.");
    }
}
