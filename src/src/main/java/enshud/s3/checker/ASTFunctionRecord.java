package enshud.s3.checker;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ASTFunctionRecord
{
	private String name="";
	private ASTVariableTable parameters=new ASTVariableTable(); // parameters of this function
	private ASTVariableTable declaredVariables=new ASTVariableTable(); // variables declared in this function
	private ASTVariableTable usedGlobalVariables=new ASTVariableTable(); // global variables used in this function
	private ArrayList<AbstractMap.SimpleEntry<String, String>> globalAndLocalVariableCorrespondence=new ArrayList<>();

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

	public void addGlobalAndLocalVariableCorrespondence(final String global, final String local)
	{
		globalAndLocalVariableCorrespondence.add(new AbstractMap.SimpleEntry<>(global, local));
	}

	public String getLocalVariableBy(final String global)
	{
		return globalAndLocalVariableCorrespondence.stream().filter(p->p.getKey().equals(global)).map(AbstractMap.SimpleEntry::getValue).findFirst().orElse(global);
	}

	public boolean hasGlobalVariableCorrespondenceOf(final String local)
	{
		return globalAndLocalVariableCorrespondence.stream().anyMatch(p->p.getValue().equals(local));
	}
	
	public boolean hasLocalVariableCorrespondenceOf(final String global)
	{
		return globalAndLocalVariableCorrespondence.stream().anyMatch(p->p.getKey().equals(global));
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
		s+="GLCores :\n\t"+String.join(",\n\t", globalAndLocalVariableCorrespondence.stream().map(AbstractMap.SimpleEntry::toString).collect(Collectors.toList()))+"\n";
		return s;
	}
}

