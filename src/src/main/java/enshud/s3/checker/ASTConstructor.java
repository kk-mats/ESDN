package enshud.s3.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import enshud.s1.lexer.TSToken;

public class ASTConstructor
{
	private Record lookahead;
	private boolean fail=false;

	private ArrayList<Record> records;
	private int pos;

	private ASTProgram root;
	
	public ASTConstructor(final String inputFileName)
	{
		records=new ArrayList<>();
		pos=0;
		run(inputFileName);
	}

	private boolean match(final TSToken TSToken)
	{
		if(lookahead.is(TSToken))
		{
			return read();
		}
		
		throw new Error("expecting "+TSToken.toString()+"; "+lookahead.getTSToken().toString()+" found.");
		
	}
	private boolean read()
	{
		if(pos<records.size()-1)
		{
			++pos;
			lookahead=records.get(pos);
			return true;
		}
		return false;
	}
	
	private int store()
	{
		return pos;
	}
	
	private void back(final int previous)
	{
		pos=previous;
		lookahead=records.get(pos);
	}
	
	private void syntaxError()
	{
		if(!fail)
		{
			System.err.println("Syntax error: line "+records.get(pos).getLineNumber());
			//System.out.println(tokens.get(current).label());
			fail=true;
		}
	}

	private String name()
	{
		if(lookahead.is(TSToken.SIDENTIFIER))
		{
			String s=lookahead.getText();
			match(TSToken.SIDENTIFIER);
			return s;
		}
		return null;
	}
	
	private ASTVariableType standardType()
	{
		for(TSToken t:new TSToken[]{TSToken.SINTEGER, TSToken.SCHAR, TSToken.SBOOLEAN})
		{
			if(lookahead.is(t))
			{
				Record r=records.get(pos);
				match(t);
				return new ASTVariableType(r);
			}
		}

		return null;
	}
	
	private Integer natural()
	{
		if(lookahead.is(TSToken.SCONSTANT))
		{
			Integer s=Integer.parseInt(lookahead.getText());
			match(TSToken.SCONSTANT);
			return s;
		}
		return null;
	}
	
	private boolean sign()
	{
		if(lookahead.is(TSToken.SPLUS))
		{
			match(TSToken.SPLUS);
			return ASTSimpleExpression.POSITIVE;
		}
		else if(lookahead.is(TSToken.SMINUS))
		{
			match(TSToken.SMINUS);
			return ASTSimpleExpression.NEGATIVE;
		}
		return ASTSimpleExpression.POSITIVE;
	}
	
	private Integer integer()
	{
		boolean sign=sign();
		Integer n=natural();

		return null!=n ? sign ? n : -n : null;
	}
	
	private Integer minOfIndex()
	{
		return integer();
	}
	
	private Integer maxOfIndex()
	{
		return integer();
	}
	
	private ASTVariableType arrayType()
	{
		if(lookahead.is(TSToken.SARRAY))
		{
			Record r=records.get(pos);
			match(TSToken.SARRAY);
			if(lookahead.is(TSToken.SLBRACKET))
			{
				match(TSToken.SLBRACKET);
				Integer minOfIndex=minOfIndex();
				if(null!=minOfIndex && lookahead.is(TSToken.SRANGE))
				{
					match(TSToken.SRANGE);
					Integer maxOfIndex=maxOfIndex();
					if(null!=maxOfIndex && lookahead.is(TSToken.SRBRACKET))
					{
						match(TSToken.SRBRACKET);
						if(lookahead.is(TSToken.SOF))
						{
							match(TSToken.SOF);
							ASTVariableType type=standardType();
							if(null!=type)
							{
								return new ASTVariableType(minOfIndex, maxOfIndex, type.getEvalType().toArrayType(), r);
							}
						}
					}
				}
			}
		}
		syntaxError();
		return null;
	}
	
	private ASTVariableType type()
	{
		int previous=store();
		ASTVariableType type=standardType();
		if(null==type)
		{	
			back(previous);
			if(null==(type=arrayType()))
			{
				syntaxError();
				return null;
			}
		}

		return type;
	}
	
