package enshud.s4.compiler;

public class AST2IL //implements ASTVisitor
{/*
	public static class Label
	{
		private static int n=-1;
		private static String prefix="@L_";
		public static String getNew()
		{
			++n;
			return prefix+n;
		}
		public static String getLatest()
		{
			return prefix+n;
		}
	}

	public static class Temporally
	{
		private static int n=-1;
		private static String prefix="@V_";
		public static String getNew()
		{
			++n;
			return prefix+n;
		}
		public static String getLatest()
		{
			return prefix+n;
		}
	}

	private IL il=new IL();

	public IL getIL()
	{
		return il;
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

	public void visit(ASTAssignmentStatement n) throws ASTException
	{
		n.getVariable().accept(this);
		n.getExpression().accept(this);
		il.addCode(IL.Code.Assign(n.getVariable().getResultSymbol(), n.getExpression().getResultSymbol()));
	}

	public void visit(ASTBlock n) throws ASTException
	{
		if(n.getVariableDeclarations()!=null)
		{
			for(ASTVariableDeclaration v : n.getVariableDeclarations())
			{
				v.accept(this);
			}
		}

		if(n.getSubprogramDeclarations()!=null)
		{
			for(ASTSubprogramDeclaration s : n.getSubprogramDeclarations())
			{
				s.accept(this);
			}
		}
	}

	public void visit(ASTCompoundStatement n) throws ASTException
	{
		for(ASTStatement s:n.getStatements())
		{
			s.accept(this);
		}
	}

	public void visit(ASTExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		n.getRight().accept(this);
		String L1=Label.getNew();
		String L2=Label.getNew();
		n.setResultSymbol(Temporally.getNew());
		il.addCode(IL.Code.IfGoto(n.getLeft().getResultSymbol(), IL.Op.of(n.getRecord().getTSToken()), n.getRight().getResultSymbol(), L1));
		il.addCode(IL.Code.Assign(Temporally.getLatest(), "false"));
		il.addCode(IL.Code.Goto(L2));
		il.addCode(IL.Code.Label(L1));
		il.addCode(IL.Code.Assign(Temporally.getLatest(), "true"));
		il.addCode(IL.Code.Label(L2));
	}

	public void visit(ASTSimpleExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		String left=n.getLeft().getResultSymbol();
		if(n.getSign()==ASTSimpleExpression.NEGATIVE)
		{
			left=Temporally.getNew();
			il.addCode(IL.Code.Assign(left, IL.Op.Sub, n.getLeft().getResultSymbol()));
		}
		if(n.getRight()!=null)
		{
			n.getRight().accept(this);
			il.addCode(IL.Code.Assign(Temporally.getNew(), left, IL.Op.of(n.getRecord().getTSToken()), n.getRight().getResultSymbol()));
		}
		n.setResultSymbol(Temporally.getLatest());
	}

	public void visit(ASTTerm n) throws ASTException
	{
		n.getLeft().accept(this);
		n.getRight().accept(this);
		il.addCode(IL.Code.Assign(Temporally.getNew(), n.getLeft().getResultSymbol(), IL.Op.of(n.getRecord().getTSToken()), n.getRight().getResultSymbol()));
		n.setResultSymbol(Temporally.getLatest());
	}

	public void visit(ASTFactor n) throws ASTException
	{
		if(n.getVariable()!=null)
		{
			n.getVariable().accept(this);
			n.setResultSymbol(n.getVariable().getResultSymbol());
		}
		else if(n.getExpression()!=null)
		{
			n.getExpression().accept(this);
			n.setResultSymbol(n.getExpression().getResultSymbol());
		}
		else if(n.getNotFactor()!=null)
		{
			n.getNotFactor().accept(this);
			il.addCode(IL.Code.Assign(Temporally.getNew(), IL.Op.not, n.getNotFactor().getResultSymbol()));
			n.setResultSymbol(Temporally.getLatest());
		}
		else if(n.getEvalType()==ASTEvalType.tString)
		{
			il.addConstant(IL.Code.Def(Temporally.getNew(), n.getRecord().getText()));
			n.setResultSymbol(Temporally.getLatest());
		}
		else
		{
			n.setResultSymbol(n.getRecord().getText());
		}
	}

	public void visit(ASTIfThenElseStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		il.addCode(IL.Code.IfGoto(n.getCondition().getResultSymbol(), IL.Op.Equal, "false", Label.getNew()));
		String address=Label.getLatest();
		n.getThenStatement().accept(this);
		il.addCode(IL.Code.Goto(Label.getNew()));
		il.addCode(IL.Code.Label(address));
		address=Label.getLatest();
		n.getElseStatement().accept(this);
		il.addCode(IL.Code.Label(address));
	}

	public void visit(ASTIfThenStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		il.addCode(IL.Code.IfGoto(n.getCondition().getResultSymbol(), IL.Op.Equal, "false", Label.getNew()));
		String address=Label.getLatest();
		n.getThenStatement().accept(this);
		il.addCode(IL.Code.Label(address));
	}

	public void visit(ASTIndexedVariable n) throws ASTException
	{
		n.getIndex().accept(this);
		n.setResultSymbol(n.getName()+"["+n.getIndex().getResultSymbol()+"]");
	}

	public void visit(ASTInputStatement n) throws ASTException
	{
	}

	public void visit(ASTOutputStatement n) throws ASTException
	{
		for(ASTExpressionNode e:n.getExpressions())
		{
			e.accept(this);
		}
		n.getExpressions().forEach(e->il.addCode(IL.Code.Param(e.getResultSymbol())));
		il.addCode(IL.Code.Call("WRITELN", String.valueOf(n.getExpressions().size())));
	}

	public void visit(ASTParameter n) throws ASTException
	{

	}

	public void visit(ASTProcedureCallStatement n) throws ASTException
	{
		if(n.getExpressions()!=null)
		{
			for(ASTExpressionNode e : n.getExpressions())
			{
				e.accept(this);
			}
			n.getExpressions().forEach(e->il.addCode(IL.Code.Param(e.getResultSymbol())));
			il.addCode(IL.Code.Call(n.getName(), String.valueOf(n.getExpressions().size())));
			return;
		}
		il.addCode(IL.Code.Call(n.getName(), "0"));
	}

	public void visit(ASTProgram n) throws ASTException
	{
		il.addCode(IL.Code.Fun(n.getName()));
		n.getCompoundStatement().accept(this);
		il.addCode(IL.Code.Return());
		n.getBlock().accept(this);
	}

	public void visit(ASTPureVariable n) throws ASTException
	{
		n.setResultSymbol(n.getName());
	}

	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{
		if(n.getVariableDeclaration()!=null)
		{
			for(ASTVariableDeclaration v:n.getVariableDeclaration())
			{
				v.accept(this);
			}
		}
		il.addCode(IL.Code.Fun(n.getName()));
		n.getCompoundStatement().accept(this);
		il.addCode(IL.Code.Return());
	}

	public void visit(ASTVariableDeclaration n) throws ASTException
	{
		if(n.getType().getEvalType().isArrayType())
		{
			for(String s:n.getNames())
			{
				il.addStorage(IL.Code.Def(s, String.valueOf(n.getType().getLength())));
			}
		}
	}

	public void visit(ASTVariableType n) throws ASTException
	{
	}

	public void visit(ASTWhileDoStatement n) throws ASTException
	{
		String L1=Label.getNew();
		String L2=Label.getNew();
		il.addCode(IL.Code.Goto(L2));
		il.addCode(IL.Code.Label(L1));
		n.getDoStatement().accept(this);
		il.addCode(IL.Code.Label(L2));
		n.getCondition().accept(this);
		il.addCode(IL.Code.IfGoto(n.getCondition().getResultSymbol(), IL.Op.Equal, "true", L1));
	}*/
}
