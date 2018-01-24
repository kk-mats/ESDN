package enshud.s3.checker;

import enshud.s1.lexer.TSToken;

public class ASTVariableType extends AST
{
	private ASTEvalType evalType;
	private int length;
	private int offset=0;

	ASTVariableType(final Record record)
	{
		super(record);
		evalType=record.getTSToken()==TSToken.SINTEGER ? ASTEvalType.tInteger : record.getTSToken()==TSToken.SBOOLEAN ? ASTEvalType.tBoolean : ASTEvalType.tChar;
		length=1;
	}

	ASTVariableType(final int minOfIndex, final int maxOfIndex, final ASTEvalType type, final Record record)
	{
		super(record);
		this.length=maxOfIndex-minOfIndex+1;
		this.offset=minOfIndex;
		evalType=type;
	}

	public ASTEvalType getEvalType()
	{
		return evalType;
	}

	public int getLength()
	{
		return length;
	}

	public int getOffset()
	{
		return offset;
	}

	@Override
	public void accept(final ASTVisitor visitor) throws ASTException
	{
		try
		{
			visitor.visit(this);
		}
		catch (ASTException e)
		{
			throw e;
		}
	}
}
