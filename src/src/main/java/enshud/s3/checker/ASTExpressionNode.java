package enshud.s3.checker;

abstract public class ASTExpressionNode extends AST
{
	protected ASTEvalType evalType;
	protected String resultSymbol;

	public ASTExpressionNode(final Record record)
	{
		super(record);
	}

	public ASTEvalType getEvalType()
	{
		return evalType;
	}

	public String getResultSymbol()
	{
		return resultSymbol;
	}

	public void setEvalType(final ASTEvalType evalType)
	{
		this.evalType=evalType;
	}

	public void setResultSymbol(String resultSymbol)
	{
		this.resultSymbol=resultSymbol;
	}
}
