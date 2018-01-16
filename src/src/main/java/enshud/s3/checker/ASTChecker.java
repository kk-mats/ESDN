package enshud.s3.checker;

import enshud.s1.lexer.TSToken;

import java.util.ArrayDeque;

public class ASTChecker implements ASTVisitor
{
	private ASTSymbolTable table;
	private ArrayDeque<String> scope=new ArrayDeque<>();

	public ASTChecker()
	{
		table=new ASTSymbolTable();
	}

	public ASTSymbolTable getTable()
	{
		return table;
	}

	public void run(ASTProgram program)
	{
		try
		{
			program.accept(this);
			System.out.println("OK");
		}
		catch(ASTException e)
		{
			System.err.println(e.toString());
		}
	}

	@Override
	public void visit(ASTAssignmentStatement n) throws ASTException
	{
		n.getVariable().accept(this);
		n.getExpression().accept(this);

		if(n.getVariable().getEvalType()!=n.getExpression().getEvalType() || n.getVariable().getEvalType().isArrayType())
		{
			throw new SemErrorException(n.getVariable());
		}
	}

	@Override
	public void visit(ASTBlock n) throws ASTException
	{
		if(n.getVariableDeclarations()!=null)
		{
			for(ASTVariableDeclaration v:n.getVariableDeclarations())
			{
				v.accept(this);
				if(!table.addGlobalVariable(v))
				{
					throw new SemErrorException(v);
				}
			}
		}

		for(ASTSubprogramDeclaration s:n.getSubprogramDeclarations())
		{
			s.accept(this);
		}
	}

	@Override
	public void visit(ASTCompoundStatement n) throws ASTException
	{
		for(ASTStatement s:n.getStatements())
		{
			s.accept(this);
		}
	}

	@Override
	public void visit(ASTExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		n.getRight().accept(this);
		if(n.getLeft().getEvalType()!=n.getRight().getEvalType() || n.getLeft().getEvalType().isArrayType())
		{
			throw new SemErrorException(n.getRight());
		}
		n.setEvalType(ASTEvalType.tBoolean);
	}

	@Override
	public void visit(ASTSimpleExpression n) throws ASTException
	{
		n.getLeft().accept(this);

		if(n.getRight()!=null)
		{
			n.getRight().accept(this);

			if(n.getLeft().getEvalType()!=n.getRight().getEvalType())
			{
				throw new SemErrorException(n.getRight());
			}

			switch(n.getRecord().getTSToken())
			{
				case SPLUS:
				case SMINUS:
				{
					if(n.getLeft().getEvalType()!=ASTEvalType.tInteger)
					{
						throw new SemErrorException(n);
					}
					break;
				}
				case SOR:
				{
					if(n.getLeft().getEvalType()!=ASTEvalType.tBoolean)
					{
						throw new SemErrorException(n);
					}
					break;
				}
				default:
				{
					throw new SemErrorException(n);
				}
			}
		}
		n.setEvalType(n.getLeft().getEvalType());
	}

	@Override
	public void visit(ASTTerm n) throws ASTException
	{
		n.getLeft().accept(this);
		n.getRight().accept(this);
		if(n.getLeft().getEvalType()!=n.getRight().getEvalType())
		{
			throw new SemErrorException(n.getRight());
		}

		switch(n.getRecord().getTSToken())
		{
			case SSTAR:
			case SDIVD:
			case SMOD:
			{
				if(n.getLeft().getEvalType()!=ASTEvalType.tInteger)
				{
					throw new SemErrorException(n);
				}
				break;
			}
			case SAND:
			{
				if(n.getLeft().getEvalType()!=ASTEvalType.tBoolean)
				{
					throw new SemErrorException(n);
				}
				break;
			}
			default:
			{
				throw new SemErrorException(n);
			}
		}
		n.setEvalType(n.getLeft().getEvalType());
	}

	@Override
	public void visit(ASTFactor n) throws ASTException
	{
		if(n.getVariable()!=null)
		{
			n.getVariable().accept(this);
			n.setEvalType(n.getVariable().getEvalType());
		}
		else if(n.getExpression()!=null)
		{
			n.getExpression().accept(this);
			n.setEvalType(n.getExpression().getEvalType());
		}
		else if(n.getNotFactor()!=null)
		{
			n.getNotFactor().accept(this);
			if(n.getNotFactor().getEvalType()!=ASTEvalType.tBoolean)
			{
				throw new SemErrorException(n.getNotFactor());
			}
			n.setEvalType(n.getNotFactor().getEvalType());
		}
	}

	@Override
	public void visit(ASTIfThenElseStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		if(n.getCondition().getEvalType()!=ASTEvalType.tBoolean)
		{
			throw new SemErrorException(n.getCondition());
		}
		n.getThenStatement().accept(this);
		n.getElseStatement().accept(this);
	}

	@Override
	public void visit(ASTIfThenStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		if(n.getCondition().getEvalType()!=ASTEvalType.tBoolean)
		{
			throw new SemErrorException(n.getCondition());
		}
		n.getThenStatement().accept(this);
	}

