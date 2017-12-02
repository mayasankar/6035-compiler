package edu.mit.compilers.assembly.lines;

public abstract class AssemblyLine {

    protected String command;

    public abstract String getString();

    public interface AssemblyLineVisitor<R>{
        public R on(ACall line);
        public R on(ACmov line);
        public R on(ACmp line);
        public R on(AComm line);
        public R on(ACommand line);
        public R on(AEnter line);
        public R on(AJmp line);
        public R on(ALabel line);
        public R on(AMov line);
        public R on(AOps line);
        public R on(APop line);
        public R on(APush line);
        public R on(AShift line);
        public R on(AString line);
        public R on(ATrivial line);
        public R on(AUnaryOp line);
        public R on(AWhitespace line);
	}
    public abstract <R> R accept(AssemblyLineVisitor<R> visitor);
}
