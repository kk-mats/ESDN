package enshud.s3.checker;

import java.util.ArrayList;

public class ASTFunctionRecord
{
	private String name;
	private ASTVariableTable parameters;
	private ASTVariableTable localVariables;

	ASTFunctionRecord()
	{
		name="";
		parameters=new ASTVariableTable();
		localVariables=new ASTVariableTable();
	}

	public boolean findBy(final String name)
	{
		return this.name.equals(name) || parameters.findBy(name) || localVariables.findBy(name);
	}

	public boolean findBy(final ArrayList<String> names)
	{
		for(String s:names)
		{
			if(findBy(s))
			{
				return true;
			}
		}
		return false;
	}

	public ASTVariableTable.ASTVariableRecord getRecordOf(final String name)
	{
		ASTVariableTable.ASTVariableRecord r=localVariables.getRecordOf(name);
		if(null!=r)
		{
			return r;
		}
		return parameters.getRecordOf(name);
	}

	public boolean setName(final String name)
	{
		if(findBy(name))
		{
			return false;
		}

		this.name=name;
		return true;
	}

	public boolean addParameter(final String name, final ASTVariableType type)
	{
		if(findBy(name))
		{
			return false;
		}
		parameters.add(name, type);
		return true;
	}

	public boolean addLocalVariable(final String name, final ASTVariableType type)
	{
		if(findBy(name))
		{
			return false;
		}
		localVariables.add(name, type);
		return true;
	}

	public String getName()
	{
		return name;
	}

	public ASTVariableTable getParameters()
	{
		return parameters;
	}

	public ASTVariableTable getLocalVariables()
	{
		return localVariables;
	}
}

