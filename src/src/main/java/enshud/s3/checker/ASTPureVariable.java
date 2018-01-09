package enshud.s3.checker;

import enshud.s1.lexer.TSToken;

public class ASTPureVariable extends ASTVariable
{
	public ASTPureVariable(final String name, final Record record)
	{
		super(record);
		this.name=name;
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
