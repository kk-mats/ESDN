package enshud.s3.checker;

public abstract class ASTVariable extends AST
{
	protected ASTEvalType evalType;
	protected String name;
	protected int length=1;
	protected String resultSymbol;

	public ASTVariable(final Record record)
	{
		super(record);
	}

	public String getName()
	{
		return name;
	}

	public int getLength()
	{
		return length;
	}

	public String getResultSymbol()
	{
		return resultSymbol;
	}

	public ASTEvalType getEvalType()
	{
		return evalType;
	}

	public void setName(final String name)
	{
		this.name=name;
	}

	public void setLength(final int length)
	{
		this.length=length;
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
