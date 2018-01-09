package enshud.s4.compiler;

import enshud.s3.checker.*;

public class ASTCompiler implements ASTVisitor
{
	CASLCode code=new CASLCode();
	CASLCode storage=new CASLCode();
	CASLCode constant=new CASLCode();

	ASTFunctionTable table;

	public ASTCompiler(final ASTFunctionTable table)
	{
		this.table=table;
		setStorage(table.getGlobal());
		table.getSubprogram().forEach(this::setStorage);
	}

	public void run(final ASTProgram program)
	{
		try
		{
			program.accept(this);
		}
		catch(ASTException e)
		{
			System.out.println(e);
		}
	}

	public String toString()
	{
		return String.join("\n", new String[]{code.toString(), storage.toString(), constant.toString()});
	}

	private void setStorage(final ASTFunctionRecord f)
	{
		for(ASTVariableTable.ASTVariableRecord r:f.getLocalVariables().getRecords())
		{

		}
	}

	private void setConstant(final ASTFactor f)
	{
		if(f.getEvalType()==ASTEvalType.tString)
		{
		}
	}

	@Override
	public void visit(ASTAssignmentStatement n) throws ASTException
	{

	}

	@Override
	public void visit(ASTBlock n) throws ASTException
	{
		for(ASTVariableDeclaration v:n.getVariableDeclarations())
		{
			v.accept(this);
		}
		for(ASTSubprogramDeclaration s:n.getSubprogramDeclarations())
		{
			s.accept(this);
		}
	}

	@Override
	public void visit(ASTCompoundStatement n) throws ASTException
	{

	}

	@Override
	public void visit(ASTExpression n) throws ASTException
	{

	}

	@Override
	public void visit(ASTSimpleExpression n) throws ASTException
	{

	}

	@Override
	public void visit(ASTTerm n) throws ASTException
	{

	}

	@Override
	public void visit(ASTFactor n) throws ASTException
	{

	}

	@Override
	public void visit(ASTIfThenElseStatement n) throws ASTException
	{

	}

	@Override
	public void visit(ASTIfThenStatement n) throws ASTException
	{

	}

	@Override
	public void visit(ASTIndexedVariable n) throws ASTException
	{

	}

	@Override
	public void visit(ASTInputStatement n) throws ASTException
	{

	}

	@Override
	public void visit(ASTOutputStatement n) throws ASTException
	{

	}

	@Override
	public void visit(ASTParameter n) throws ASTException
	{

	}

	@Override
	public void visit(ASTProcedureCallStatement n) throws ASTException
	{

	}

	@Override
	public void visit(ASTProgram n) throws ASTException
	{
		code.addLine(n.getName(), CASLInst.START, new String[]{"BEGIN"});
		n.getBlock().accept(this);
		n.getCompoundStatement().accept(this);
		code.addLine(null, CASLInst.END, new String[]{});
	}

	@Override
	public void visit(ASTPureVariable n) throws ASTException
	{

	}

	@Override
	public void visit(ASTVariableType n) throws ASTException
	{

	}

	@Override
	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{

	}

	@Override
	public void visit(ASTVariableDeclaration n) throws ASTException
	{

	}

	@Override
	public void visit(ASTWhileDoStatement n) throws ASTException
	{

	}
}
