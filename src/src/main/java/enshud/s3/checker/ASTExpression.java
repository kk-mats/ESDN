package enshud.s3.checker;

public class ASTExpression extends ASTExpressionNode
{
	private ASTExpressionNode left;
	private ASTExpressionNode right;

	public ASTExpression(ASTExpressionNode left, ASTExpressionNode right, Record record)
	{
		super(record);
		this.left=left;
		this.right=right;
	}

	public ASTExpressionNode getLeft()
	{
		return left;
	}

	public ASTExpressionNode getRight()
	{
		return right;
	}
	
	public void setLeft(ASTExpressionNode left)
	{
		this.left=left;
	}
	
	public void setRight(ASTExpressionNode right)
	{
		this.right=right;
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
