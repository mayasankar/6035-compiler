package edu.mit.compilers.cfg;
import java.util.ArrayList;
import java.util.List;
import edu.mit.compilers.cfg.CFGLine;
import java.util.Set;

public class CFGBlock extends CFGLine {
    private List<CFGLine> lines;

    public CFGBlock(CFGBlock trueBranch, CFGBlock falseBranch) {
        super(trueBranch, falseBranch);
        this.lines = new ArrayList<CFGLine>();
    }

    public CFGBlock () {
        super();
        this.lines = new ArrayList<CFGLine>();
    }

    public List<CFGLine> getLines() { return lines; }

    public void setTrueBranch(CFGBlock next) {
        this.trueBranch = next;
        // doesn't increment parent counter because CFGBlock just copies the underlying structure of lines
    }

    public void setFalseBranch(CFGBlock next) {
        this.falseBranch = next;
        // doesn't increment parent counter because CFGBlock just copies the underlying structure of lines
    }

    public void addLine(CFGLine l) { lines.add(l); }

    @Override
    public <R> R accept(CFGVisitor<R> visitor){
		return visitor.on(this);
	}

}
