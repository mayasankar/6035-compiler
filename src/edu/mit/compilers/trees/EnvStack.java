package edu.mit.compilers.trees;

import java.util.ArrayList;
import edu.mit.compilers.symbol_tables.*;
import edu.mit.compilers.ir.statement.*;


public class EnvStack {

    private ArrayList<MethodTable> methodScopes;
    private ArrayList<VariableTable> variableScopes;
    private ArrayList<TypeDescriptor> returnTypes;
    private ArrayList<IRStatement> loopStack;

    public EnvStack(){
      methodScopes = new ArrayList<>();
      variableScopes = new ArrayList<>();
      returnTypes = new ArrayList<>();
      loopStack = new ArrayList<>();
    }

    public void push(MethodTable m) { methodScopes.add(m); }
    public void push(VariableTable v) { variableScopes.add(v); }
    public void push(TypeDescriptor t) { returnTypes.add(t); }
    public void push(IRStatement s) { loopStack.add(s); }

    public void popMethodTable() { methodScopes.remove(methodScopes.size()-1); }
    public void popVariableTable() { variableScopes.remove(variableScopes.size()-1); }
    public void popReturnType() { returnTypes.remove(returnTypes.size()-1); }
    public void popLoopStatement() { loopStack.remove(loopStack.size() - 1); }

    public MethodTable getMethodTable() { return methodScopes.get(methodScopes.size()-1); }
    public VariableTable getVariableTable() { return variableScopes.get(variableScopes.size()-1); }
    public TypeDescriptor getReturnType() { return returnTypes.get(returnTypes.size()-1); }
    public IRStatement getLoopStatement() { return loopStack.isEmpty() ? null : loopStack.get(loopStack.size()-1); }

}
