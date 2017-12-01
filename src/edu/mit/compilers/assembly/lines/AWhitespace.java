package edu.mit.compilers.assembly.lines;

public class AWhitespace extends AssemblyLine {

    @Override
    public String getString() {
        return "\n";
    }

}
