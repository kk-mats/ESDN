package enshud.s3.checker;

import enshud.s1.lexer.TSToken;

public class ASTFactor extends ASTExpressionNode
{
	private ASTVariable variable=null;
	private ASTExpressionNode expression=null;
	private ASTFactor notFactor=null;
	private Integer cinteger=null;
	private Boolean cboolean=null;
	private String cstring=null;

	public ASTFactor(final ASTVariable variable, final Record record)
	{
		super(record);
		this.variable=variable;
	}

	public ASTFactor(final ASTExpressionNode expression, final Record record)
	{
		super(record);
		this.expression=expression;
	}

	public ASTFactor(final ASTFactor notFactor, final Record record)
	{
		super(record);
		this.notFactor=notFactor;
	}

	public ASTFactor(final Record record)
	{
		super(record);
		switch(record.getTSToken())
		{
			case SCONSTANT:
			{
				cinteger=Integer.parseInt(record.getText());
				evalType=ASTEvalType.tInteger;
				break;
			}

			case STRUE:
			{
				cboolean=true;
				evalType=ASTEvalType.tBoolean;
				break;
			}

			case SFALSE:
			{
				cboolean=false;
				evalType=ASTEvalType.tBoolean;
				break;
			}

			default:
			{
				cstring=record.getText().substring(1, record.getText().length()-1);
				evalType=cstring.length()>1 ? ASTEvalType.tString : ASTEvalType.tChar;
				break;
			}
		}
	}

	public ASTVariable getVariable()
	{
		return variable;
	}

	public ASTExpressionNode getExpression()
	{
		return expression;
	}

	public ASTFactor getNotFactor()
	{
		return notFactor;
	}

	public void accept(final ASTVisitor visitor) throws ASTException
	{
		try
		{
			visitor.visit(this);
		}
		catch (ASTException e)
		{
			throw e;
		}
	}
}
