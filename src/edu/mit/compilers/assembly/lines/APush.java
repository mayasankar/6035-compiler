package edu.mit.compilers.assembly.lines;

public class APush extends AssemblyLine {

    private String reg;

    public APush(String reg) {
        assert reg.startsWith("%");
        this.reg = reg;
    }

    @Override
    public String getString() {
        return "push " + reg + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
