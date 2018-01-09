package enshud.s3.checker;

public enum ASTEvalType
{
	tInteger, tBoolean, tChar, tIntegerArray, tBooleanArray, tString;

	public boolean isStandardType()
	{
		return this==tInteger || this==tBoolean || this==tChar;
	}

	public boolean isArrayType()
	{
		return !isStandardType();
	}

	public ASTEvalType toStandardType()
	{
		return this==tIntegerArray ? tInteger : this==tBooleanArray ? tBoolean : tChar;
	}

	public ASTEvalType toArrayType()
	{
		return this==tInteger ? tIntegerArray : this==tBoolean ? tBooleanArray : tString;
	}
}
