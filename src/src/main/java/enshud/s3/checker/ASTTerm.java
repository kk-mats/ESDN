package enshud.s3.checker;

public class ASTTerm extends ASTExpressionNode
{
	private ASTExpressionNode left;
	private ASTExpressionNode right;

	public ASTTerm(final ASTExpressionNode left, final ASTExpressionNode right, final Record record)
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
