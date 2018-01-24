package enshud.s3.checker;

import enshud.s4.compiler.CASL;

public abstract class ASTVariable extends AST
{
	protected ASTEvalType evalType;
	protected String name;
	protected int length;
	protected CASL.Operand resultSymbol;

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

	public CASL.Operand getResultSymbol()
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

	public void setResultSymbol(final CASL.Operand resultSymbol)
	{
		this.resultSymbol=resultSymbol;
	}
}
