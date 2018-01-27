package enshud.s3.checker;

import java.util.ArrayList;

public class ASTParameter extends AST
{
	private ArrayList<String> names;
	private ASTVariableType standardType;
	
	public ASTParameter(final ArrayList<String> names, final ASTVariableType standardType, final Record record)
	{
		super(record);
		this.names=names;
		this.standardType=standardType;
	}
	
	public void add(final String name)
	{
		names.add(name);
	}

	public ArrayList<String> getNames()
	{
		return names;
	}
	
	public void setName(final int i, final String name)
	{
		names.set(i, name);
	}

	public ASTEvalType getEvalType()
	{
		return standardType.getEvalType();
	}

	public ASTVariableType getStandardType()
	{
		return standardType;
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
