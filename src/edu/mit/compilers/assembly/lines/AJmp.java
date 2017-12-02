package edu.mit.compilers.assembly.lines;

public class AJmp extends AssemblyLine {

    private String label;
    private String command;  // jmp, je, jne, jl, jg, jle, jge

    public AJmp(String command, String label) {
        assert command.startsWith("j");
        this.command = command;
        this.label = label;
    }

    public Boolean isConditional() { return !this.command.equals("jmp"); }

    @Override
    public String getString() {
        return command + " " + label + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
