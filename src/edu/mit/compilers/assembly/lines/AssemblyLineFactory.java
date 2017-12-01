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
        default:
            throw new RuntimeException("The operation " + operation + " was not recognized.");
        }

    }
}
