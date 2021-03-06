package edu.mit.compilers.symbol_tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.compilers.ir.decl.IRMethodDecl;

public class SymbolTable<T extends SymbolTable<T,D>, D extends Named> {
	protected T parent;
	protected List<D> orderedChildren = new ArrayList<>() ; // allows printing in order
	protected Map<String, D> childMap = new HashMap<>(); // lookup Method by string name

	public SymbolTable(T parent){
		this.parent = parent;
	}

	public SymbolTable(){
		this.parent = null;
	}

	public T getParent() {
		return parent;
	}

	public int add(D v){
		int ret = processDescriptor(v);
		childMap.put(v.getName(), v);
		orderedChildren.add(v);
		return ret;
	}

    public void removeIndex(int index) {
        D v = orderedChildren.get(index);
        childMap.remove(v.getName());
        orderedChildren.remove(index);
        // TODO reprocess descriptors?
    }

	// overriden in subclasses; used to increment stack pointer in VariableTable
	protected int processDescriptor(D v) { return 0; }

	public D get(String name){
		if (childMap.containsKey(name)){
			return childMap.get(name);
		}
		if (parent != null) {
			return parent.get(name);
		}
		return null;
	}

	public boolean containsInThisScope(String name) {
		return childMap.containsKey(name);
	}

	public boolean isEmpty() {
		return orderedChildren.size() == 0;
	}

}
