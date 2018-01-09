package enshud.s3.checker;

public abstract class AST
{
	protected Record record;
	
	public AST() {}
	
	public AST(final Record record)
	{
		this.record=record;
	}

	public abstract void accept(ASTVisitor visitor) throws ASTException;

	public Record getRecord()
	{
		return record;
	}

	public int getLineNumber()
	{
		return record.getLineNumber();
	}
}
