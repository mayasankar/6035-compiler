okay so this I think is the plan for what classes need to exist for symbol tables:

ProgramTable - top level objects / global fields, imports (IMPORTS: )

Descriptor (abstract) - name
ClassDescriptor - parent, fields, methods (IMPORTS: Descriptor, MethodTable, VariablesScope)
TypeDescriptor - name. not sure what else this should do. (IMPORTS: Descriptor)
MethodDescriptor - return type, locals, code pointer (IMPORTS: Descriptor, TypeDescriptor)

TypeTable - list of primitive types, arrays, and classes defined in the program
MethodTable - list of methods (also other functions?) defined in the program

VariablesScope - pointer to parent, vars within the scope point to their descriptors (IMPORTS: Variable)
Variable - type, value