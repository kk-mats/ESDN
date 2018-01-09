package enshud.s3.checker;

public class ASTWhileDoStatement extends ASTStatement
{
	private ASTExpressionNode condition;
	private ASTStatement doStatement;

	public ASTWhileDoStatement(final ASTExpressionNode condition, final ASTStatement doStatement, final Record record)
	{
		super(record);
		this.condition=condition;
		this.doStatement=doStatement;
	}

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

	public ASTExpressionNode getCondition()
	{
		return condition;
	}

	public ASTStatement getDoStatement()
	{
		return doStatement;
	}
}
