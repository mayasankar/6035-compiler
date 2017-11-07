package edu.mit.compilers.ir.decl;

import java.util.Arrays;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.ir.IRNode;
import edu.mit.compilers.ir.IRType;
import edu.mit.compilers.ir.IRNode.IRNodeVisitor;
import edu.mit.compilers.symbol_tables.Named;
import edu.mit.compilers.symbol_tables.TypeDescriptor;

public abstract class IRMemberDecl extends IRNode implements Named {

	protected Token id;
	protected TypeDescriptor irType;
	protected int length;

	@Override
	public List<? extends IRNode> getChildren() {
		return Arrays.asList();
	}

	public IRMemberDecl(TypeDescriptor irType, Token id) {
		setLineNumbers(id);
		this.id = id;
		this.irType = irType;
		this.length = 0;
	}

	public IRMemberDecl(TypeDescriptor irType, Token id, int length) {
		setLineNumbers(id);
		this.id = id;
		this.irType = irType;
		this.length = length;
	}

	public TypeDescriptor getType() { return irType; }

	public boolean isArray() { // could also be implemented by return length > 0
		return irType.isArray();
	}

	public int getLength() { return length; }

	public int getSpaceRequired() { return 8 * (isArray() ? length : 1); }

	public String getName() { return id.getText(); }
	


}
