package enshud.s3.checker;

public class SemErrorException extends ASTException
{
	private AST n;

	SemErrorException(final AST n)
	{
		this.n=n;
	}

	public String toString()
	{
		return "Semantic error: line "+n.getLineNumber();
	}
}
