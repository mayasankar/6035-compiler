Notes on how I refactored Arkadiy's files:
1) I think I'm getting rid of IRNode because the getChildren method doesn't really seem useful.
2) I think IRLocalDecl is unnecessary. At present IRFieldDecl is equivalent to IRParameterDecl and IRMemberDecl
   but this might change once we do symbol tables.
3) Adding final to class variables might be necessary; on the other hand, might not be possible.
