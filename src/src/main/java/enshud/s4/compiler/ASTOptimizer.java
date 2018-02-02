package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;
import enshud.s3.checker.*;

public class ASTOptimizer implements ASTVisitor
{
	private ASTProgram program;
	
	public ASTOptimizer(ASTProgram program)
	{
		try
		{
			this.program=program;
			program.accept(this);
		}
		catch(ASTException e)
		{
			System.out.print(e);
		}
	}
	
	public ASTProgram getAST()
	{
		return program;
	}
	
	public ASTExpressionNode simplify(ASTExpressionNode n)
	{
		ASTExpressionNode left;
		ASTExpressionNode right;
		boolean sign=true;
		
		if(n instanceof ASTFactor)
		{
			if(((ASTFactor)n).getNotFactor()!=null && ((ASTFactor)n).getNotFactor().isConstant())
			{
				boolean b=((ASTFactor)n).getNotFactor().getRecord().getTSToken()==TSToken.STRUE;
				return new ASTFactor(new Record(b ? TSToken.SFALSE : TSToken.STRUE, b ? CASL.FalseString : CASL.TrueString, n.getLineNumber()));
			}
			if(((ASTFactor)n).getExpression()!=null && ((ASTFactor)n).getExpression() instanceof ASTFactor)
			{
				return ((ASTFactor)n).getExpression();
			}
		}
		
		if(n instanceof ASTExpression && ((ASTExpression)n).getLeft()!=null && ((ASTExpression)n).getRight()!=null)
		{
			left=((ASTExpression)n).getLeft();
			right=((ASTExpression)n).getRight();
		}
		else if(n instanceof ASTSimpleExpression && ((ASTSimpleExpression)n).getLeft()!=null && ((ASTSimpleExpression)n).getRight()!=null)
		{
			sign=((ASTSimpleExpression)n).getSign();
			left=((ASTSimpleExpression)n).getLeft();
			right=((ASTSimpleExpression)n).getRight();
		}
		else if(n instanceof ASTTerm && ((ASTTerm)n).getLeft()!=null && ((ASTTerm)n).getRight()!=null)
		{
			left=((ASTTerm)n).getLeft();
			right=((ASTTerm)n).getRight();
		}
		else
		{
			return n;
		}
		
		if(left instanceof ASTFactor && ((ASTFactor)left).isConstant() && right instanceof ASTFactor && ((ASTFactor)right).isConstant())
		{
			if(left.getEvalType()==ASTEvalType.tInteger && right.getEvalType()==ASTEvalType.tInteger)
			{
				int vleft=(sign ? 1 : -1)*Integer.parseInt(left.getRecord().getText());
				int vright=Integer.parseInt(right.getRecord().getText());
				Record r=null;
				switch(n.getRecord().getTSToken())
				{
					case SPLUS:
						r=new Record(TSToken.SCONSTANT, String.valueOf(vleft+vright), n.getLineNumber());
						break;
					case SMINUS:
						r=new Record(TSToken.SCONSTANT, String.valueOf(vleft-vright), n.getLineNumber());
						break;
					case SSTAR:
						r=new Record(TSToken.SCONSTANT, String.valueOf(vleft*vright), n.getLineNumber());
						break;
					case SDIVD:
						r=new Record(TSToken.SCONSTANT, String.valueOf(Math.floorDiv(vleft, vright)), n.getLineNumber());
						break;
					case SMOD:
						r=new Record(TSToken.SCONSTANT, String.valueOf(vleft%vright), n.getLineNumber());
						break;
					case SEQUAL:
						r=new Record(vleft==vright ? TSToken.STRUE : TSToken.SFALSE, vleft==vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SNOTEQUAL:
						r=new Record(vleft!=vright ? TSToken.STRUE : TSToken.SFALSE, vleft!=vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SLESS:
						r=new Record(vleft<vright ? TSToken.STRUE : TSToken.SFALSE, vleft<vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SLESSEQUAL:
						r=new Record(vleft<=vright ? TSToken.STRUE : TSToken.SFALSE, vleft<=vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SGREAT:
						r=new Record(vleft>vright ? TSToken.STRUE : TSToken.SFALSE, vleft>vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SGREATEQUAL:
						r=new Record(vleft >= vright ? TSToken.STRUE : TSToken.SFALSE, vleft >= vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
				}
				return new ASTFactor(r);
			}
			else if(left.getEvalType()==ASTEvalType.tBoolean && right.getEvalType()==ASTEvalType.tBoolean)
			{
				boolean vleft=left.getRecord().getTSToken()==TSToken.STRUE;
				boolean vright=right.getRecord().getTSToken()==TSToken.STRUE;
				Record r=null;
				switch(n.getRecord().getTSToken())
				{
					case SOR:
						r=new Record(vleft || vright ? TSToken.STRUE : TSToken.SFALSE, vleft || vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SAND:
						r=new Record(vleft && vright ? TSToken.STRUE : TSToken.SFALSE, vleft && vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SEQUAL:
						r=new Record(vleft==vright ? TSToken.STRUE : TSToken.SFALSE, vleft==vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SNOTEQUAL:
						r=new Record(vleft!=vright ? TSToken.STRUE : TSToken.SFALSE, vleft!=vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SLESS:
						r=new Record(!vleft && vright ? TSToken.STRUE : TSToken.SFALSE, !vleft && vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SLESSEQUAL:
						r=new Record(!vleft || vright ? TSToken.STRUE : TSToken.SFALSE, !vleft || vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SGREAT:
						r=new Record(vleft && !vright ? TSToken.STRUE : TSToken.SFALSE, vleft && !vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
					case SGREATEQUAL:
						r=new Record(vleft || !vright ? TSToken.STRUE : TSToken.SFALSE, vleft || !vright ? CASL.TrueString : CASL.FalseString, n.getLineNumber());
						break;
				}
				return new ASTFactor(r);
			}
		}
		return n;
	}
	
	public void visit(ASTAssignmentStatement n) throws ASTException
	{
		n.getVariable().accept(this);
		if(n.getVariable() instanceof ASTIndexedVariable)
		{
			((ASTIndexedVariable)n.getVariable()).setIndex(simplify(((ASTIndexedVariable)n.getVariable()).getIndex()));
		}
		n.getExpression().accept(this);
		n.setExpression(simplify(n.getExpression()));
	}
	
	public void visit(ASTBlock n) throws ASTException
	{
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
		if(n.getStatements()!=null)
		{
			for(ASTStatement s : n.getStatements())
			{
				s.accept(this);
			}
		}
	}
	
	public void visit(ASTExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		n.setLeft(simplify(n.getLeft()));
		n.getRight().accept(this);
		n.setRight(simplify(n.getRight()));
	}
	
	public void visit(ASTSimpleExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		n.setLeft(simplify(n.getLeft()));
		if(n.getRight()!=null)
		{
			n.getRight().accept(this);
			n.setRight(simplify(n.getRight()));
		}
	}
	
	public void visit(ASTTerm n) throws ASTException
	{
		n.getLeft().accept(this);
		n.setLeft(simplify(n.getLeft()));
		n.getRight().accept(this);
		n.setRight(simplify(n.getRight()));
	}
	
	public void visit(ASTFactor n) throws ASTException
	{
		if(n.getExpression()!=null)
		{
			n.getExpression().accept(this);
			n.setExpression(simplify(n.getExpression()));
		}
		else if(n.getVariable()!=null)
		{
			n.getVariable().accept(this);
		}
		else if(n.getNotFactor()!=null)
		{
			n.getNotFactor().accept(this);
			n.setNotFactor((ASTFactor)simplify(n.getNotFactor()));
		}
	}
	
	public void visit(ASTIfThenElseStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		n.setCondition(simplify(n.getCondition()));
		n.getThenStatement().accept(this);
		n.getElseStatement().accept(this);
	}
	
	public void visit(ASTIfThenStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		n.setCondition(simplify(n.getCondition()));
		n.getThenStatement().accept(this);
	}
	
	public void visit(ASTIndexedVariable n) throws ASTException
	{
		n.getIndex().accept(this);
		n.setIndex(simplify(n.getIndex()));
	}
	
	public void visit(ASTInputStatement n) throws ASTException
	{
		if(n.getVariables()!=null)
		{
			for(ASTVariable v : n.getVariables())
			{
				v.accept(this);
			}
		}
	}
	
	public void visit(ASTOutputStatement n) throws ASTException
	{
		if(n.getExpressions()!=null)
		{
			for(ASTExpressionNode e : n.getExpressions())
			{
				e.accept(this);
			}
		}
	}
	
	public void visit(ASTParameter n) throws ASTException
	{
	
	}
	
	public void visit(ASTProcedureCallStatement n) throws ASTException
	{
		if(n.getExpressions()!=null)
		{
			for(int i=0; i<n.getExpressions().size(); ++i)
			{
				n.getExpressions().get(i).accept(this);
				n.setExpression(i, simplify(n.getExpressions().get(i)));
			}
		}
	}
	
	public void visit(ASTProgram n) throws ASTException
	{
		n.getBlock().accept(this);
		n.getCompoundStatement().accept(this);
	}
	
	public void visit(ASTPureVariable n) throws ASTException
	{
	
	}
	
	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{
		n.getCompoundStatement().accept(this);
	}
	
	public void visit(ASTVariableDeclaration n) throws ASTException
	{
	
	}
	
	public void visit(ASTVariableType n) throws ASTException
	{
	
	}
	
	public void visit(ASTWhileDoStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		n.getDoStatement().accept(this);
		n.setCondition(simplify(n.getCondition()));
	}
}
