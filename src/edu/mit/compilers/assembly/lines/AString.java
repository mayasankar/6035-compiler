package edu.mit.compilers.assembly.lines;

public class AString extends AssemblyLine {

    private String string;

    public AString(String string) {
        this.string = string;
    }

    @Override
    public String getString() {
        return ".string " + string + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
