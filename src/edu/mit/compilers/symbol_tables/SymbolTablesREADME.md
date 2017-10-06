okay so this I think is the plan for what classes need to exist for symbol tables:

ProgramTable - top level objects / global fields, imports (IMPORTS: ClassTable)
ClassTable - parent, fields, methods (IMPORTS: IRFieldDecl, IRMethodDecl, MethodTable, VariableTable)

MethodTable - pointer to parent, map strings to methods defined in the program (IMPORTS: SymbolTable)
VariableTable - pointer to parent, map vars within the scope to their IRDecls (IMPORTS: IRMemberDecl)
