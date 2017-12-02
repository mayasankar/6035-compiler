package edu.mit.compilers.assembly.lines;

public class ACmp extends AssemblyLine {

    private String rreg;
    private String lreg;

    public ACmp(String lreg, String rreg) {
        this.lreg = lreg;
        this.rreg = rreg;
    }

    public String getLeft() { return lreg; }
    public String getRight() { return rreg; }

    @Override
    public String getString() {
        return "cmp " + lreg + ", " + rreg + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
