package enshud.s3.checker;

import java.util.ArrayList;

public class ASTCompoundStatement extends ASTStatement
{
	private ArrayList<ASTStatement> statements;
	
	public ASTCompoundStatement(final ArrayList<ASTStatement> statements, final Record record)
	{
		super(record);
		this.statements=statements;
	}
	
	public void add(ASTStatement statement)
	{
		statements.add(statement);
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

	public ArrayList<ASTStatement> getStatements()
	{
		return statements;
	}
}
