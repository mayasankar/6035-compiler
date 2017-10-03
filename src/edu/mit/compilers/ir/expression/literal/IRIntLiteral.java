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
	public IRType getType() {
		return IRType.intType();
	}

	@Override
	public List<? extends IRNode> getChildren() {
		return new ArrayList<IRNode>();
	}
}
