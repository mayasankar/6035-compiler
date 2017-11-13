// package edu.mit.compilers.cfg;
//
// import edu.mit.compilers.ir.*;
//
// public class IRToCFGVisitor<R> implements CFGLine.CFGVisitor<R> {
//     private IRNode.IRNodeVisitor<R> irVisitor;
//     R trivialAnswer;
//
//     public IRToCFGVisitor(IRNode.IRNodeVisitor<R> visitor, R ta) {
//         irVisitor = visitor;
//         trivialAnswer = ta;
//     }
//
// 	public R on(CFGBlock line) {
//         throw new RuntimeException("IRToCFGVisitor should never be called on a CFGBlock.");
//     }
//
// 	public R on(CFGStatement line) {
//         return line.getStatement().accept(irVisitor);
//     }
// 	public R on(CFGExpression line) {
//         return line.getExpression().accept(irVisitor);
//     }
// 	public R on(CFGDecl line) {
//         return line.getDecl().accept(irVisitor);
//     }
// 	public R on(CFGMethodDecl line) {
//         return line.getMethodDecl().accept(irVisitor);
//     }
// 	public R on(CFGNoOp line) {
//         return trivialAnswer;
//     }
// 	public R on(CFGAssignStatement line) {
//         return line.getStatement().accept(irVisitor);
//     }
// }
