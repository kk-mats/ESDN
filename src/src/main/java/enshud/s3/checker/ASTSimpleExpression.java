package enshud.s3.checker;

public class ASTSimpleExpression extends ASTExpressionNode
{
	public static final boolean POSITIVE=true;
	public static final boolean NEGATIVE=false;

	protected boolean sign;
	private ASTExpressionNode left;
	private ASTExpressionNode right;

	public ASTSimpleExpression(final boolean sign, final ASTExpressionNode left, final ASTExpressionNode right, final Record record)
	{
		super(record);
		this.sign=sign;
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

	public boolean getSign()
	{
		return sign;
	}

	public void setSign(boolean sign)
	{
		this.sign=sign;
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
