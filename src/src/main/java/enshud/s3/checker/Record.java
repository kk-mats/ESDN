package enshud.s3.checker;

import enshud.s1.lexer.TSToken;

public class Record
{
	private TSToken TSToken;
	private String text;
	private int lineNumber;
	
	public Record(TSToken TSToken, String text, int lineNumber)
	{
		this.TSToken=TSToken;
		this.text=text;
		this.lineNumber=lineNumber;
	}
	
	public boolean is(TSToken TSToken)
	{
		return this.TSToken==TSToken;
	}

	public TSToken getTSToken()
	{
		return TSToken;
	}

	public String getText()
	{
		return text;
	}

	public String toString()
	{
		return text;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}
}
