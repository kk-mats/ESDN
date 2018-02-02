package enshud.s3.checker;

public class ASTAssignmentStatement extends ASTStatement
{
	private ASTVariable variable;
	private ASTExpressionNode expression;
	
	public ASTAssignmentStatement(final ASTVariable variable, final ASTExpressionNode expression, final Record record)
	{
		super(record);
		this.variable=variable;
		this.expression=expression;
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

	public ASTVariable getVariable()
	{
		return variable;
	}

	public ASTExpressionNode getExpression()
	{
		return expression;
	}
	
	public void setExpression(ASTExpressionNode expression)
	{
		this.expression=expression;
	}
}
