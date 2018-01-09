package enshud.s3.checker;

import java.util.ArrayList;

public class ASTInputStatement extends ASTStatement
{
	private ArrayList<ASTVariable> variables;
	
	public ASTInputStatement(final ArrayList<ASTVariable> variables, final Record record)
	{
		super(record);
		this.variables=variables;
	}
	
	public void add(final ASTVariable variable)
	{
		if(null!=variables)
		{
			variables=new ArrayList<>();
		}
		variables.add(variable);
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

	public ArrayList<ASTVariable> getVariables()
	{
		return variables;
	}
}
