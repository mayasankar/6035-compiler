// package edu.mit.compilers.cfg;
//
// import java.util.List;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.HashSet;
// import java.util.Set;
// import java.math.BigInteger;
//
// import antlr.Token;
// import edu.mit.compilers.ir.*;
// import edu.mit.compilers.ir.decl.*;
// import edu.mit.compilers.ir.expression.*;
// import edu.mit.compilers.ir.expression.literal.*;
// import edu.mit.compilers.ir.statement.*;
// import edu.mit.compilers.symbol_tables.*;
// import edu.mit.compilers.trees.EnvStack;
// import edu.mit.compilers.cfg.lines.*;
//
// // boolean returns true always
// public class DCEVisitor implements CFGLine.CFGVisitor<Boolean> {
//
//     private IRNode.IRNodeVisitor<Set<String>> USE = new USEVisitor();
//     private IRNode.IRNodeVisitor<Set<String>> ASSIGN = new ASSIGNVisitor();
//
//     private Set<String> use;
//     private Set<String> assign;
//
// 	@Override
//     public Boolean on(CFGBlock line){
//         // TODO should this do anything other than nothing? I think we never call it on this
//         //return false;
//         throw new RuntimeException("DCEVisitor should never be called on a CFGBlock.");
//     }
//
//
//     @Override
//     public Boolean on(CFGAssignStatement line){
//         use = line.getExpression().accept(USE);
//         assign = line.getVarAssigned().accept(USE);
//         return true;
//     }
//
//     @Override
//     public Boolean on(CFGConditional line){
//         IRExpression expr = line.getExpression();
//         use = expr.accept(USE);
//         assign = expr.accept(ASSIGN);
//         return true;
//     }
//
//     @Override
//     public Boolean on(CFGMethodCall line){
//         IRExpression expr = line.getExpression();
//         use = expr.accept(USE);
//         assign = expr.accept(ASSIGN);
//         return true;
//     }
//
//     @Override
//     public Boolean on(CFGReturn line){
//         if (line.isVoid()) {
//             Set empty = new HashSet<>();
//             return onHelper(line, empty, empty);
//         }
//         IRExpression expr = line.getExpression();
//         use = expr.accept(USE);
//         assign = expr.accept(ASSIGN);
//         return true;
//     }
//
//     @Override
//     public Boolean on(CFGNoOp line){
//         use = new HashSet<>();
//         assign = new HashSet<>();
//         return true;
//     }
// }
