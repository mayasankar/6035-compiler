package edu.mit.compilers.assembly.lines;

public class AOps extends AssemblyLine {

    private String rreg;
    private String lreg;

    public AOps(String operation, String lreg, String rreg) {
        command = operation;
        this.lreg = lreg;
        this.rreg = rreg;
    }

    @Override
    public String getString() {
        return command + " " + lreg + ", " + rreg + "\n";
    }

}
