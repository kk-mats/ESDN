package enshud.s2.parser;

import enshud.s1.lexer.TSToken;
import enshud.s3.checker.ASTConstructor;

import java.util.ArrayList;

public class Parser {
	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// normalの確認
		//new Parser().run("data/ts/normal01.ts");
		new Parser().run("data/ts/normal03.ts");

		// synerrの確認
		//new Parser().run("data/ts/synerr01.ts");
		//new Parser().run("data/ts/synerr07.ts");
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるParser実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，構文解析を行う．
	 * 構文が正しい場合は標準出力に"OK"を，正しくない場合は"Syntax error: line"という文字列とともに，
	 * 最初のエラーを見つけた行の番号を標準エラーに出力すること （例: "Syntax error: line 1"）．
	 * 入力ファイル内に複数のエラーが含まれる場合は，最初に見つけたエラーのみを出力すること．
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力tsファイル名
	 */
	
	int current;
	boolean fail=false;
	
	ArrayList<TSToken> TSTokens;
	ArrayList<String> strings;
	ArrayList<Integer> lineNumber;
	
	ASTConstructor astc;
	
	private boolean read()
	{
		if(current<TSTokens.size())
		{
			++current;
			return true;
		}
		return false;
	}
	
	private void syntaxError()
	{
		if(!fail)
		{
			System.err.println("Syntax error: line "+lineNumber.get(current));
			//System.out.println(TSTokens.get(current).label());
			fail=true;
		}
	}
	
	private int store()
	{
		return current;
	}
	
	private void restore(int previous)
	{
		current=previous;
	}
	
	private boolean currentIs(TSToken TSToken)
	{
		return TSTokens.get(current)==TSToken ? read() : false;
	}
	
	private boolean name()
	{
		return currentIs(TSToken.SIDENTIFIER);
	}
	
	private boolean standardType()
	{
		return currentIs(TSToken.SINTEGER) || currentIs(TSToken.SCHAR) || currentIs(TSToken.SBOOLEAN);
	}
	
	private boolean natural()
	{
		return currentIs(TSToken.SCONSTANT);
	}
	
	private boolean sign()
	{
		return currentIs(TSToken.SPLUS) || currentIs(TSToken.SMINUS);
	}
	
	private boolean integer()
	{
		sign();
		if(natural())
		{
			return true;
		}
		return false;
	}
	
	private boolean minOfIndex()
	{
		return integer();
	}
	
	private boolean maxOfIndex()
	{
		return integer();
	}
	
	private boolean arrayType()
	{
		int previous=store();
		if(currentIs(TSToken.SARRAY))
		{
			if(currentIs(TSToken.SLBRACKET) &&
			minOfIndex() &&
			currentIs(TSToken.SRANGE) &&
			maxOfIndex() &&
			currentIs(TSToken.SRBRACKET) &&
			currentIs(TSToken.SOF) &&
			standardType())
			{
				return true;
			}
			syntaxError();
			restore(previous);
			return false;
		}
		return false;
	}
	
	private boolean type()
	{
		if(standardType() || arrayType())
		{
			return true;
		}
		syntaxError();
		return false;
	}
	
	private boolean variableName()
	{
		return name();
	}
	
	private boolean arrayOfVariableNames()
	{
		if(variableName())
		{
			while(currentIs(TSToken.SCOMMA))
			{
				if(!variableName())
				{
					syntaxError();
					return false;
				}
			}
			
			return true;
		}
		return false;
	}
	
	private boolean arrayOfVariableDeclarations()
	{
		if(arrayOfVariableNames())
		{
			do
			{
				if(!(currentIs(TSToken.SCOLON) && type() && currentIs(TSToken.SSEMICOLON)))
				{
					syntaxError();
					return false;
				}
				
				int previous=store();
				if(!variableName())
				{
					break;
				}
				restore(previous);
			}
			while(arrayOfVariableNames());
			return true;
		}
		syntaxError();
		return false;
	}
	
	private boolean variableDeclaration()
	{
		if(currentIs(TSToken.SVAR))
		{
			if(arrayOfVariableDeclarations())
			{
				return true;
			}

			syntaxError();
			return false;
		}
		return true;
	}
	
	private boolean procedureName()
	{
		return name();
	}
	
	private boolean parameterName()
	{
		return name();
	}
	
	private boolean arrayOfParameterNames()
	{
		if(parameterName())
		{
			do
			{
				if(!currentIs(TSToken.SCOMMA))
				{
					return true;
				}
			}
			while(parameterName());
		}
		return false;
	}
	
