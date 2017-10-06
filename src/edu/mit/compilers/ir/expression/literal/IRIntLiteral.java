package edu.mit.compilers.ir.expression.literal;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public class IRIntLiteral extends IRLiteral<Integer> {

	public IRIntLiteral(Integer value) {
		super(value);
	}

	@Override
	public IRType.Type getType() {
		return IRType.Type.INT;
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return new ArrayList<IRNode>();
	}
}
