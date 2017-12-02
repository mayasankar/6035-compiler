package edu.mit.compilers.assembly.lines;

public class ACommand extends AssemblyLine {

    private String command;

    //cqto, .globl main, leave, ret, int $0x80
    public ACommand(String command) {
        this.command = command;
    }

    @Override
    public String getString() {
        return command + "\n";
    }

    @Override
    public <R> R accept(AssemblyLineVisitor<R> visitor) {
        return visitor.on(this);
    }

}
