package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;

public class CFGNoOp extends CFGLine {

    public CFGNoOp() {
        super();
    }

    @Override
    public boolean isNoOp() { return true; }

    @Override
    public String ownValue() {
        return "NOOP";
    }
}
