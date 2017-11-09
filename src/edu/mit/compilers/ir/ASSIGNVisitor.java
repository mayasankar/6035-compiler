package edu.mit.compilers.ir;

import java.util.List;
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
import edu.mit.compilers.cfg.*;

// return a set of used variables in any IRNode
public class ASSIGNVisitor implements IRNode.IRNodeVisitor<Set<String>> {

    @Override
    public Set<String> on(IRProgram ir){
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public Set<String> on(IRFieldDecl ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRLocalDecl ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRParameterDecl ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRMethodDecl ir){
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public Set<String> on(IRUnaryOpExpression ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRBinaryOpExpression ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRTernaryOpExpression ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRLenExpression ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRVariableExpression ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRMethodCallExpression ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> onBool(IRLiteral<Boolean> ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> onString(IRLiteral<String> ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> onInt(IRLiteral<BigInteger> ir){
        throw new RuntimeException("Unimplemented");
    }

    @Override
    public Set<String> on(IRAssignStatement ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRBlock ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRForStatement ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRIfStatement ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRLoopStatement ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRMethodCallStatement ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRReturnStatement ir){
        throw new RuntimeException("Unimplemented");
    }
    @Override
    public Set<String> on(IRWhileStatement ir){
        throw new RuntimeException("Unimplemented");
    }

}
