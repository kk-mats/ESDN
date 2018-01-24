package enshud.s3.checker;

public class ASTPureVariable extends ASTVariable
{
	public ASTPureVariable(final String name, final Record record)
	{
		super(record);
		this.name=name;
	}

	public ASTPureVariable(final String name, final int length,  Record record)
	{
		super(record);
		this.name=name;
		this.length=length;
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
