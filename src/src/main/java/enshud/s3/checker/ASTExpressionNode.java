package enshud.s3.checker;

import enshud.s1.lexer.TSToken;

abstract public class ASTExpressionNode extends AST
{
	protected ASTEvalType evalType;

	public ASTExpressionNode(final Record record)
	{
		super(record);
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