	private boolean arrayOfParameters()
	{
		if(arrayOfParameterNames() && currentIs(TSToken.SCOLON) && standardType())
		{
			do
			{
				if(!currentIs(TSToken.SSEMICOLON))
				{
					return true;
				}
			}
			while(arrayOfParameterNames() && currentIs(TSToken.SCOLON) && standardType());
		}
		syntaxError();
		return false;
	}
	
	private boolean parameter()
	{
		if(currentIs(TSToken.SLPAREN))
		{
			if(!(arrayOfParameters() && currentIs(TSToken.SRPAREN)))
			{
				syntaxError();
				return false;
			}
		}
		return true;
	}
	
	private boolean subprogramHead()
	{
		int previous=store();
		if(currentIs(TSToken.SPROCEDURE))
		{
			if(procedureName() && parameter() && currentIs(TSToken.SSEMICOLON))
			{
				return true;
			}
			syntaxError();
		}
		restore(previous);
		return false;
	}
	
	private boolean pureVariable()
	{
		return variableName();
	}
	
	private boolean constant()
	{
		return natural() || currentIs(TSToken.SSTRING) || currentIs(TSToken.SFALSE) || currentIs(TSToken.STRUE);
	}
	
	private boolean factor()
	{
		int previous=store();
		if(variable())
		{
			return true;
		}
		restore(previous);

		if(constant())
		{
			return true;
		}
		restore(previous);

		if(currentIs(TSToken.SLPAREN) && expression() && currentIs(TSToken.SRPAREN))
		{
			return true;
		}
		restore(previous);

		if(currentIs(TSToken.SNOT) && factor())
		{
			return true;
		}
		syntaxError();
		return false;
	}
	
	private boolean additiveOperator()
	{
		return currentIs(TSToken.SPLUS) || currentIs(TSToken.SMINUS) || currentIs(TSToken.SOR);
	}
	
	private boolean multiplicativeOperator()
	{
		return currentIs(TSToken.SSTAR) || currentIs(TSToken.SDIVD) || currentIs(TSToken.SMOD) || currentIs(TSToken.SAND);
	}
	
	private boolean term()
	{
		while(factor())
		{
			if(!multiplicativeOperator())
			{
				return true;
			}
		}
		syntaxError();
		return false;
	}
	
	private boolean simpleExpression()
	{
		sign();
		
		while(term())
		{
			if(!additiveOperator())
			{
				return true;
			}
		}
		syntaxError();
		return false;
	}
	
	private boolean relationalOperator()
	{
		return currentIs(TSToken.SEQUAL) || currentIs(TSToken.SNOTEQUAL) ||
				currentIs(TSToken.SGREAT) || currentIs(TSToken.SGREATEQUAL) ||
				currentIs(TSToken.SLESS) || currentIs(TSToken.SLESSEQUAL);
	}
	
