package edu.mit.compilers.assembly.lines;

public class ACall extends AssemblyLine {

    private String function;

    public ACall(String function) {
        this.function = function;
    }

    @Override
    public String getString() {
        return "call " + function + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
