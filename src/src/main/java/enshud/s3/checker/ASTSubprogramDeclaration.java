package enshud.s3.checker;

import java.util.ArrayList;

public class ASTSubprogramDeclaration extends AST
{
	private String name;
	private ArrayList<ASTParameter> parameters;
	private ArrayList<ASTVariableDeclaration> variableDeclaration;
	private ASTCompoundStatement compoundStatement;
	
	
	public ASTSubprogramDeclaration(final String name, final ArrayList<ASTParameter> parameters, final ArrayList<ASTVariableDeclaration> variableDeclaration, final ASTCompoundStatement compoundStatement, final Record record)
	{
		super(record);
		this.name=name;
		this.parameters=parameters;
		this.variableDeclaration=variableDeclaration;
		this.compoundStatement=compoundStatement;
	}

	public void set(final ArrayList<ASTVariableDeclaration> variableDeclaration, final ASTCompoundStatement compoundStatement)
	{
		this.variableDeclaration=variableDeclaration;
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

	public ArrayList<ASTParameter> getParameters()
	{
		return parameters;
	}

	public ArrayList<ASTVariableDeclaration> getVariableDeclaration()
	{
		return variableDeclaration;
	}

	public ASTCompoundStatement getCompoundStatement()
	{
		return compoundStatement;
	}
}
