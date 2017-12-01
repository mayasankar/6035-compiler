package edu.mit.compilers.assembly.lines;

public class AUnaryOp extends AssemblyLine {

    private String reg;

    // neg, idiv
    public AUnaryOp(String operation, String reg) {
        command = operation;
        this.reg = reg;
    }

    @Override
    public String getString() {
        return command + " " + reg + "\n";
    }

}
