okay so this I think is the plan for what classes need to exist for symbol tables:

ProgramDescriptor - top level objects / global fields, imports (IMPORTS: ClassDescriptor)

Descriptor (abstract) - name
ClassDescriptor - parent, fields, methods (IMPORTS: Descriptor, MethodTable, VariableTable)
TypeDescriptor - name. not sure what else this should do. (IMPORTS: Descriptor)
MethodDescriptor - return type, locals, code pointer (IMPORTS: Descriptor, TypeDescriptor, VariableTable)

SymbolTable (abstract) - parent, map strings to descriptors (IMPORTS: Descriptor)
TypeTable - parent, map strings to primitive types, arrays, and classes defined in the program (IMPORTS: SymbolTable)
MethodTable - parent, map strings to methods defined in the program (IMPORTS: SymbolTable)

VariableTable - pointer to parent, vars within the scope point to their descriptors (IMPORTS: Variable)
Variable - type, value
