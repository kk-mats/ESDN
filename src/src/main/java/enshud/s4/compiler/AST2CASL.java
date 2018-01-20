package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;
import enshud.s3.checker.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public class AST2CASL implements ASTVisitor
{
	public static class Label
	{
		private static int n=-1;
		private static String prefix="L";
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
		private static String prefix="@V";
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

	private CASL casl=new CASL();
	private ArrayList<CASL> caslList=new ArrayList<>();
	private ASTSymbolTable table;
	private BitSet libsInUse=new BitSet(CASL.Library.values().length);

	public CASL getCasl()
	{
		return caslList.get(0);
	}

	public ArrayList<CASL> getCaslSet()
	{
		return caslList;
	}

	private void appendLib()
	{
		if(libsInUse.isEmpty())
		{
			return;
		}
		caslList.get(0).addStorage("LIBBUF", 256);
		caslList.get(0).addStorage("LIBLEN", 1);
		for(CASL.Library type:CASL.Library.values())
		{
			if(libsInUse.get(type.ordinal()))
			{
				caslList.add(type.get());
			}
		}
	}

	private void use(final CASL.Library subLib)
	{
		libsInUse.set(subLib.ordinal());
	}

	public void run(final ASTProgram program, final ASTSymbolTable table)
	{
		try
		{
			this.table=table;
			program.accept(this);
			appendLib();
		}
		catch(ASTException e)
		{
			System.out.print(e);
		}
	}

	public void visit(ASTAssignmentStatement n) throws ASTException
	{
		n.getVariable().accept(this);
		n.getExpression().accept(this);
		if(n.getVariable() instanceof ASTIndexedVariable)
		{
			casl.addCode(CASL.Inst.ST, n.getExpression().getResultSymbol(), n.getVariable().getResultSymbol());
		}
		else
		{
			casl.addCode(CASL.Inst.LD, n.getVariable().getResultSymbol(), n.getExpression().getResultSymbol());
		}
	}

	public void visit(ASTBlock n) throws ASTException
	{

		if(n.getVariableDeclarations()!=null)
		{
			for(ASTVariableDeclaration v:n.getVariableDeclarations())
			{
				v.accept(this);
			}
		}

		caslList.add(casl);
		casl=new CASL();

		if(n.getSubprogramDeclarations()!=null)
		{
			for(ASTSubprogramDeclaration s:n.getSubprogramDeclarations())
			{
				s.accept(this);
				caslList.add(casl);
				casl=new CASL();
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
		casl.addCode(n.getLeft().getEvalType()==ASTEvalType.tBoolean ? CASL.Inst.CPL : CASL.Inst.CPA, n.getLeft().getResultSymbol(), n.getRight().getResultSymbol());
		String L1=Label.getNew();
		String L2=Label.getNew();
		switch(n.getRecord().getTSToken())
		{
			case SEQUAL: casl.addCode(CASL.Inst.JZE, L1); break;
			case SNOTEQUAL: casl.addCode(CASL.Inst.JNZ, L1); break;
			case SLESS: casl.addCode(CASL.Inst.JMI, L1); break;
			case SLESSEQUAL: casl.addCode(CASL.Inst.JMI, L1); casl.addCode(CASL.Inst.JZE, L1); break;
			case SGREAT: casl.addCode(CASL.Inst.JPL, L1); break;
			case SGREATEQUAL: casl.addCode(CASL.Inst.JPL, L1); casl.addCode(CASL.Inst.JZE, L1); break;
		}
		casl.addCode(CASL.Inst.LAD, Temporally.getNew(), String.valueOf(CASL.Code._false));
		casl.addCode(CASL.Inst.JUMP, L2);
		casl.addCode(L1);
		casl.addCode(CASL.Inst.LAD, Temporally.getLatest(), String.valueOf(CASL.Code._true));
		casl.addCode(L2);
		n.setResultSymbol(Temporally.getLatest());
	}

	public void visit(ASTSimpleExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		String left=Temporally.getNew();
		casl.addCode(CASL.Inst.LD, left, n.getLeft().getResultSymbol());
		if(n.getSign()==ASTSimpleExpression.NEGATIVE)
		{
			casl.addCode(CASL.Inst.XOR, left, "=#FFFF");
			casl.addCode(CASL.Inst.ADDA, left, "=1");
		}
		if(n.getRight()!=null)
		{
			n.getRight().accept(this);
			casl.addCode(CASL.Inst.of(n.getRecord().getTSToken()), left, n.getRight().getResultSymbol());
		}
		n.setResultSymbol(left);
	}

	public void visit(ASTTerm n) throws ASTException
	{
		n.getLeft().accept(this);
		n.getRight().accept(this);
		if(n.getRecord().getTSToken()==TSToken.SAND)
		{
			casl.addCode(CASL.Inst.LD, Temporally.getNew(), n.getLeft().getResultSymbol());
			casl.addCode(CASL.Inst.AND, Temporally.getLatest(), n.getRight().getResultSymbol());
		}
		else
		{
			casl.addCode(CASL.Inst.PUSH, "0", n.getLeft().getResultSymbol());
			casl.addCode(CASL.Inst.PUSH, "0", n.getRight().getResultSymbol());
			casl.addCode(CASL.Inst.CALL, n.getRecord().getTSToken()==TSToken.SSTAR ? "@Mul" : n.getRecord().getTSToken()==TSToken.SDIVD ? "@Div" : "@Mod");
			casl.addCode(CASL.Inst.POP, Temporally.getNew());
			if(n.getRecord().getTSToken()==TSToken.SSTAR)
			{
				libsInUse.set(CASL.Library.MULT.ordinal());
			}
			else if(n.getRecord().getTSToken()==TSToken.SDIVD)
			{
				libsInUse.set(CASL.Library.DIV.ordinal());
			}
			else
			{
				libsInUse.set(CASL.Library.MOD.ordinal());
			}
		}
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
			casl.addCode(CASL.Inst.LD, Temporally.getNew(), n.getNotFactor().getResultSymbol());
			casl.addCode(CASL.Inst.XOR, Temporally.getLatest(), "=#FFFF");
			n.setResultSymbol(Temporally.getLatest());
		}
		else if(n.getEvalType()==ASTEvalType.tString)
		{
			casl.addConstant(Label.getNew(), n.getRecord().getText());
			n.setResultSymbol(Label.getLatest());
		}
		else if(n.getEvalType()==ASTEvalType.tBoolean)
		{
			n.setResultSymbol("="+String.valueOf(n.getRecord().getText().equals("true") ? String.valueOf(CASL.Code._true) : String.valueOf(CASL.Code._false)));
		}
		else
		{
			n.setResultSymbol("="+n.getRecord().getText());
		}
	}

	public void visit(ASTIfThenElseStatement n) throws ASTException
	{
		String L1=Label.getNew();
		String L2=Label.getNew();
		n.getCondition().accept(this);
		casl.addCode(CASL.Inst.CPA, n.getCondition().getResultSymbol(), String.valueOf(CASL.Code._false));
		casl.addCode(CASL.Inst.JZE, L1);
		n.getThenStatement().accept(this);
		casl.addCode(CASL.Inst.JUMP, L2);
		casl.addCode(L1);
		n.getElseStatement().accept(this);
		casl.addCode(L2);
	}

	public void visit(ASTIfThenStatement n) throws ASTException
	{
		n.getCondition().accept(this);
		casl.addCode(CASL.Inst.CPA, n.getCondition().getResultSymbol(), String.valueOf(CASL.Code._false));
		casl.addCode(CASL.Inst.JZE, Label.getNew());
		n.getThenStatement().accept(this);
		casl.addCode(Label.getLatest());
	}

	public void visit(ASTIndexedVariable n) throws ASTException
	{
		n.getIndex().accept(this);
		n.setResultSymbol("@"+n.getName()+"["+n.getIndex().getResultSymbol()+"]");
	}

	public void visit(ASTInputStatement n) throws ASTException
	{
		casl.addCode(CASL.Inst.RPUSH);
		if(n.getVariables()==null)
		{
			casl.addCode(CASL.Inst.CALL, "RDLN");
			libsInUse.set(CASL.Library.RDLN.ordinal());
		}
		else
		{
			for(ASTVariable v:n.getVariables())
			{
				v.accept(this);
			}
			for(ASTVariable v:n.getVariables())
			{
				switch(v.getEvalType())
				{
					case tInteger:
					{
						casl.addCode(CASL.Inst.CALL, "RDINT");
						libsInUse.set(CASL.Library.RDINT.ordinal());
						break;
					}
					case tChar:
					{
						casl.addCode(CASL.Inst.CALL, "RDCH");
						libsInUse.set(CASL.Library.RDCH.ordinal());
						break;
					}
					case tString:
					{
						casl.addCode(CASL.Inst.PUSH, String.valueOf(v.getLength()));
						casl.addCode(CASL.Inst.CALL, "RDSTR");
						libsInUse.set(CASL.Library.RDSTR.ordinal());
					}
				}
				casl.addCode(CASL.Inst.POP, v.getResultSymbol());
			}
		}
		casl.addCode(CASL.Inst.RPOP);
	}

	public void visit(ASTOutputStatement n) throws ASTException
	{
		if(n.getExpressions()!=null)
		{
			for(ASTExpressionNode e:n.getExpressions())
			{
				e.accept(this);
			}

			for(ASTExpressionNode e:n.getExpressions())
			{
				casl.addCode(CASL.Inst.RPUSH);
				casl.addCode(CASL.Inst.PUSH, "LIBBUF");
				casl.addCode(CASL.Inst.PUSH, "LIBLEN");
				switch(e.getEvalType())
				{
					case tInteger:
					{
						casl.addCode(CASL.Inst.PUSH, e.getResultSymbol());
						casl.addCode(CASL.Inst.CALL, "WRTINT");
						libsInUse.set(CASL.Library.WRTINT.ordinal());
						break;
					}
					case tChar:
					{
						casl.addCode(CASL.Inst.PUSH, e.getResultSymbol());
						casl.addCode(CASL.Inst.CALL, "WRTCH");
						libsInUse.set(CASL.Library.WRTCH.ordinal());
						break;
					}
					case tString:
					{
						casl.addCode(CASL.Inst.PUSH, e.getResultSymbol());
						if(e instanceof ASTFactor && ((ASTFactor)e).getVariable()!=null)
						{
							casl.addCode(CASL.Inst.PUSH, String.valueOf(((ASTFactor)e).getVariable().getLength()-2));
						}
						else
						{
							casl.addCode(CASL.Inst.PUSH, String.valueOf(e.getRecord().getText().length()-2));
						}
						casl.addCode(CASL.Inst.CALL, "WRTSTR");
						libsInUse.set(CASL.Library.WRTSTR.ordinal());
					}
				}
				casl.addCode(CASL.Inst.RPOP);
			}
		}

		casl.addCode(CASL.Inst.RPUSH);
		casl.addCode(CASL.Inst.PUSH, "LIBBUF");
		casl.addCode(CASL.Inst.PUSH, "LIBLEN");
		casl.addCode(CASL.Inst.CALL, "WRTLN");
		libsInUse.set(CASL.Library.WRTLN.ordinal());
		casl.addCode(CASL.Inst.RPOP);
	}

	public void visit(ASTParameter n) throws ASTException
	{
		n.getNames().forEach(s->casl.addCode(CASL.Inst.POP, s));
	}

	public void visit(ASTProcedureCallStatement n) throws ASTException
	{
		casl.addCode(CASL.Inst.RPUSH);
		if(n.getExpressions()!=null)
		{
			for(ASTExpressionNode e : n.getExpressions())
			{
				e.accept(this);
			}
			for(ASTExpressionNode e:n.getExpressions())
			{
				casl.addCode(CASL.Inst.PUSH, "0", e.getResultSymbol());
			}
		}
		for(ASTVariableTable.ASTVariableRecord r:table.getGlobalVariableUsedIn(n.getName()).getRecords())
		{
			casl.addCode(CASL.Inst.PUSH, "0", r.getName());
		}
		casl.addCode(CASL.Inst.CALL, n.getName());
		for(ASTVariableTable.ASTVariableRecord r:table.getGlobalVariableUsedIn(n.getName()).getRecords())
		{
			casl.addCode(CASL.Inst.POP, r.getName());
		}
		casl.addCode(CASL.Inst.RPOP);
	}

	public void visit(ASTProgram n) throws ASTException
	{
		casl.addCode(n.getName().substring(0, 8).toUpperCase(), CASL.Inst.START);
		n.getCompoundStatement().accept(this);
		casl.addCode(CASL.Inst.RET);
		n.getBlock().accept(this);
	}

	public void visit(ASTPureVariable n) throws ASTException
	{
		n.setResultSymbol("@"+n.getName());
	}

	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{
		casl.addCode(n.getName(), CASL.Inst.START);
		String ret=Temporally.getNew();
		casl.addCode(CASL.Inst.POP, ret);
		ArrayList<ASTVariableTable.ASTVariableRecord> r=table.getGlobalVariableUsedIn(n.getName()).getRecords();
		Collections.reverse(r);
		r.forEach(s->casl.addCode(CASL.Inst.POP, s.getName()));
		if(n.getParameters()!=null)
		{
			ArrayList<ASTParameter> p=n.getParameters();
			Collections.reverse(p);
			p.forEach(s->s.getNames().forEach(t->casl.addCode(CASL.Inst.POP, t)));
		}
		casl.addCode(CASL.Inst.PUSH, "0", ret);
		n.getCompoundStatement().accept(this);
		casl.addCode(CASL.Inst.RET);
		if(n.getVariableDeclaration()!=null)
		{
			for(ASTVariableDeclaration v:n.getVariableDeclaration())
			{
				v.accept(this);
			}
		}
	}

	public void visit(ASTVariableDeclaration n) throws ASTException
	{
		if(n.getType().getEvalType().isArrayType())
		{
			n.getNames().forEach(s->casl.addStorage(s, n.getType().getLength()));
		}
	}

	public void visit(ASTVariableType n) throws ASTException
	{
	}

	public void visit(ASTWhileDoStatement n) throws ASTException
	{
		String L1=Label.getNew();
		String L2=Label.getNew();
		casl.addCode(CASL.Inst.JUMP, L2);
		casl.addCode(L1);
		n.getDoStatement().accept(this);
		casl.addCode(L2);
		n.getCondition().accept(this);
		casl.addCode(CASL.Inst.CPL, n.getCondition().getResultSymbol(), String.valueOf(CASL.Code._true));
		casl.addCode(CASL.Inst.JZE, L1);
	}
}
