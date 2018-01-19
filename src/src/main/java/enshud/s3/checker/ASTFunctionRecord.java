package enshud.s3.checker;

import java.util.ArrayList;

public class ASTFunctionRecord
{
	private String name="";
	private ASTVariableTable parameters=new ASTVariableTable(); // parameters of this function
	private ASTVariableTable declaredVariables=new ASTVariableTable(); // variables declared in this function
	private ASTVariableTable usedGlobalVariables=new ASTVariableTable(); // global variables used in this function

	public boolean findBy(final String name)
	{
		return this.name.equals(name) || parameters.findBy(name) || declaredVariables.findBy(name);
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
		ASTVariableTable.ASTVariableRecord r=declaredVariables.getRecordOf(name);
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

	public boolean addDeclaredVariable(final String name, final ASTVariableType type)
	{
		if(findBy(name))
		{
			return false;
		}
		declaredVariables.add(name, type);
		return true;
	}

	public boolean addUsedGlobalVariable(final String name, final ASTVariableType type)
	{
		usedGlobalVariables.add(name, type);
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

	public ASTVariableTable getDeclaredVariables()
	{
		return declaredVariables;
	}

	public ASTVariableTable getUsedGlobalVariables()
	{
		return usedGlobalVariables;
	}

	public String toString()
	{
		String s="function "+name+"\n";
		s+="parameter :\n"+parameters.toString()+"\n";
		s+="declared variable :\n"+declaredVariables.toString()+"\n";
		s+="used global variable :\n"+usedGlobalVariables.toString()+"\n";
		return s;
	}
}

