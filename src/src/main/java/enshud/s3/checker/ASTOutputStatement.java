package enshud.s3.checker;

import java.util.ArrayList;

public class ASTOutputStatement extends ASTStatement
{
	private ArrayList<ASTExpressionNode> expressions;
	
	public ASTOutputStatement(final ArrayList<ASTExpressionNode> expressions, final Record record)
	{
		super(record);
		this.expressions=expressions;
	}
	
	public void add(final ASTExpressionNode e)
	{
		if(null!=expressions)
		{
			expressions=new ArrayList<>();
		}
		expressions.add(e);
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

	public ArrayList<ASTExpressionNode> getExpressions()
	{
		return expressions;
	}
}