	private String variableName()
	{
		return name();
	}
	
	private ArrayList<String> variableNameList()
	{
		ArrayList<String> names=new ArrayList<>();
		int previous=store();
		String name=variableName();
		if(null!=name)
		{
			names.add(name);
			while(lookahead.is(TSToken.SCOMMA))
			{
				match(TSToken.SCOMMA);
				if(null==(name=variableName()))
				{
					syntaxError();
					return null;
				}
				names.add(name);
			}
			
			return names;
		}
		back(previous);
		return null;
	}
	
	private ArrayList<ASTVariableDeclaration> variableDeclarationList()
	{
		Record r=records.get(pos);
		ArrayList<String> names=variableNameList();
		ArrayList<ASTVariableDeclaration> variableDeclaration=new ArrayList<>();
		ASTVariableType variableType;
		if(null!=names)
		{
			do
			{
				if(!lookahead.is(TSToken.SCOLON))
				{
					syntaxError();
					return null;
				}
				match(TSToken.SCOLON);
				variableType=type();
				if(null==variableType || !lookahead.is(TSToken.SSEMICOLON))
				{
					syntaxError();
					return null;
				}
				match(TSToken.SSEMICOLON);

				variableDeclaration.add(new ASTVariableDeclaration(names, variableType, r));
				r=records.get(pos);
			}
			while(lookahead.is(TSToken.SIDENTIFIER) && null!=(names=variableNameList()));

			return variableDeclaration;
		}
		syntaxError();
		return null;
	}
	
	private ArrayList<ASTVariableDeclaration> variableDeclaration()
	{
		ArrayList<ASTVariableDeclaration> variableDeclarations=null;
		if(lookahead.is(TSToken.SVAR))
		{
			match(TSToken.SVAR);
			if(null==(variableDeclarations=variableDeclarationList()))
			{
				syntaxError();
				return null;
			}
		}

		return variableDeclarations;
	}
	
	private String procedureName()
	{
		return name();
	}
	
	private String parameterName()
	{
		return name();
	}
	
	private ArrayList<String> parameterNameList()
	{
		String name;
		ArrayList<String> names=new ArrayList<>();

		do
		{
			if(null!=(name=parameterName()))
			{
				names.add(name);
				continue;
			}
			syntaxError();
			return null;
		}
		while(lookahead.is(TSToken.SCOMMA) && match(TSToken.SCOMMA));

		return names;
	}
	
	private ArrayList<ASTParameter> parameterList()
	{
		ArrayList<String> names;
		ASTVariableType type;
		ArrayList<ASTParameter> parameters=new ArrayList<>();
		Record r;
		do
		{
			r=records.get(pos);
			if(null!=(names=parameterNameList()) && lookahead.is(TSToken.SCOLON))
			{
				match(TSToken.SCOLON);
				type=standardType();
				if(null!=type)
				{
					parameters.add(new ASTParameter(names, type, r));
					continue;
				}
			}
			syntaxError();
			return null;
		}
		while(lookahead.is(TSToken.SSEMICOLON) && match(TSToken.SSEMICOLON));

		return parameters;
	}
	
	private ArrayList<ASTParameter> parameter()
	{
		if(lookahead.is(TSToken.SLPAREN))
		{
			match(TSToken.SLPAREN);
			ArrayList<ASTParameter> parameters=parameterList();
			if(null!=parameters && lookahead.is(TSToken.SRPAREN))
			{
				match(TSToken.SRPAREN);
				return parameters;
			}
			syntaxError();
		}
		return null;
	}
	
	private ASTSubprogramDeclaration subprogramHead()
	{
		if(lookahead.is(TSToken.SPROCEDURE))
		{
			Record r=records.get(pos);
			match(TSToken.SPROCEDURE);
			String name=procedureName();
			if(null!=name)
			{
				ArrayList<ASTParameter> parameters=parameter();
				if(lookahead.is(TSToken.SSEMICOLON))
				{
					match(TSToken.SSEMICOLON);
					return new ASTSubprogramDeclaration(name, parameters, null, null, r);
				}
			}
			syntaxError();
		}
		return null;
	}
	
