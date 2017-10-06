package edu.mit.compilers.ir;

import java.util.ArrayList;
import java.util.List;

import antlr.Token;

import edu.mit.compilers.grammar.DecafParserTokenTypes;

public class IRType extends IRNode {
	public enum Type {
		UNSPECIFIED,
		INT,
		BOOL,
		INT_ARRAY,
		BOOL_ARRAY,
		VOID
	}

	public static Type getType(Token t) {
		switch(t.getType()) {
			case DecafParserTokenTypes.TK_int: {
				return Type.INT;
			}
			case DecafParserTokenTypes.TK_bool: {
				return Type.BOOL;
			}
			case DecafParserTokenTypes.TK_void: {
				return Type.VOID;
			}
		}
		return Type.UNSPECIFIED;
	}

	public static Type getType(Token t, int al) {
		switch(t.getType()) {
			case DecafParserTokenTypes.TK_int: {
				return Type.INT_ARRAY;
			}
			case DecafParserTokenTypes.TK_bool: {
				return Type.BOOL_ARRAY;
			}
		}
		return Type.UNSPECIFIED;
	}

	private Type type = Type.UNSPECIFIED;
	private int arrayLength = -1;

	// public IRType(IRType.Type type) {
	// 	this.type = type;
	// }
	//
	// public IRType(Token t) {
	// 	switch(t.getType()) {
	// 		case DecafParserTokenTypes.TK_int: {
	// 			type = Type.INT; break;
	// 		}
	// 		case DecafParserTokenTypes.TK_bool: {
	// 			type = Type.BOOL; break;
	// 		}
	// 		case DecafParserTokenTypes.TK_void: {
	// 			type = Type.VOID; break;
	// 		}
	// 	}
	// }
	//
	// public IRType(Token t, int al) {
	// 	arrayLength = al;
	// 	switch(t.getType()) {
	// 		case DecafParserTokenTypes.TK_int: {
	// 			type = Type.INT_ARRAY; break;
	// 		}
	// 		case DecafParserTokenTypes.TK_bool: {
	// 			type = Type.BOOL_ARRAY; break;
	// 		}
	// 	}
	// }

	public boolean isArray() {
		return type == Type.INT_ARRAY || type == Type.BOOL_ARRAY;
	}

	public boolean isVoid() {
		return type == Type.VOID;
	}

	// @Override
	// public List<? extends IRNode> getChildren() {
	// 	return new ArrayList<IRNode>();
	// }

	// // TODO: int and bool need type descriptors
	// public static IRType intType() {
	// 	return new IRType(new Token());
	// }
	//
	// public static IRType boolType() {
	// 	return new IRType(new Token());
	// }

	@Override
	public String toString() {
		return type.name() + (isArray() ? arrayLength : "");
	}

}
