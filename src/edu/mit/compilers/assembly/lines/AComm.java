package edu.mit.compilers.assembly.lines;

public class AComm extends AssemblyLine {

    private String name;
    private String size;

    public AComm(String name, String size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getString() {
        return ".comm " + name + ", " + size + ", 8\n";
    }

}
