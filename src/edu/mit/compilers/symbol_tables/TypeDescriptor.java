package edu.mit.compilers.symbol_tables;

import antlr.Token;
import edu.mit.compilers.grammar.DecafParserTokenTypes;

public class TypeDescriptor {
	private final String name;
	
	protected TypeDescriptor(String name) {this.name = name;}
	
	public static final TypeDescriptor INT = new TypeDescriptor("int");
	public static final TypeDescriptor BOOL = new TypeDescriptor("bool");
	public static final TypeDescriptor STRING = new TypeDescriptor("string");
	public static TypeDescriptor array(TypeDescriptor elementType) {
		return new ArrayDescriptor(elementType);
	}
	
	public static final TypeDescriptor VOID = new TypeDescriptor("void");
	public static final TypeDescriptor UNSPECIFIED = new TypeDescriptor("unspecified");
	

	
	public boolean isArray() {
		return false;
	}
	
	/**
	 * 
	 * @return The type of element in this array, or throws an exception if type is not an array
	 */
	public TypeDescriptor getArrayElementType() {
		throw new RuntimeException("This is not an array, but a " + this.toString());
	}
	
	public boolean isVoid() {
		return this == VOID;
	}
	
	public static TypeDescriptor getType(Token t) {
		switch(t.getType()) {
			case DecafParserTokenTypes.TK_int: {
				return INT;
			}
			case DecafParserTokenTypes.TK_bool: {
				return BOOL;
			}
			case DecafParserTokenTypes.TK_void: {
				return VOID;
			}
		}
		return UNSPECIFIED;
	}

	public static TypeDescriptor getType(Token t, int al) {
		switch(t.getType()) {
			case DecafParserTokenTypes.TK_int: {
				return array(INT);
			}
			case DecafParserTokenTypes.TK_bool: {
				return array(BOOL);
			}
		}
		return UNSPECIFIED;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

class ArrayDescriptor extends TypeDescriptor {
	private TypeDescriptor elementType;
	ArrayDescriptor(TypeDescriptor elementType) {
		super("array");
		this.elementType = elementType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ArrayDescriptor)) {
			return false;
		}
		ArrayDescriptor desc = (ArrayDescriptor) obj;
		return this.elementType.equals(desc.elementType);
	}
	
	@Override
	public boolean isArray() {
		return true;
	}
	
	@Override
	public TypeDescriptor getArrayElementType() {
		return elementType;
	}
	
	@Override
	public String toString() {
		return elementType.toString() + "_" + super.toString();
	}
}
