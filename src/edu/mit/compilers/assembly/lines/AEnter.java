package edu.mit.compilers.assembly.lines;

public class AEnter extends AssemblyLine {

    private String size;

    public AEnter(String size) {
        this.size = size;
    }

    @Override
    public String getString() {
        return "enter $" + size + ", $0\n";
    }

}