	private ASTPureVariable pureVariable()
	{
		int previous=store();
		Record r=records.get(pos);
		String name=variableName();
		if(null!=name)
		{
			return new ASTPureVariable(name, r);
		}
		back(previous);
		return null;
	}
	
	private ASTFactor constant()
	{
		Record r=records.get(pos);
		Integer n=natural();
		if(null!=n)
		{
			return new ASTFactor(r);
		}
		else if(lookahead.is(TSToken.SSTRING))
		{
			match(TSToken.SSTRING);
			return new ASTFactor(r);
		}
		else if(lookahead.is(TSToken.SFALSE))
		{
			match(TSToken.SFALSE);
			return new ASTFactor(r);
		}
		else if(lookahead.is(TSToken.STRUE))
		{
			match(TSToken.STRUE);
			return new ASTFactor(r);
		}
		return null;
	}
	
	private ASTFactor factor()
	{
		Record r=records.get(pos);
		ASTVariable variable=variable();
		if(variable!=null)
		{
			return new ASTFactor(variable, r);
		}
		
		ASTFactor constant=constant();
		if(constant!=null)
		{
			return constant;
		}

		if(lookahead.is(TSToken.SLPAREN))
		{
			match(TSToken.SLPAREN);
			r=records.get(pos);
			ASTExpressionNode expression=expression();
			if(expression!=null && lookahead.is(TSToken.SRPAREN))
			{
				match(TSToken.SRPAREN);
				return new ASTFactor(expression, r);
			}
			syntaxError();
			return null;
		}

		if(lookahead.is(TSToken.SNOT))
		{
			match(TSToken.SNOT);
			ASTFactor factor=factor();
			if(factor!=null)
			{
				return new ASTFactor(factor, r);
			}
		}
		syntaxError();
		return null;
	}
	
	private Record additiveOperator()
	{
		for(TSToken t:new TSToken[]{TSToken.SPLUS, TSToken.SMINUS, TSToken.SOR})
		{
			if(lookahead.is(t))
			{
				Record r=records.get(pos);
				match(t);
				return r;
			}
		}
		return null;
	}
	
	private Record multiplicativeOperator()
	{
		for(TSToken t:new TSToken[]{TSToken.SSTAR, TSToken.SDIVD, TSToken.SMOD, TSToken.SAND})
		{
			if(lookahead.is(t))
			{
				Record r=records.get(pos);
				match(t);
				return r;
			}
		}
		return null;
	}
	
	private ASTExpressionNode term()
	{
		ASTExpressionNode left=factor();
		if(left==null)
		{
			syntaxError();
			return null;
		}

		Record r=multiplicativeOperator();
		if(r!=null)
		{
			ASTExpressionNode right;
			do
			{
				right=factor();
				if(right==null)
				{
					syntaxError();
					return null;
				}
				left=new ASTTerm(left, right, r);
			}
			while((r=multiplicativeOperator())!=null);
		}
		return left;
	}
	
	private ASTExpressionNode simpleExpression()
	{
		boolean sign=sign();
		ASTExpressionNode left=term();

		if(left==null)
		{
			syntaxError();
			return null;
		}
		Record r=additiveOperator();

		if(r==null)
		{
			return sign ? left : new ASTSimpleExpression(sign, left, null, left.getRecord());
		}

		ASTExpressionNode right=term();
		if(right==null)
		{
			syntaxError();
			return null;
		}

		left=new ASTSimpleExpression(sign, left, right, r);

		if((r=additiveOperator())==null)
		{
			return left;
		}

		do
		{
			right=term();
			if(right==null)
			{
				syntaxError();
				return null;
			}
			left=new ASTSimpleExpression(ASTSimpleExpression.POSITIVE, left, right, r);
		}
		while((r=additiveOperator())!=null);

		return new ASTSimpleExpression(ASTSimpleExpression.POSITIVE, ((ASTSimpleExpression)left).getLeft(), ((ASTSimpleExpression)left).getRight(), left.getRecord());
	}
	
