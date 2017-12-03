package edu.mit.compilers.assembly.lines;

public class AOps extends AssemblyLine {

    private String rreg;
    private String lreg;
    private String command;

    public AOps(String operation, String lreg, String rreg) {
        command = operation;
        this.lreg = lreg;
        this.rreg = rreg;
    }

    @Override
    public String getString() {
        return command + " " + lreg + ", " + rreg + "\n";
    }

    public String getLeft() { return lreg; }
    public String getRight() { return rreg; }
    public void setRight(String right) { this.rreg = right; }
    public void setLeft(String left) { this.lreg = left; }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
