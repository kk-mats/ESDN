package enshud.s4.compiler;

import enshud.s3.checker.ASTProgram;

public class ASTCompiler
{
	private CASL code=new CASL();
	private CASL storage=new CASL();
	private CASL constant=new CASL();

	private IL il;

	public ASTCompiler(final ASTProgram program)
	{
		AST2IL a2i=new AST2IL();
		a2i.run(program);
		il=a2i.getIL();
	}

	public void run()
	{

	}

	public void optimize()
	{

	}

	public void IL2CASL()
	{

	}
}
