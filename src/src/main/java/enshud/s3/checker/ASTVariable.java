package enshud.s3.checker;

public abstract class ASTVariable extends AST
{
	protected String name;
	protected ASTEvalType evalType;

	public ASTVariable(final Record record)
	{
		super(record);
	}

	public String getName()
	{
		return name;
	}

	public ASTEvalType getEvalType()
	{
		return evalType;
	}

	public void setEvalType(final ASTEvalType evalType)
	{
		this.evalType=evalType;
	}
}
