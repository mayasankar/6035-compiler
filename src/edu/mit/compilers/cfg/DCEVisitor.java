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

	@Override
    public Boolean on(CFGBlock line, BitSet parentBitVector){
        throw new RuntimeException("Unimplemented.");
    }

    @Override
    public Boolean on(CFGStatement line, BitSet parentBitVector){
        throw new RuntimeException("Unimplemented.");
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
        throw new RuntimeException("Unimplemented.");
    }

    @Override
    public Boolean on(CFGAssignStatement line, BitSet parentBitVector){
        throw new RuntimeException("Unimplemented.");
    }
}
