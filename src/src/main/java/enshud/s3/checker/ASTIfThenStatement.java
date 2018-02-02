package enshud.s3.checker;

public class ASTIfThenStatement extends ASTStatement
{
	private ASTExpressionNode condition;
	private ASTCompoundStatement thenStatement;
	
	public ASTIfThenStatement(final ASTExpressionNode condition, final ASTCompoundStatement thenStatement, final Record record)
	{
		super(record);
		this.condition=condition;
		this.thenStatement=thenStatement;
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
	
	public void setCondition(ASTExpressionNode condition)
	{
		this.condition=condition;
	}
	
	public ASTCompoundStatement getThenStatement()
	{
		return thenStatement;
	}
}
