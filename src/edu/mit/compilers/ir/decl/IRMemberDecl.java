package edu.mit.compilers.ir.decl;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;

public abstract class IRMemberDecl extends IRNode {

	protected Token id;
	protected IRType.Type irType;
	protected int length;

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}

	public IRMemberDecl(IRType.Type irType, Token id) {
		setLineNumbers(id);
		this.id = id;
		this.irType = irType;
		this.length = 0;
	}

	public IRMemberDecl(IRType.Type irType, Token id, int length) {
		setLineNumbers(id);
		this.id = id;
		this.irType = irType;
		this.length = length;
	}

	public IRType.Type getType() { return irType; }

	public int getLength() { return length; }

	public String getName() { return id.getText(); }

}
