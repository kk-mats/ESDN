package enshud.s3.checker;

import java.util.ArrayList;

public class ASTProgram extends AST
{
	private String name;
	private ArrayList<String> names;
	private ASTBlock block;
	private ASTCompoundStatement compoundStatement;
	
	public ASTProgram(final String name, final ArrayList<String> names, final ASTBlock block, final ASTCompoundStatement compoundStatement, final Record record)
	{
		super(record);
		this.name=name;
		this.names=names;
		this.block=block;
		this.compoundStatement=compoundStatement;
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

	public ArrayList<String> getNames()
	{
		return names;
	}

	public ASTBlock getBlock()
	{
		return block;
	}

	public ASTCompoundStatement getCompoundStatement()
	{
		return compoundStatement;
	}
}
