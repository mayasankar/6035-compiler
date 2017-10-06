package edu.mit.compilers.trees;

import java.util.ArrayList;
import edu.mit.compilers.symbol_tables.*;


public class EnvStack {

    private ArrayList<MethodTable> methodScopes;
    private ArrayList<VariableTable> variableScopes;

    public EnvStack(){
      methodScopes = new ArrayList<>();
      variableScopes = new ArrayList<>();
    }

    public void push(MethodTable m) { methodScopes.add(m); }
    public void push(VariableTable v) { variableScopes.add(v); }

    public void popMethods() { methodScopes.remove(methodScopes.size()-1); }
    public void popVariables() { variableScopes.remove(variableScopes.size()-1); }

    public MethodTable getMethods() { return methodScopes.get(methodScopes.size()-1); }
    public VariableTable getVariables() { return variableScopes.get(variableScopes.size()-1); }
}
