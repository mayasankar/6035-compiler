package edu.mit.compilers.assembly.lines;

public class AssemblyLineFactory {
    public AssemblyLine makeLine(String operation, String... args) {
        switch(operation) {
        case "add":
        case "sub":
        case "imul":
            if(args.length != 2) {
                throw new RuntimeException(operation + " takes 2 arguments");
            }
            return new AOps(operation, args[0], args[1]);
        default:
            throw new RuntimeException("The operation " + operation + " was not recognized");
        }

    }
}
