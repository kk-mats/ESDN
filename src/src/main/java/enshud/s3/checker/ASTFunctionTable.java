package enshud.s3.checker;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class ASTFunctionTable
{
	private ASTFunctionRecord global;
	private ArrayList<ASTFunctionRecord> subprogram;

	public ASTFunctionTable()
	{
		global=new ASTFunctionRecord();
		subprogram=new ArrayList<>();
	}

	public boolean setGlobalName(final String name)
	{
		return global.setName(name);
	}

	public boolean addGlobalParameter(final String name, final Record record)
	{
		return global.addParameter(name, new ASTVariableType(record));
	}

	public boolean addGlobalVariable(final ASTVariableDeclaration v)
	{
		if(global.findBy(v.getNames()))
		{
			return false;
		}

		for(String s:v.getNames())
		{
			if(!global.addLocalVariable(s, v.getType()))
			{
				return false;
			}
		}
		return true;
	}

	public boolean usedFunctionName(final String name)
	{
		return global.getLocalVariables().findBy(name);
	}

	public void addRecord(final ASTFunctionRecord r)
	{
		subprogram.add(r);
	}

	public ASTFunctionRecord getGlobal()
	{
		return global;
	}

	public ArrayList<ASTFunctionRecord> getSubprogram()
	{
		return subprogram;
	}

	public ASTVariableType getVariableType(final String name)
	{
		ASTVariableTable.ASTVariableRecord r;
		if(subprogram.size()>0)
		{
			r=subprogram.get(subprogram.size()-1).getRecordOf(name);
			if(null!=r)
			{
				return r.getVariableType();
			}
		}
		return (r=global.getRecordOf(name))!=null ? r.getVariableType() : null;
	}

	public String getScope(final ArrayDeque<String> scope, final String name)
	{
		ASTFunctionRecord r=subprogram.stream().filter(s->(s.getName().equals(scope.getLast()) && s.findBy(name))).findAny().orElse(null);
		return  r!=null ? scope.stream().reduce("", (joined, s)->joined+s+".")+name : global.getName()+"."+name;
	}

	public ASTVariableTable getFunctionParameter(final String name)
	{
		ASTFunctionRecord r=subprogram.stream().filter(s->s.getName().equals(name)).findAny().get();
		return r!=null ? r.getParameters() : null;
	}
}
