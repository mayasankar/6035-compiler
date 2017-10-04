Notes on how I refactored Arkadiy's files:
1) I think I'm getting rid of the getChildren method. Keeping IRNode because we can add semantic checks there.
2) I think IRLocalDecl is unnecessary. At present IRFieldDecl is equivalent to IRParameterDecl and IRMemberDecl
   but this might change once we do symbol tables.
3) Adding final to class variables might be necessary; on the other hand, might not be possible.
4) Combined break and continue statements into one IR class, IRLoopStatement
5) Not all of these necessarily need to be public classes within the package?
