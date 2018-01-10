package enshud.s3.checker;

public abstract class ASTVariable extends AST
{
	protected String name;
	protected ASTEvalType evalType;
	protected String returnValueSymbol;

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

	public String getReturnValueSymbol()
	{
		return returnValueSymbol;
	}

	public void setName(final String name)
	{
		this.name=name;
	}

	public void setEvalType(final ASTEvalType evalType)
	{
		this.evalType=evalType;
	}

	public void setReturnValueSymbol(final String returnValueSymbol)
	{
		this.returnValueSymbol=returnValueSymbol;
	}
}
