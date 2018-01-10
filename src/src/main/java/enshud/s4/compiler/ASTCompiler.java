package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;
import enshud.s3.checker.*;
import java.util.HashMap;

public class ASTCompiler implements ASTVisitor
{
	public class TemporaryVariable
	{
		private int nvariable=0;
		private static final String prefix="@V";
		public String getNew()
		{
			++nvariable;
			return prefix+nvariable;
		}
		public String getLatest()
		{
			return prefix+nvariable;
		}
	}

	public class LabelTable
	{
		private HashMap<String, Integer> labels=new HashMap<>();
		private int nlabel=0;

		public String getNew()
		{
			String s="L"+nlabel;
			++nlabel;
			return s;
		}

		public void add(final String name, final int position)
		{
			labels.put(name, position);
		}
	}
	private CASLCode code=new CASLCode();
	private CASLCode storage=new CASLCode();
	private CASLCode constant=new CASLCode();
	private LabelTable labelTable=new LabelTable();
	private ASTFunctionTable table;
	private TemporaryVariable temporaryVariable=new TemporaryVariable();

	public ASTCompiler(final ASTFunctionTable table)
	{
		this.table=table;
		setStorage(table.getGlobal());
		table.getSubprogram().forEach(this::setStorage);
	}

	private void setStorage(final ASTFunctionRecord f)
	{
		f.getLocalVariables().getRecords().stream()
				.filter(v->v.getVariableType().getLength()>1)
				.forEach(v->storage.addLine(f.getName()+"."+v.getName(), CASLInst.DS, String.valueOf(v.getVariableType().getLength())));
	}

