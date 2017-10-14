package edu.mit.compilers.cfg;
import java.util.ArrayList;
import java.util.List;
import edu.mit.compilers.cfg.CFGLine;

public class CFGBlock extends CFGLine {
    private List<CFGLine> lines;

    public CFGBlock(CFGLine trueBranch, CFGLine falseBranch) {
        super(trueBranch, falseBranch);
        this.lines = new ArrayList<CFGLine>();
    }

    public CFGBlock () {
        super();
        this.lines = new ArrayList<CFGLine>();
    }

    public List<CFGLine> getLines() { return lines; }

    public void setTrueBranch(CFGLine next) {
        this.trueBranch = next;
        // doesn't increment parent counter because CFGBlock just copies the underlying structure of lines
    }

    public void setFalseBranch(CFGLine next) {
        this.falseBranch = next;
        // doesn't increment parent counter because CFGBlock just copies the underlying structure of lines
    }

    public void addLine(CFGLine l) { lines.add(l); }

}
