package enshud.s3.checker;

import java.util.ArrayList;

public class ASTBlock extends AST
{
	private ArrayList<ASTVariableDeclaration> variableDeclarations;
	private ArrayList<ASTSubprogramDeclaration> subprogramDeclarations;
	
	public ASTBlock(final ArrayList<ASTVariableDeclaration> variableDeclarations, final ArrayList<ASTSubprogramDeclaration> subprogramDeclarations, final Record record)
	{
		super(record);
		this.variableDeclarations=variableDeclarations;
		this.subprogramDeclarations=subprogramDeclarations;
	}
	
	public void add(final ASTVariableDeclaration variableDeclaration)
	{
		if(null!=variableDeclarations)
		{
			variableDeclarations=new ArrayList<>();
		}
		variableDeclarations.add(variableDeclaration);
	}
	
	public void add(final ASTSubprogramDeclaration subrogramDeclaration)
	{
		if(null!=subprogramDeclarations)
		{
			subprogramDeclarations=new ArrayList<>();
		}
		subprogramDeclarations.add(subrogramDeclaration);
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

	public ArrayList<ASTVariableDeclaration> getVariableDeclarations()
	{
		return variableDeclarations;
	}

	public ArrayList<ASTSubprogramDeclaration> getSubprogramDeclarations()
	{
		return subprogramDeclarations;
	}
}