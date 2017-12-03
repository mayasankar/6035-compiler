package edu.mit.compilers.assembly.lines;

public class APop extends AssemblyLine {

    private String reg;

    public APop(String reg) {
        assert reg.startsWith("%");
        this.reg = reg;
    }

    public String getReg() { return reg; }
    public void setReg(String reg) {
        assert reg.startsWith("%");
        this.reg = reg;
    }

    @Override
    public String getString() {
        return "pop " + reg + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