	private Record relationalOperator()
	{
		for(TSToken t:new TSToken[]{TSToken.SEQUAL, TSToken.SNOTEQUAL, TSToken.SLESS, TSToken.SLESSEQUAL, TSToken.SGREAT, TSToken.SGREATEQUAL})
		{
			if(lookahead.is(t))
			{
				Record r=records.get(pos);
				match(t);
				return r;
			}
		}
		return null;
	}
	
	private ASTExpressionNode expression()
	{
		int previous=store();
		ASTExpressionNode left=simpleExpression();
		if(null!=left)
		{
			Record r=relationalOperator();
			if(null!=r)
			{
				ASTExpressionNode right=simpleExpression();
				if(null!=right)
				{
					return new ASTExpression(left, right, r);
				}
				syntaxError();
				return null;
			}
			return left;
		}
		back(previous);
		return null;
	}
	
	private ASTExpressionNode index()
	{
		return expression();
	}
	
	private ASTIndexedVariable indexedVariable()
	{
		int previous=store();
		Record r=records.get(pos);
		String name=variableName();
		if(null!=name && lookahead.is(TSToken.SLBRACKET))
		{
			match(TSToken.SLBRACKET);
			ASTExpressionNode index=index();
			if(null!=index && lookahead.is(TSToken.SRBRACKET))
			{
				match(TSToken.SRBRACKET);
				return new ASTIndexedVariable(name, index, r);
			}
			syntaxError();
		}
		back(previous);
		return null;
	}

	private ASTVariable variable()
	{
		ASTIndexedVariable indexedVariable=indexedVariable();
		if(null!=indexedVariable)
		{
			return indexedVariable;
		}

		ASTPureVariable pureVariable=pureVariable();
		if(null!=pureVariable)
		{
			return pureVariable;
		}
		return null;
	}
	
	private ASTVariable leftHandSide()
	{
		return variable();
	}
	
	private ASTAssignmentStatement assignmentStatement()
	{
		int previous=store();
		Record r=records.get(pos);
		ASTVariable variable=leftHandSide();
		if(null!=variable && lookahead.is(TSToken.SASSIGN))
		{
			match(TSToken.SASSIGN);
			ASTExpressionNode expression=expression();
			if(null!=expression)
			{
				return new ASTAssignmentStatement(variable, expression, r);
			}
			syntaxError();
			return null;
		}
		back(previous);
		return null;
	}
	
	private ArrayList<ASTExpressionNode> expressionList()
	{
		ASTExpressionNode expression;
		ArrayList<ASTExpressionNode> expressions=new ArrayList<>();

		do
		{
			expression=expression();
			if(null!=expression)
			{
				expressions.add(expression);
				continue;
			}
			syntaxError();
			return null;
		}
		while(lookahead.is(TSToken.SCOMMA) && match(TSToken.SCOMMA));

		return expressions;
	}
	
	private ASTProcedureCallStatement procedureCallStatement()
	{
		Record r=records.get(pos);
		String name=procedureName();
		if(null!=name)
		{
			if(lookahead.is(TSToken.SLPAREN))
			{
				match(TSToken.SLPAREN);
				ArrayList<ASTExpressionNode> expressions=expressionList();
				if(lookahead.is(TSToken.SRPAREN))
				{
					match(TSToken.SRPAREN);
					return new ASTProcedureCallStatement(name, expressions, r);
				}
				syntaxError();
				return null;
			}
			return new ASTProcedureCallStatement(name, null, r);
		}
		return null;
	}
	
	private ArrayList<ASTVariable> variableList()
	{
		ArrayList<ASTVariable> variables=new ArrayList<>();
		do
		{
			ASTVariable variable=variable();
			if(null==variable)
			{
				syntaxError();
				return null;
			}
			variables.add(variable);
		}
		while(lookahead.is(TSToken.SCOMMA) && match(TSToken.SCOMMA));
		
		return variables;
	}
	
