package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;
import enshud.s3.checker.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AST2CASL implements ASTVisitor
{
	public static class Label
	{
		private static int n=-1;
		private static String prefix="L";
		public static CASL.OperandElement getNew()
		{
			++n;
			return new CASL.OperandElement(prefix+n, CASL.OperandElement.Attribute.address);
		}
		public static CASL.OperandElement getLatest()
		{
			return new CASL.OperandElement(prefix+n, CASL.OperandElement.Attribute.address);
		}
	}

	public static class Temporally
	{
		private static int n=-1;
		private static String prefix="@V";
		public static CASL.OperandElement getNew()
		{
			++n;
			return new CASL.OperandElement(prefix+n, CASL.OperandElement.Attribute.register);
		}
		public static CASL.OperandElement getLatest()
		{
			return new CASL.OperandElement(prefix+n, CASL.OperandElement.Attribute.register);
		}
	}

	private CASL casl=new CASL();
	private ArrayList<CASL> caslList=new ArrayList<>();
	private ASTSymbolTable table;

	public CASL getCasl()
	{
		if(!CASL.Library.isEmpty())
		{
			caslList.get(0).addStorage(new CASL.OperandElement("LIBBUF", CASL.OperandElement.Attribute.address), 256);
			caslList.get(0).addStorage(new CASL.OperandElement("LIBLEN", CASL.OperandElement.Attribute.address), 1);
		}
		return caslList.get(0);
	}

	public String getLibraries()
	{
		return Arrays.stream(CASL.Library.values())
				.filter(CASL.Library::isInUse)
				.map(CASL.Library::get)
				.reduce("", (joined, s)->joined+"\n\n"+s);
	}

	public void run(final ASTProgram program, final ASTSymbolTable table)
	{
		try
		{
			this.table=table;
			program.accept(this);
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
			//casl.addCode(CASL.Inst.LD, );
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
		CASL.OperandElement L1=Label.getNew();
		CASL.OperandElement L2=Label.getNew();
		switch(n.getRecord().getTSToken())
		{
			case SEQUAL: casl.addCode(CASL.Inst.JZE, L1); break;
			case SNOTEQUAL: casl.addCode(CASL.Inst.JNZ, L1); break;
			case SLESS: casl.addCode(CASL.Inst.JMI, L1); break;
			case SLESSEQUAL: casl.addCode(CASL.Inst.JMI, L1); casl.addCode(CASL.Inst.JZE, L1); break;
			case SGREAT: casl.addCode(CASL.Inst.JPL, L1); break;
			case SGREATEQUAL: casl.addCode(CASL.Inst.JPL, L1); casl.addCode(CASL.Inst.JZE, L1); break;
		}
		casl.addCode(CASL.Inst.LAD, Temporally.getNew(), CASL.OperandElement.ofFalse);
		casl.addCode(CASL.Inst.JUMP, L2);
		casl.addCode(L1);
		casl.addCode(CASL.Inst.LAD, Temporally.getLatest(), CASL.OperandElement.ofTrue);
		casl.addCode(L2);
		n.setResultSymbol(new CASL.Operand(Temporally.getLatest()));
	}

	public void visit(ASTSimpleExpression n) throws ASTException
	{
		n.getLeft().accept(this);
		CASL.OperandElement left=Temporally.getNew();
		casl.addCode(CASL.Inst.LD, left, n.getLeft().getResultSymbol());
		if(n.getSign()==ASTSimpleExpression.NEGATIVE)
		{
			casl.addCode(CASL.Inst.XOR, left, new CASL.OperandElement("#FFFF", CASL.OperandElement.Attribute.literal));
			casl.addCode(CASL.Inst.ADDA, left, new CASL.OperandElement("1", CASL.OperandElement.Attribute.integer));
		}
		if(n.getRight()!=null)
		{
			n.getRight().accept(this);
			casl.addCode(CASL.Inst.of(n.getRecord().getTSToken()), left, n.getRight().getResultSymbol());
		}
		n.setResultSymbol(new CASL.Operand(left));
	}

	public void visit(ASTTerm n) throws ASTException
	{
		n.getLeft().accept(this);
		n.getRight().accept(this);
		if(n.getRecord().getTSToken()==TSToken.SAND)
		{
			casl.addCode(CASL.Inst.LD, Temporally.getNew(), n.getLeft().getResultSymbol());
			casl.addCode(CASL.Inst.AND, Temporally.getLatest(), n.getRight().getResultSymbol());
			n.setResultSymbol(new CASL.Operand(Temporally.getLatest()));
			return;
		}
		casl.addCode(CASL.Inst.RPUSH);
		casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement("RETV", CASL.OperandElement.Attribute.address));
		casl.addCode(CASL.Inst.PUSH, n.getRight().getResultSymbol());
		casl.addCode(CASL.Inst.PUSH, n.getLeft().getResultSymbol());
		casl.addCode(CASL.Inst.CALL, new CASL.OperandElement(n.getRecord().getTSToken()==TSToken.SSTAR ? "MULT" : n.getRecord().getTSToken()==TSToken.SDIVD ? "DIV" : "MOD", CASL.OperandElement.Attribute.address));
		casl.addCode(CASL.Inst.RPOP);
		if(n.getRecord().getTSToken()==TSToken.SSTAR)
		{
			CASL.Library.use(CASL.Library.MULT);
		}
		else if(n.getRecord().getTSToken()==TSToken.SDIVD)
		{
			CASL.Library.use(CASL.Library.DIV);
		}
		else
		{
			CASL.Library.use(CASL.Library.MOD);
		}

		n.setResultSymbol(new CASL.Operand(new CASL.OperandElement("RETV", CASL.OperandElement.Attribute.address)));
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
			casl.addCode(CASL.Inst.XOR, Temporally.getLatest(), new CASL.OperandElement("#FFFF", CASL.OperandElement.Attribute.literal));
			n.setResultSymbol(new CASL.Operand(Temporally.getLatest()));
		}
		else if(n.getEvalType()==ASTEvalType.tString)
		{
			casl.addConstant(Label.getNew(), new CASL.OperandElement(n.getRecord().getText(), CASL.OperandElement.Attribute.literal));
			n.setResultSymbol(new CASL.Operand(Label.getLatest()));
		}
		else if(n.getEvalType()==ASTEvalType.tBoolean)
		{
			n.setResultSymbol(new CASL.Operand(n.getRecord().getText().equals("true") ? CASL.OperandElement.ofTrue : CASL.OperandElement.ofFalse));
		}
		else
		{
			n.setResultSymbol(new CASL.Operand(new CASL.OperandElement(n.getRecord().getText(), CASL.OperandElement.Attribute.literal)));
		}
	}

	public void visit(ASTIfThenElseStatement n) throws ASTException
	{
		CASL.OperandElement L1=Label.getNew();
		CASL.OperandElement L2=Label.getNew();
		n.getCondition().accept(this);
		casl.addCode(CASL.Inst.CPA, n.getCondition().getResultSymbol(), CASL.OperandElement.ofFalse);
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
		casl.addCode(CASL.Inst.CPA, n.getCondition().getResultSymbol(), CASL.OperandElement.ofFalse);
		n.getThenStatement().accept(this);
		casl.addCode(Label.getLatest());
	}

	public void visit(ASTIndexedVariable n) throws ASTException
	{
		n.getIndex().accept(this);
		n.setResultSymbol(new CASL.Operand(new CASL.OperandElement(n.getName(), CASL.OperandElement.Attribute.address)).join(n.getIndex().getResultSymbol()));
	}

	public void visit(ASTInputStatement n) throws ASTException
	{
		if(n.getVariables()!=null)
		{
			for(ASTVariable v:n.getVariables())
			{
				v.accept(this);
			}
			for(ASTVariable v:n.getVariables())
			{
				casl.addCode(CASL.Inst.RPUSH);
				switch(v.getEvalType())
				{
					case tInteger:
					{
						casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement("RETV", CASL.OperandElement.Attribute.address));
						casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("RDINT", CASL.OperandElement.Attribute.address));
						casl.addCode(CASL.Inst.RPOP);
						casl.addCode(CASL.Inst.LD, v.getResultSymbol(), new CASL.OperandElement("RETV", CASL.OperandElement.Attribute.address));
						// use Library.RDINT
						CASL.Library.use(CASL.Library.RDINT);
						break;
					}
					case tChar:
					{
						casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement("RETV", CASL.OperandElement.Attribute.address));
						casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("RDCH", CASL.OperandElement.Attribute.address));
						casl.addCode(CASL.Inst.RPOP);
						casl.addCode(CASL.Inst.LD, v.getResultSymbol(), new CASL.OperandElement("RETV", CASL.OperandElement.Attribute.address));
						// use Library.RDCH
						CASL.Library.use(CASL.Library.RDCH);
						break;
					}
					case tString:
					{
						casl.addCode(CASL.Inst.PUSH, v.getResultSymbol());
						casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement(String.valueOf(v.getLength()), CASL.OperandElement.Attribute.address));
						casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("RDSTR", CASL.OperandElement.Attribute.address));
						casl.addCode(CASL.Inst.RPOP);
						// use Library.RDSTR
						CASL.Library.use(CASL.Library.RDSTR);
					}
				}
			}
			return;
		}
		casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("RDLN", CASL.OperandElement.Attribute.address));
		CASL.Library.use(CASL.Library.RDLN);
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
				casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement("LIBBUF", CASL.OperandElement.Attribute.address));
				casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement("LIBLEN", CASL.OperandElement.Attribute.address));
				CASL.Operand operand=e.getResultSymbol();
				switch(e.getEvalType())
				{
					case tInteger:
					{
						casl.addCode(CASL.Inst.PUSH, operand);
						casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("WRTINT", CASL.OperandElement.Attribute.address));
						CASL.Library.use(CASL.Library.WRTINT);
						break;
					}
					case tChar:
					{
						casl.addCode(CASL.Inst.PUSH, operand);
						casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("WRTCH", CASL.OperandElement.Attribute.address));
						CASL.Library.use(CASL.Library.WRTCH);
						break;
					}
					case tString:
					{
						casl.addCode(CASL.Inst.PUSH, e.getResultSymbol());
						if(e instanceof ASTFactor && ((ASTFactor)e).getVariable()!=null)
						{
							casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement(String.valueOf(((ASTFactor)e).getVariable().getLength()), CASL.OperandElement.Attribute.address));
						}
						else
						{
							casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement(String.valueOf(e.getRecord().getText().length()-2), CASL.OperandElement.Attribute.address));
						}
						casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("WRTSTR", CASL.OperandElement.Attribute.address));
						CASL.Library.use(CASL.Library.WRTSTR);
					}
				}
				casl.addCode(CASL.Inst.RPOP);
			}
		}

		casl.addCode(CASL.Inst.RPUSH);
		casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement("LIBBUF", CASL.OperandElement.Attribute.address));
		casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement("LIBLEN", CASL.OperandElement.Attribute.address));
		casl.addCode(CASL.Inst.CALL, new CASL.OperandElement("WRTLN", CASL.OperandElement.Attribute.address));
		CASL.Library.use(CASL.Library.WRTLN);
		casl.addCode(CASL.Inst.RPOP);
	}

	public void visit(ASTParameter n) throws ASTException
	{
		n.getNames().forEach(s->casl.addCode(CASL.Inst.POP, new CASL.OperandElement(s, CASL.OperandElement.Attribute.register)));
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
				casl.addCode(CASL.Inst.PUSH, e.getResultSymbol());
			}
		}
		// Todo support label and register
		for(ASTVariableTable.ASTVariableRecord r:table.getGlobalVariableUsedIn(n.getName()).getRecords())
		{
			casl.addCode(CASL.Inst.PUSH, new CASL.OperandElement(r.getName(), CASL.OperandElement.Attribute.register));
		}
		casl.addCode(CASL.Inst.CALL, new CASL.OperandElement(n.getName(), CASL.OperandElement.Attribute.address));
		for(ASTVariableTable.ASTVariableRecord r:table.getGlobalVariableUsedIn(n.getName()).getRecords())
		{
			casl.addCode(CASL.Inst.POP, new CASL.OperandElement(r.getName(), CASL.OperandElement.Attribute.register));
		}
		casl.addCode(CASL.Inst.RPOP);
	}

	public void visit(ASTProgram n) throws ASTException
	{
		casl.addCode(new CASL.OperandElement(n.getName(), CASL.OperandElement.Attribute.address), CASL.Inst.START);
		n.getCompoundStatement().accept(this);
		casl.addCode(CASL.Inst.RET);
		n.getBlock().accept(this);
		if(CASL.Library.useRETV())
		{
			caslList.get(0).addStorage(new CASL.OperandElement("RETV", CASL.OperandElement.Attribute.address), 1);
		}
	}

	public void visit(ASTPureVariable n) throws ASTException
	{
		if(n.getEvalType().isArrayType())
		{
			n.setResultSymbol(new CASL.Operand(new CASL.OperandElement(n.getName(), CASL.OperandElement.Attribute.address)));
			return;
		}
		n.setResultSymbol(new CASL.Operand(new CASL.OperandElement("@"+n.getName(), CASL.OperandElement.Attribute.register)));
	}

	public void visit(ASTSubprogramDeclaration n) throws ASTException
	{
		casl.addCode(new CASL.OperandElement(n.getName(), CASL.OperandElement.Attribute.address), CASL.Inst.START);
		CASL.OperandElement ret=Temporally.getNew();
		casl.addCode(CASL.Inst.POP, ret);
		ArrayList<ASTVariableTable.ASTVariableRecord> r=table.getGlobalVariableUsedIn(n.getName()).getRecords();
		Collections.reverse(r);
		// Todo support label and register
		r.forEach(s->casl.addCode(CASL.Inst.POP, new CASL.OperandElement(s.getName(), CASL.OperandElement.Attribute.register)));
		if(n.getParameters()!=null)
		{
			ArrayList<ASTParameter> p=n.getParameters();
			Collections.reverse(p);
			p.forEach(s->s.getNames().forEach(t->casl.addCode(CASL.Inst.POP, new CASL.OperandElement(t, CASL.OperandElement.Attribute.register))));
		}
		casl.addCode(CASL.Inst.PUSH, ret);
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
			n.getNames().forEach(s->casl.addStorage(new CASL.OperandElement(s, CASL.OperandElement.Attribute.address), n.getType().getLength()));
		}
	}

	public void visit(ASTVariableType n) throws ASTException
	{
	}

	public void visit(ASTWhileDoStatement n) throws ASTException
	{
		CASL.OperandElement L1=Label.getNew();
		CASL.OperandElement L2=Label.getNew();
		casl.addCode(CASL.Inst.JUMP, L2);
		casl.addCode(L1);
		n.getDoStatement().accept(this);
		casl.addCode(L2);
		n.getCondition().accept(this);
		casl.addCode(CASL.Inst.CPL, n.getCondition().getResultSymbol(), CASL.OperandElement.ofTrue);
		casl.addCode(CASL.Inst.JZE, L1);
	}
}
