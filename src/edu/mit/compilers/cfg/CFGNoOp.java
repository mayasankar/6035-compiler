package edu.mit.compilers.cfg;
import edu.mit.compilers.cfg.CFGLine;
import java.util.BitSet;

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

    @Override
    public <R> R accept(CFGBitSetVisitor<R> visitor, BitSet parentBitVector){
		return visitor.on(this, parentBitVector);
	}
}
