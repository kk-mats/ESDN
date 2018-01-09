package enshud.s3.checker;

import java.util.stream.IntStream;

public class ASTPrinter implements ASTVisitor
{
	private int depth=0;

	private void printTabs()
	{
		IntStream.range(0, depth).forEach(i->System.out.print("\t"));
	}

	private void incDepth()
	{
		++depth;
	}

	private void decDepth()
	{
		--depth;
	}

	public void run(final ASTProgram program)
	{
		try
		{
			program.accept(this);
		}
		catch(ASTException e)
		{
			System.out.print(e);
		}
	}

	@Override
	public void visit(ASTAssignmentStatement n) throws ASTException
	{
		try
		{
			n.getVariable().accept(this);
			System.out.print(" := ");
			n.getExpression().accept(this);
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTBlock n) throws ASTException
	{
		try
		{
			if(null!=n.getVariableDeclarations())
			{
				System.out.print("var ");
				incDepth();
				for(ASTVariableDeclaration v:n.getVariableDeclarations())
				{
					v.accept(this);
				}
				decDepth();
			}

			System.out.println();

			if(null!=n.getSubprogramDeclarations())
			{
				for(ASTSubprogramDeclaration s:n.getSubprogramDeclarations())
				{
					s.accept(this);
				}
			}
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTCompoundStatement n) throws ASTException
	{
		printTabs();
		System.out.println("begin");
		incDepth();
		for(ASTStatement s:n.getStatements())
		{
			printTabs();
			s.accept(this);
			System.out.println(";");
		}
		decDepth();
		printTabs();
		System.out.print("end");
	}

	@Override
	public void visit(ASTExpression n) throws ASTException
	{
		System.out.print("(");
		System.out.print("");
		n.getLeft().accept(this);
		System.out.print(" "+n.getRecord().getText()+" ");
		n.getRight().accept(this);
		System.out.print(")");
	}

	@Override
	public void visit(ASTSimpleExpression n) throws ASTException
	{
		System.out.print("(");
		if(n.getSign()==ASTSimpleExpression.NEGATIVE)
		{
			System.out.print("-");
		}
		n.getLeft().accept(this);
		if(n.getRight()!=null)
		{
			System.out.print(" "+n.getRecord().getText()+" ");
			n.getRight().accept(this);
		}
		System.out.print(")");
	}

	@Override
	public void visit(ASTTerm n) throws ASTException
	{
		System.out.print("(");
		n.getLeft().accept(this);
		System.out.print(" "+n.getRecord().getText()+" ");
		n.getRight().accept(this);
		System.out.print(")");
	}

	@Override
	public void visit(ASTFactor n) throws ASTException
	{
		if(n.getVariable()!=null)
		{
			n.getVariable().accept(this);
		}
		else if(n.getExpression()!=null)
		{
			n.getExpression().accept(this);
		}
		else if(n.getNotFactor()!=null)
		{
			System.out.print("(not ");
			n.getNotFactor().accept(this);
			System.out.print(")");
		}
		else
		{
			System.out.print(n.getRecord().getText());
		}
	}

	@Override
	public void visit(ASTIfThenElseStatement n) throws ASTException
	{
		try
		{
			System.out.print("if ");
			n.getCondition().accept(this);
			System.out.println(" then");
			n.getThenStatement().accept(this);
			System.out.println("\n");
			printTabs();
			System.out.println("else");
			n.getElseStatement().accept(this);
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTIfThenStatement n) throws ASTException
	{
		try
		{
			System.out.print("if ");
			n.getCondition().accept(this);
			System.out.println(" then");
			n.getThenStatement().accept(this);
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTIndexedVariable n) throws ASTException
	{
		try
		{
			System.out.print(n.getName()+"[");
			n.getIndex().accept(this);
			System.out.print("]");
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTInputStatement n) throws ASTException
	{
		try
		{
			System.out.print("readln");
			if(n.getVariables()!=null)
			{
				System.out.print("(");
				for(ASTVariable v:n.getVariables())
				{
					v.accept(this);
					System.out.print(", ");
				}
				System.out.print(")");
			}
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTOutputStatement n) throws ASTException
	{
		try
		{
			System.out.print("writeln");
			if(n.getExpressions()!=null)
			{
				System.out.print("(");
				for(ASTExpressionNode e:n.getExpressions())
				{
					e.accept(this);
					System.out.print(", ");
				}
				System.out.print(")");
			}
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTParameter n) throws ASTException
	{
		try
		{
			System.out.print(String.join(",", n.getNames())+":");
			n.getStandardType().accept(this);
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTProcedureCallStatement n) throws ASTException
	{
		try
		{
			System.out.print(n.getName());
			if(n.getExpressions()!=null)
			{
				System.out.print("(");
				for(ASTExpressionNode e:n.getExpressions())
				{
					e.accept(this);
					System.out.print(", ");
				}
				System.out.print(")");
			}
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTProgram n) throws ASTException
	{
		try
		{
			System.out.print("program "+n.getName());
			System.out.print(" ("+String.join(", ", n.getNames())+");\n");
			n.getBlock().accept(this);
			n.getCompoundStatement().accept(this);
			System.out.println(".");
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTPureVariable n) throws ASTException
	{
		System.out.print(n.getName());
	}

	@Override
	public void visit(ASTVariableType n) throws ASTException
	{
		if(n.getEvalType().isStandardType())
		{
			System.out.print(n.getRecord().getText());
			return;
		}
		System.out.print("array["+n.getOffset()+".."+(n.getOffset()+n.getLength())+"] of ");
		System.out.print(n.getEvalType()==ASTEvalType.tIntegerArray ? "integer" : n.getEvalType()==ASTEvalType.tBooleanArray ? "boolean" : "char");

	}

	@Override
	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{
		try
		{
			System.out.print("procedure "+n.getName());
			if(n.getParameters()!=null)
			{
				System.out.print(" (");
				for(ASTParameter p:n.getParameters())
				{
					p.accept(this);
					System.out.print(";");
				}
				System.out.print(")");
			}
			System.out.println(";");

			if(n.getVariableDeclaration()!=null)
			{
				System.out.print("var ");
				for(ASTVariableDeclaration v:n.getVariableDeclaration())
				{
					v.accept(this);
				}
			}
			n.getCompoundStatement().accept(this);
			System.out.println(";\n");
		}
		catch(ASTException e)
		{
			throw e;
		}
	}

	@Override
	public void visit(ASTVariableDeclaration n) throws ASTException
	{
		System.out.print(String.join(", ", n.getNames())+":");
		n.getType().accept(this);
		System.out.println(";");
	}

	@Override
	public void visit(ASTWhileDoStatement n) throws ASTException
	{
		try
		{
			System.out.print("while ");
			n.getCondition().accept(this);
			System.out.println(" do");
			n.getDoStatement().accept(this);
		}
		catch(ASTException e)
		{
			throw e;
		}
	}
}