	private ASTStatement IOStatement()
	{
		Record r=records.get(pos);
		if(lookahead.is(TSToken.SREADLN))
		{
			match(TSToken.SREADLN);
			if(lookahead.is(TSToken.SLPAREN))
			{
				match(TSToken.SLPAREN);
				ArrayList<ASTVariable> variables=variableList();
				if(lookahead.is(TSToken.SRPAREN))
				{
					match(TSToken.SRPAREN);
					return new ASTInputStatement(variables, r);
				}
				syntaxError();
				return null;
			}
			return new ASTInputStatement(null, r);
		}
		else if(lookahead.is(TSToken.SWRITELN))
		{
			match(TSToken.SWRITELN);
			if(lookahead.is(TSToken.SLPAREN))
			{
				match(TSToken.SLPAREN);
				ArrayList<ASTExpressionNode> expressions=expressionList();
				if(lookahead.is(TSToken.SRPAREN))
				{
					match(TSToken.SRPAREN);
					return new ASTOutputStatement(expressions, r);
				}
				syntaxError();
				return null;
			}
			return new ASTOutputStatement(null, r);
		}
		return null;
	}
	
	private ASTStatement basicStatement()
	{
		ASTStatement statement=IOStatement();
		if(null!=statement)
		{
			return statement;
		}
		
		statement=compoundStatement();
		if(null!=statement)
		{
			return statement;
		}
		
		statement=assignmentStatement();
		if(null!=statement)
		{
			return statement;
		}
		
		statement=procedureCallStatement();
		if(null!=statement)
		{
			return statement;
		}
		
		return null;
	}
	
	private ASTStatement statement()
	{
		Record r=records.get(pos);
		ASTStatement statement=basicStatement();
		if(null!=statement)
		{
			return statement;
		}
		
		if(lookahead.is(TSToken.SIF))
		{
			match(TSToken.SIF);
			ASTExpressionNode condition=expression();
			if(null!=condition && lookahead.is(TSToken.STHEN))
			{
				match(TSToken.STHEN);
				ASTCompoundStatement thenStatement=compoundStatement();

				if(null!=thenStatement)
				{
					if(lookahead.is(TSToken.SELSE))
					{
						match(TSToken.SELSE);
						ASTCompoundStatement elseStatement=compoundStatement();
						if(null!=elseStatement)
						{
							return new ASTIfThenElseStatement(condition, thenStatement, elseStatement, r);
						}
						syntaxError();
						return null;
					}
					return new ASTIfThenStatement(condition, thenStatement, r);
				}
			}
			syntaxError();
			return null;
		}
		
		if(lookahead.is(TSToken.SWHILE))
		{
			match(TSToken.SWHILE);
			ASTExpressionNode condition=expression();
			if(null!=condition && lookahead.is(TSToken.SDO))
			{
				match(TSToken.SDO);
				ASTStatement doStatement=statement();
				if(null!=doStatement)
				{
					return new ASTWhileDoStatement(condition, doStatement, r);
				}
			}
			syntaxError();
		}
		return null;
	}
	
	private ArrayList<ASTStatement> statementList()
	{
		ArrayList<ASTStatement> statements=new ArrayList<>();
		ASTStatement statement=statement();
		if(null!=statement)
		{
			do
			{
				statements.add(statement);
				if(!lookahead.is(TSToken.SSEMICOLON))
				{
					return statements;
				}
				match(TSToken.SSEMICOLON);
			}
			while(null!=(statement=statement()));
		}
		syntaxError();
		return null;
	}
	
	private ASTCompoundStatement compoundStatement()
	{
		if(lookahead.is(TSToken.SBEGIN))
		{
			Record r=records.get(pos);
			match(TSToken.SBEGIN);
			ArrayList<ASTStatement> statements=statementList();
			if(null!=statements && lookahead.is(TSToken.SEND))
			{
				match(TSToken.SEND);
				return new ASTCompoundStatement(statements, r);
			}
			syntaxError();
		}
		return null;
	}
	
