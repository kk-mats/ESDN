package enshud.s3.checker;

import java.util.ArrayList;

public class ASTProcedureCallStatement extends ASTStatement
{
	private String name;
	private ArrayList<ASTExpressionNode> expressions;
	
	public ASTProcedureCallStatement(final String name, final ArrayList<ASTExpressionNode> expressions, final Record record)
	{
		super(record);
		this.name=name;
		this.expressions=expressions;
	}
	
	public void add(final ASTExpressionNode expression)
	{
		if(null!=expressions)
		{
			expressions=new ArrayList<>();
		}
		expressions.add(expression);
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

	public String getName()
	{
		return name;
	}

	public ArrayList<ASTExpressionNode> getExpressions()
	{
		return expressions;
	}
}
