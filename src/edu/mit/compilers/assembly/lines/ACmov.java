package edu.mit.compilers.assembly.lines;

public class ACmov extends AssemblyLine {

    private String right;
    private String left;
    private String command;

    public ACmov(String command, String left, String right) {
        assert command.startsWith("cmov");
        this.command = command;
        this.left = left;
        this.right = right;
    }

    public Boolean leftIsRegister() { return left.startsWith("%"); }
    public Boolean rightIsRegister() { return right.startsWith("%"); }
    public String getLeft() { return left; }
    public String getRight() { return right; }

    @Override
    public String getString() {
        return command + " " + left + ", " + right + "\n";
    }

}
