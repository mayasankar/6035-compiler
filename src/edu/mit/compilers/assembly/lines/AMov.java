package edu.mit.compilers.assembly.lines;

public class AMov extends AssemblyLine {

    private String rreg;
    private String lreg;

    public AMov(String lreg, String rreg) {
        this.lreg = lreg;
        this.rreg = rreg;
    }

    public Boolean leftIsRegister() { return lreg.startsWith("%"); }
    public Boolean rightIsRegister() { return rreg.startsWith("%"); }
    public Boolean getLeft() { return lreg; }
    public Boolean getRight() { return rreg; }

    @Override
    public String getString() {
        return "mov " + lreg + ", " + rreg + "\n";
    }

}
