package edu.mit.compilers.assembly.lines;

public class AMov extends AssemblyLine {

    private String right;
    private String left;

    public AMov(String left, String right) {
        this.left = left;
        this.right = right;
    }

    public Boolean leftIsRegister() { return left.startsWith("%"); }
    public Boolean rightIsRegister() { return right.startsWith("%"); }
    public String getLeft() { return left; }
    public String getRight() { return right; }

    @Override
    public String getString() {
        return "mov " + left + ", " + right + "\n";
    }

}
