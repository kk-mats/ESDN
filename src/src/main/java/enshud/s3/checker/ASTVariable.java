package enshud.s3.checker;

public abstract class ASTVariable extends AST
{
	protected String name;
	protected ASTEvalType evalType;
	protected String resultSymbol;

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

	public String getResultSymbol()
	{
		return resultSymbol;
	}

	public void setName(final String name)
	{
		this.name=name;
	}

	public void setEvalType(final ASTEvalType evalType)
	{
		this.evalType=evalType;
	}

	public void setResultSymbol(final String resultSymbol)
	{
		this.resultSymbol=resultSymbol;
	}
}
