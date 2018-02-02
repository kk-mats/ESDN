package enshud.s3.checker;

public class ASTIfThenElseStatement extends ASTStatement
{
	private ASTExpressionNode condition;
	private ASTCompoundStatement thenStatement;
	private ASTCompoundStatement elseStatement;
	
	public ASTIfThenElseStatement(final ASTExpressionNode condition, final ASTCompoundStatement thenStatement, final ASTCompoundStatement elseStatement, final Record record)
	{
		super(record);
		this.condition=condition;
		this.thenStatement=thenStatement;
		this.elseStatement=elseStatement;
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

	public ASTCompoundStatement getThenStatement()
	{
		return thenStatement;
	}

	public ASTCompoundStatement getElseStatement()
	{
		return elseStatement;
	}
	
	public void setCondition(ASTExpressionNode condition)
	{
		this.condition=condition;
	}
}
