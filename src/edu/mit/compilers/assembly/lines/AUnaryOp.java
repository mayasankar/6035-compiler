package edu.mit.compilers.assembly.lines;

public class AUnaryOp extends AssemblyLine {

    private String reg;

    // neg, idiv
    public AUnaryOp(String operation, String reg) {
        command = operation;
        this.reg = reg;
    }

    public String getReg() { return this.reg; }
    public void setReg(String reg) { this.reg = reg; }

    @Override
    public String getString() {
        return command + " " + reg + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
