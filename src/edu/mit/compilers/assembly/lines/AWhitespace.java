package edu.mit.compilers.assembly.lines;

public class AWhitespace extends AssemblyLine {

    @Override
    public String getString() {
        return "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
