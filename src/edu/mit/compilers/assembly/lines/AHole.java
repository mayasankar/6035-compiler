// I'm an A-troll

package edu.mit.compilers.assembly.lines;

public class AHole extends AssemblyLine {

    private String rreg;
    private String lreg;

    public AHole(String lreg, String rreg) {
        this.lreg = lreg;
        this.rreg = rreg;
    }

    @Override
    public String getString() {
        return "hole " + lreg + ", " + rreg + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return null;
    }

}
