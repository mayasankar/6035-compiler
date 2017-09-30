package edu.mit.compilers.ir.expression.literal;

import java.util.ArrayList;
import java.util.List;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public class IRBoolLiteral extends IRLiteral<Boolean> {

	public IRBoolLiteral(Boolean value) {
		super(value);
	}

	@Override
	public IRType getType() {
		return IRType.boolType();
	}

	@Override
	public List<IRNode> getChildren() {
		return new ArrayList<IRNode>();
	}
}
