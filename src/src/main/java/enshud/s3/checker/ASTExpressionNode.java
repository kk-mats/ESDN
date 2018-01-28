package enshud.s3.checker;

import enshud.s4.compiler.CASL;

abstract public class ASTExpressionNode extends AST
{
	protected ASTEvalType evalType;
	protected CASL.Operand resultSymbol;

	public ASTExpressionNode(final Record record)
	{
		super(record);
	}

	public ASTEvalType getEvalType()
	{
		return evalType;
	}

	public CASL.Operand getResultSymbol()
	{
		return resultSymbol;
	}

	public void setEvalType(final ASTEvalType evalType)
	{
		this.evalType=evalType;
	}

	public void setResultSymbol(final CASL.Operand resultSymbol)
	{
		this.resultSymbol=resultSymbol;
	}
}
