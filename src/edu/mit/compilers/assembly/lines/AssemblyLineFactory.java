package edu.mit.compilers.assembly.lines;

public class AssemblyLineFactory {
    public AssemblyLine makeLine(String operation, String... args) {
        switch(operation) {
        case "add":
        case "sub":
        case "and":
        case "or":
        case "imul":
            if(args.length != 2) {
                throw new RuntimeException(operation + " takes 2 arguments.");
            }
            return new AOps(operation, args[0], args[1]);
        case "mov":
            if(args.length != 2) {
                throw new RuntimeException(operation + " takes 2 arguments.");
            }
            return new AMov(args[0], args[1]);
        case "cmove":
        case "cmovne":
        case "cmovl":
        case "cmovle":
        case "cmovg":
        case "cmovge":
            if(args.length != 2) {
                throw new RuntimeException(operation + " takes 2 arguments.");
            }
            return new ACmov(operation, args[0], args[1]);
        case "neg":
        case "idiv":
            if(args.length != 1) {
                throw new RuntimeException(operation + " takes 1 argument.");
            }
            return new AUnaryOp(operation, args[0]);
        case "jmp":
        case "je":
        case "jne":
        case "jl":
        case "jg":
        case "jle":
        case "jge":
            if(args.length != 1) {
                throw new RuntimeException(operation + " takes 1 argument.");
            }
            return new AJmp(operation, args[0]);
        case "cqto":
        case ".globl main":
        case "leave":
        case "ret":
        case "int $0x80":
            if(args.length != 0) {
                throw new RuntimeException(operation + " takes 0 arguments.");
            }
            return new ACommand(operation);
        case ".string":
            if(args.length != 1) {
                throw new RuntimeException(operation + " takes 1 argument.");
            }
            return new AString(args[0]);
        case "call":
            if(args.length != 1) {
                throw new RuntimeException(operation + " takes 1 argument.");
            }
            return new ACall(args[0]);
        case "enter":
            if(args.length != 1) {
                throw new RuntimeException(operation + " takes 1 argument.");
            }
            return new AEnter(args[0]);
        case ".comm":
            if(args.length != 2) {
                throw new RuntimeException(operation + " takes 2 arguments.");
            }
            return new AComm(args[0], args[1]);
        default:
            throw new RuntimeException("The operation " + operation + " was not recognized.");
        }

    }
}
