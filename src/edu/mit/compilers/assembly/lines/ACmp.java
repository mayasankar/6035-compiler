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
        if (rreg.charAt(0) == '$') { // TODO hack
            String freeRegister = "%r11";
            return "mov" + rreg + ", " + freeRegister + "\n" +
                "cmp " + lreg + ", " + freeRegister + "\n";
        }
        return "cmp " + lreg + ", " + rreg + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
