package enshud.s3.checker;

import java.util.ArrayList;

public class ASTVariableDeclaration extends AST
{
	private ArrayList<String> names;
	private ASTVariableType type;
	
	ASTVariableDeclaration(final ArrayList<String> names, final ASTVariableType type, final Record record)
	{
		super(record);
		this.names=names;
		this.type=type;
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

	public ArrayList<String> getNames()
	{
		return names;
	}

	public ASTVariableType getType()
	{
		return type;
	}

	public void setNames(final ArrayList<String> names)
	{
		this.names=names;
	}
}