	private ASTSubprogramDeclaration subprogramDeclaration()
	{
		ASTSubprogramDeclaration subprogramDeclaration=subprogramHead();
		if(null!=subprogramDeclaration)
		{
			ArrayList<ASTVariableDeclaration> variableDeclaration=variableDeclaration();
			ASTCompoundStatement compoundStatement=compoundStatement();
			if(null!=compoundStatement)
			{
				subprogramDeclaration.set(variableDeclaration, compoundStatement);
				return subprogramDeclaration;
			}
			syntaxError();
		}
		return null;
	}
	
	private ArrayList<ASTSubprogramDeclaration> subprogramDeclarations()
	{
		ASTSubprogramDeclaration s;
		ArrayList<ASTSubprogramDeclaration> subprogramDeclarations=new ArrayList<>();
		
		while(null!=(s=subprogramDeclaration()))
		{
			subprogramDeclarations.add(s);
			if(!lookahead.is(TSToken.SSEMICOLON))
			{
				syntaxError();
				return null;
			}
			match(TSToken.SSEMICOLON);
		}
		return subprogramDeclarations;
	}
	
	private ASTBlock block()
	{
		Record r=records.get(pos);
		return new ASTBlock(variableDeclaration(), subprogramDeclarations(), r);
	}
	
	private String programName()
	{
		return name();
	}
	
	private ArrayList<String> nameList()
	{
		String s=name();
		ArrayList<String> r;
		
		if(null!=s)
		{
			r=new ArrayList<>();
			do
			{
				r.add(s);
				if(!lookahead.is(TSToken.SCOMMA))
				{
					return r;
				}
				match(TSToken.SCOMMA);
			}
			while(null!=(s=name()));
			syntaxError();
		}
		
		return null;
	}
	
	private ASTProgram program()
	{
		if(lookahead.is(TSToken.SPROGRAM))
		{
			Record r=records.get(pos);
			match(TSToken.SPROGRAM);
			String name=programName();
			if(null!=name && lookahead.is(TSToken.SLPAREN))
			{
				match(TSToken.SLPAREN);
				ArrayList<String> names=nameList();
				if(null!=names && lookahead.is(TSToken.SRPAREN))
				{
					match(TSToken.SRPAREN);
					if(lookahead.is(TSToken.SSEMICOLON))
					{
						match(TSToken.SSEMICOLON);
						ASTBlock block=block();
						//if(null!=block)
						//{
							ASTCompoundStatement compoundStatement=compoundStatement();
							if(null!=compoundStatement && lookahead.is(TSToken.SDOT))
							{
								match(TSToken.SDOT);
								return new ASTProgram(name, names, block, compoundStatement, r);
							}
						//}
					}
				}
			}
		}
		syntaxError();
		return null;
	}

	public ASTProgram getAST()
	{
		return root;
	}

	public boolean success()
	{
		return null!=root && !fail;
	}

	public void printOK()
	{
		System.out.println("OK");
	}

	public void run(final String inputFileName)
	{
		String line;
		String [] list;
				
		try
		{
			File in=new File(inputFileName);
			
			FileReader fr=new FileReader(in);
			BufferedReader br=new BufferedReader(fr);
			
			while((line=br.readLine())!=null)
			{
				list=line.split("\t");
				TSToken[] t=TSToken.values();
				int i;
				for(i=0; i<t.length; ++i)
				{
					if(t[i].toString().equals(list[1]))
					{
						records.add(new Record(t[i], list[0], Integer.parseInt(list[3])));
						break;
					}
				}
				//System.out.println("  |"+list[0]+"\t"+t[i].toString()+"\t"+list[3]);
			}
			
			pos=0;
			lookahead=records.get(0);
			
			root=program();

			if(null==root)
			{
				syntaxError();
			}
			
			br.close();
			fr.close();
		}
		catch(FileNotFoundException e)
		{
			System.err.println("File not found");
		}
		catch(IOException e)
		{
			System.err.println(e);
		}

	}
}
