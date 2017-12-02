package edu.mit.compilers.assembly.lines;

public class AShift extends AssemblyLine {

    private String reg;

    // shl, shr
    public AShift(String operation, String reg) {
        command = operation;
        assert command.equals("shl") || command.equals("shr");
        this.reg = reg;
    }

    @Override
    public String getString() {
        return command + " %cl, " + reg + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
