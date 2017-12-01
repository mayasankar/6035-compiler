package edu.mit.compilers.assembly.lines;

public class ALabel extends AssemblyLine {

    private String label;

    public ALabel(String label) {
        this.label = label;
    }

    @Override
    public String getString() {
        return label + ":\n";
    }

}
