package enshud.s3.checker;

abstract public class ASTExpressionNode extends AST
{
	protected ASTEvalType evalType;
	protected String returnValueSymbol;

	public ASTExpressionNode(final Record record)
	{
		super(record);
	}

	public ASTEvalType getEvalType()
	{
		return evalType;
	}

	public String getReturnValueSymbol()
	{
		return returnValueSymbol;
	}

	public void setEvalType(final ASTEvalType evalType)
	{
		this.evalType=evalType;
	}

	public void setReturnValueSymbol(String returnValueSymbol)
	{
		this.returnValueSymbol=returnValueSymbol;
	}
}