	private boolean expression()
	{
		while(simpleExpression())
		{
			if(!relationalOperator())
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean index()
	{
		return expression();
	}
	
	private boolean indexedVariable()
	{
		int previous=store();
		if(variableName() && currentIs(TSToken.SLBRACKET))
		{
			if(index() && currentIs(TSToken.SRBRACKET))
			{
				return true;
			}
			syntaxError();
		}
		restore(previous);
		return false;
	}

	private boolean variable()
	{
		return indexedVariable() || pureVariable();
	}
	
	private boolean leftHandSide()
	{
		return variable();
	}
	
	private boolean assignmentStatement()
	{
		if(leftHandSide())
		{
			if(currentIs(TSToken.SASSIGN) && expression())
			{
				return true;
			}
			syntaxError();
		}
		return false;
	}
	
	private boolean arrayOfExpressions()
	{
		if(expression())
		{
			do
			{
				if(!currentIs(TSToken.SCOMMA))
				{
					return true;
				}
			}
			while(expression());
			syntaxError();
		}
		return false;
	}
	
	private boolean procedureCallStatement()
	{
		if(procedureName())
		{
			if(currentIs(TSToken.SLPAREN))
			{
				if(arrayOfExpressions() && currentIs(TSToken.SRPAREN))
				{
					return true;
				}
				syntaxError();
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean IOStatement()
	{
		if(currentIs(TSToken.SREADLN))
		{
			if(currentIs(TSToken.SLPAREN))
			{
				if(arrayOfVariableNames() && currentIs(TSToken.SRPAREN))
				{
					return true;
				}
				syntaxError();
				return false;
			}
			return true;
		}
		else if(currentIs(TSToken.SWRITELN))
		{
			if(currentIs(TSToken.SLPAREN))
			{
				if(arrayOfExpressions() && currentIs(TSToken.SRPAREN))
				{
					return true;
				}
				syntaxError();
				return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean basicStatement()
	{
		if(IOStatement() || compoundStatement())
		{
			return true;
		}
		int previous=store();
		if(currentIs(TSToken.SIDENTIFIER))
		{
			if(currentIs(TSToken.SASSIGN) || currentIs(TSToken.SLBRACKET))
			{
				restore(previous);
				if(assignmentStatement())
				{
					return true;
				}
				syntaxError();
				return false;
			}
			restore(previous);
			if(procedureCallStatement())
			{
				return true;
			}
			syntaxError();
			return false;
		}
		
		restore(previous);
		return false;
	}
	
	private boolean statement()
	{
		if(basicStatement())
		{
			return true;
		}
		
		if(currentIs(TSToken.SIF))
		{
			if(expression() && currentIs(TSToken.STHEN) && compoundStatement())
			{
				if(currentIs(TSToken.SELSE))
				{
					if(compoundStatement())
					{
						return true;
					}
					syntaxError();
					return false;
				}
				return true;
			}
			syntaxError();
			return false;
		}
		
		if(currentIs(TSToken.SWHILE))
		{
			if(expression() && currentIs(TSToken.SDO) && statement())
			{
				return true;
			}
			syntaxError();
			return false;
		}
		return false;
	}
	
	private boolean arrayOfStatements()
	{
		if(statement())
		{
			do
			{
				if(!currentIs(TSToken.SSEMICOLON))
				{
					return true;
				}
			}
			while(statement());
		}
		syntaxError();
		return false;
	}
	
	private boolean compoundStatement()
	{
		if(currentIs(TSToken.SBEGIN))
		{
			if(arrayOfStatements() && currentIs(TSToken.SEND))
			{
				return true;
			}
			syntaxError();
		}
		return false;
	}
	
	private boolean subprogramDeclaration()
	{
		if(subprogramHead())
		{
			if(variableDeclaration() && compoundStatement())
			{
				return true;
			}
			syntaxError();
		}
		return false;
	}
	
	private boolean subprogramDeclarations()
	{
		int previous=store();
		while(subprogramDeclaration())
		{
			if(!currentIs(TSToken.SSEMICOLON))
			{
				restore(previous);
				return false;
			}
		}
		return true;
	}
	
	private boolean block()
	{
		if(variableDeclaration() && subprogramDeclarations())
		{
			return true;
		}
		syntaxError();
		return false;
	}
	
	private boolean programName()
	{
		return name();
	}
	
	private boolean arrayOfNames()
	{
		
		if(name())
		{
			do
			{
				if(!currentIs(TSToken.SCOMMA))
				{
					return true;
				}
			}
			while(name());
			syntaxError();
		}
		return false;
	}
	
	private boolean program()
	{
		if(currentIs(TSToken.SPROGRAM) && programName() &&
			currentIs(TSToken.SLPAREN) && arrayOfNames() && currentIs(TSToken.SRPAREN) &&
			currentIs(TSToken.SSEMICOLON) &&
			block() && compoundStatement() && currentIs(TSToken.SDOT))
		{
			return true;
		}
		syntaxError();
		return false;
	}
	
	public void run(final String inputFileName)
	{
		ASTConstructor c=new ASTConstructor(inputFileName);
		if(c.success())
		{
			c.printOK();
		}

		//System.out.println(c.getAST().toStringTree());
		
		/*
		current=0;
		String [] list;
		String line;
		TSTokens=new ArrayList<TSToken>();
		strings=new ArrayList<String>();
		lineNumber=new ArrayList<Integer>();
		
		
		try
		{
			File in=new File(inputFileName);
			
			FileReader fr=new FileReader(in);
			BufferedReader br=new BufferedReader(fr);
			
			while((line=br.readLine())!=null)
			{
				list=line.split("\t");
				TSToken [] t=TSToken.values();
				int i;
				for(i=0; i<t.length; ++i)
				{
					if(t[i].toString().equals(list[1]))
					{
						TSTokens.addCode(t[i]);
						break;
					}
				}
				strings.addCode(list[0]);
				lineNumber.addCode(Integer.parseInt(list[3]));
				//System.out.println("  |"+list[0]+"\t"+t[i].toString()+"\t"+list[3]);
			}
			
			if(program())
			{
				System.out.println("OK");
			}
			else
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
		*/
	}
}
