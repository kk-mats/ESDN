package enshud.s3.checker;

public class ASTIndexedVariable extends ASTVariable
{
	private ASTExpressionNode index;
	
	public ASTIndexedVariable(final String name, final ASTExpressionNode index, final Record record)
	{
		super(record);
		this.name=name;
		this.index=index;
		this.length=1;
	}
	
	@Override
	public void convertToPointer()
	{
		isPointer=false;
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

	public ASTExpressionNode getIndex()
	{
		return index;
	}

	public void setIndex(final ASTExpressionNode index)
	{
		this.index=index;
	}
}