	@Override
	public void visit(ASTIndexedVariable n) throws ASTException
	{
		ASTVariableType v=table.getVariableType(n.getName());
		if(v.getEvalType().isArrayType())
		{
			n.getIndex().accept(this);
			if(n.getIndex().getEvalType()!=ASTEvalType.tInteger)
			{
				throw new SemErrorException(n.getIndex());
			}
			n.setIndex(new ASTSimpleExpression(ASTSimpleExpression.POSITIVE, n.getIndex(), new ASTFactor(new Record(TSToken.SCONSTANT, String.valueOf(v.getOffset()), n.getRecord().getLineNumber())), new Record(TSToken.SMINUS, "-", n.getRecord().getLineNumber())));
			((ASTSimpleExpression)n.getIndex()).getRight().setEvalType(ASTEvalType.tInteger);
			n.setEvalType(v.getEvalType().toStandardType());
			n.setName(table.getScope(scope, n.getName()));
			return;
		}
		throw new SemErrorException(n);
	}

	@Override
	public void visit(ASTInputStatement n) throws ASTException
	{
		if(n.getVariables()==null)
		{
			return;
		}
		for(ASTVariable v:n.getVariables())
		{
			v.accept(this);
			if(v.getEvalType()!=ASTEvalType.tInteger && v.getEvalType()!=ASTEvalType.tChar && v.getEvalType()!=ASTEvalType.tString)
			{
				throw new SemErrorException(v);
			}
		}
	}

	@Override
	public void visit(ASTOutputStatement n) throws ASTException
	{
		if(n.getExpressions()==null)
		{
			return;
		}
		for(ASTExpressionNode e:n.getExpressions())
		{
			e.accept(this);
			if(e.getEvalType()!=ASTEvalType.tInteger && e.getEvalType()!=ASTEvalType.tChar && e.getEvalType()!=ASTEvalType.tString)
			{
				throw new SemErrorException(e);
			}
		}
	}

	@Override
	public void visit(ASTParameter n) throws ASTException
	{

	}
	
	public void visit(ASTProcedureCallStatement n) throws ASTException
	{
		ASTVariableTable parameter=table.getFunctionParameter(n.getName());

		if(parameter==null)
		{
			throw new SemErrorException(n);
		}

		if(n.getExpressions()==null)
		{
			if(parameter.size()!=0)
			{
				throw new SemErrorException(n);
			}
			return;
		}

		if(n.getExpressions().size()==parameter.size())
		{
			for(int i=0; i<parameter.size(); ++i)
			{
				n.getExpressions().get(i).accept(this);
				if(n.getExpressions().get(i).getEvalType()!=parameter.get(i).getEvalType())
				{
					throw new SemErrorException(n.getExpressions().get(i));
				}
			}
			return;
		}
		throw new SemErrorException(n);
	}

	@Override
	public void visit(ASTProgram n) throws ASTException
	{
		table.setGlobalName(n.getName());
		scope.addLast(n.getName());
		for(String s:n.getNames())
		{
			if(!table.addGlobalParameter(s, n.getRecord()))
			{
				throw new SemErrorException(n);
			}
		}
		n.getBlock().accept(this);
		n.getCompoundStatement().accept(this);
		scope.removeLast();
	}

	@Override
	public void visit(ASTPureVariable n) throws ASTException
	{
		ASTVariableType v=table.getVariableType(n.getName());
		if(v!=null)
		{
			n.setEvalType(v.getEvalType());
			n.setName(table.getScope(scope, n.getName()));
			return;
		}
		throw new SemErrorException(n);
	}

	@Override
	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{
		ASTFunctionRecord r=new ASTFunctionRecord();

		if(table.usedFunctionName(n.getName()))
		{
			throw new SemErrorException(n);
		}
		r.setName(n.getName());
		scope.addLast(n.getName());

		if(n.getParameters()!=null)
		{
			for(ASTParameter p:n.getParameters())
			{
				for(String s:p.getNames())
				{
					if(r.findBy(s))
					{
						throw new SemErrorException(p);
					}
					r.addParameter(s, p.getStandardType());
				}
			}
		}

		if(null!=n.getVariableDeclaration())
		{
			for(ASTVariableDeclaration v:n.getVariableDeclaration())
			{
				v.accept(this);
				for(String s:v.getNames())
				{
					if(r.findBy(s))
					{
						throw new SemErrorException(v);
					}
					r.addLocalVariable(s, v.getType());
				}
			}
		}
		table.addRecord(r);
		n.getCompoundStatement().accept(this);
		scope.removeLast();
	}

	@Override
	public void visit(ASTVariableDeclaration n) throws ASTException
	{

	}

	@Override
	public void visit(ASTVariableType n) throws ASTException
	{
	}

	@Override
	public void visit(ASTWhileDoStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		if(n.getCondition().getEvalType()!=ASTEvalType.tBoolean)
		{
			throw new SemErrorException(n.getCondition());
		}
		n.getDoStatement().accept(this);
	}
}