	private void setConstant(final ASTFactor f)
	{

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

	@Override
	public void visit(ASTAssignmentStatement n) throws ASTException
	{
		n.getExpression().accept(this);
		n.getVariable().accept(this);
		if(!n.getVariable().getReturnValueSymbol().equals(n.getExpression().getReturnValueSymbol()))
		{
			code.addLine(null, CASLInst.LD, n.getVariable().getReturnValueSymbol(), n.getExpression().getReturnValueSymbol());
		}
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
		code.addLine(null, n.getEvalType()==ASTEvalType.tInteger ? CASLInst.CPA : CASLInst.CPL, n.getLeft().getReturnValueSymbol(), n.getRight().getReturnValueSymbol());
	}

	@Override
	public void visit(ASTSimpleExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		if(n.getSign()==ASTSimpleExpression.NEGATIVE)
		{
			code.addLine(null, CASLInst.XOR, n.getLeft().getReturnValueSymbol(), "=#FFFF");
			code.addLine(null, CASLInst.ADDA, n.getLeft().getReturnValueSymbol(), "=#0001");
		}
		if(n.getRight()!=null)
		{
			n.getRight().accept(this);
			switch(n.getRecord().getTSToken())
			{
				case SPLUS:
				{
					code.addLine(null, CASLInst.ADDA, n.getLeft().getReturnValueSymbol(), n.getRight().getReturnValueSymbol());
					break;
				}

				case SMINUS:
				{
					code.addLine(null, CASLInst.SUBA, n.getLeft().getReturnValueSymbol(), n.getRight().getReturnValueSymbol());
					break;
				}

				case SOR:
				{
					code.addLine(null, CASLInst.OR, n.getLeft().getReturnValueSymbol(), n.getRight().getReturnValueSymbol());
					break;
				}
			}
		}
		n.setReturnValueSymbol(n.getLeft().getReturnValueSymbol());

	}

	@Override
	public void visit(ASTTerm n) throws ASTException
	{
		n.getLeft().accept(this);
		n.getRight().accept(this);
		if(n.getRecord().getTSToken()==TSToken.SAND)
		{
			code.addLine(null, CASLInst.OR, n.getLeft().getReturnValueSymbol(), n.getRight().getReturnValueSymbol());
		}
		else if(n.getRecord().getTSToken()==TSToken.SSTAR)
		{
			code.addLine(null, CASLInst.CALL, "MULT");
		}
		else
		{
			code.addLine(null, CASLInst.CALL, "DIV");
		}
		n.setReturnValueSymbol(n.getLeft().getReturnValueSymbol());
	}

	@Override
	public void visit(ASTFactor n) throws ASTException
	{
		if(n.getVariable()!=null)
		{
			n.getVariable().accept(this);
			n.setReturnValueSymbol(n.getVariable().getReturnValueSymbol());
		}
		else if(n.getExpression()!=null)
		{
			n.getExpression().accept(this);
			n.setReturnValueSymbol(n.getExpression().getReturnValueSymbol());
		}
		else if(n.getNotFactor()!=null)
		{
			n.getNotFactor().accept(this);
			code.addLine(null, CASLInst.XOR, n.getNotFactor().getReturnValueSymbol(), "=1");
			n.setReturnValueSymbol(n.getNotFactor().getReturnValueSymbol());
		}
		else
		{
			switch(n.getRecord().getTSToken())
			{
				case STRUE:
				{
					n.setReturnValueSymbol("=1");
					break;
				}

				case SFALSE:
				{
					n.setReturnValueSymbol("=0");
					break;
				}

				default:
				{
					if(n.getEvalType()==ASTEvalType.tString)
					{
						constant.addLine(null, CASLInst.DC, temporaryVariable.getNew(), n.getRecord().getText());
						break;
					}
					n.setReturnValueSymbol("="+n.getRecord().getText());
				}
			}
		}
	}

	@Override
	public void visit(ASTIfThenElseStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		String label=labelTable.getNew();
		switch(n.getCondition().getRecord().getTSToken())
		{
			case SEQUAL:
			{
				code.addLine(null, CASLInst.JNZ, label);
				break;
			}
			case SNOTEQUAL:
			{
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SLESS:
			{
				code.addLine(null, CASLInst.JPL, label);
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SLESSEQUAL:
			{
				code.addLine(null, CASLInst.JPL, label);
				break;
			}
			case SGREAT:
			{
				code.addLine(null, CASLInst.JMI, label);
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SGREATEQUAL:
			{
				code.addLine(null, CASLInst.JMI, label);
				break;
			}
		}
		n.getThenStatement().accept(this);
		labelTable.add(label, code.size());
		code.addLine(label, CASLInst.NOP);
		n.getElseStatement().accept(this);
		label=labelTable.getNew();
		labelTable.add(label, code.size());
		code.addLine(label, CASLInst.NOP);
	}

	@Override
	public void visit(ASTIfThenStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		String label=labelTable.getNew();
		switch(n.getCondition().getRecord().getTSToken())
		{
			case SEQUAL:
			{
				code.addLine(null, CASLInst.JNZ, label);
				break;
			}
			case SNOTEQUAL:
			{
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SLESS:
			{
				code.addLine(null, CASLInst.JPL, label);
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SLESSEQUAL:
			{
				code.addLine(null, CASLInst.JPL, label);
				break;
			}
			case SGREAT:
			{
				code.addLine(null, CASLInst.JMI, label);
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SGREATEQUAL:
			{
				code.addLine(null, CASLInst.JMI, label);
				break;
			}
		}
		n.getThenStatement().accept(this);
		labelTable.add(label, code.size());
		code.addLine(label, CASLInst.NOP);
	}

	@Override
	public void visit(ASTIndexedVariable n) throws ASTException
	{
		n.getIndex().accept(this);
		code.addLine(null, CASLInst.LD, temporaryVariable.getNew(), n.getName(), n.getIndex().getReturnValueSymbol());
		n.setReturnValueSymbol(temporaryVariable.getLatest());
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
		code.addLine(n.getName(), CASLInst.START, "BEGIN");
		n.getBlock().accept(this);
		n.getCompoundStatement().accept(this);
		code.addLine(null, CASLInst.END);
	}

	@Override
	public void visit(ASTPureVariable n) throws ASTException
	{
		n.setReturnValueSymbol(n.getName());
	}

	@Override
	public void visit(ASTVariableType n) throws ASTException
	{
	}

	@Override
	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{
		n.getCompoundStatement().accept(this);
	}

	@Override
	public void visit(ASTVariableDeclaration n) throws ASTException
	{
	}

	@Override
	public void visit(ASTWhileDoStatement n) throws ASTException
	{
		String label=labelTable.getNew();
		labelTable.add(label, code.size());
		code.addLine(label, CASLInst.NOP, "//while begin");
		n.getCondition().accept(this);
		label=labelTable.getNew();
		switch(n.getCondition().getRecord().getTSToken())
		{
			case SEQUAL:
			{
				code.addLine(null, CASLInst.JNZ, label);
				break;
			}
			case SNOTEQUAL:
			{
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SLESS:
			{
				code.addLine(null, CASLInst.JPL, label);
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SLESSEQUAL:
			{
				code.addLine(null, CASLInst.JPL, label);
				break;
			}
			case SGREAT:
			{
				code.addLine(null, CASLInst.JMI, label);
				code.addLine(null, CASLInst.JZE, label);
				break;
			}
			case SGREATEQUAL:
			{
				code.addLine(null, CASLInst.JMI, label);
				break;
			}
		}
		n.getDoStatement().accept(this);
		code.addLine(null, CASLInst.JPL, label, "//while end");
		labelTable.add(label, code.size());
		code.addLine(label, CASLInst.NOP);
	}
}
