package enshud.s3.checker;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;

public class ASTSymbolTable
{
	private ArrayList<ASTFunctionRecord> table=new ArrayList<>();
	private ArrayList<AbstractMap.SimpleEntry<String, String>> labelAliasTable=new ArrayList<>();

	public ASTSymbolTable()
	{
		table.add(new ASTFunctionRecord());
	}

	public boolean setGlobalFunctionName(final String name)
	{
		return table.get(0).setName(name);
	}

	public boolean addGlobalFunctionParameter(final String name, final Record record)
	{
		return table.get(0).addParameter(name, new ASTVariableType(record));
	}

	public boolean addDeclaredVariableOfGlobalFunction(final ASTVariableDeclaration v)
	{
		if(table.get(0).findBy(v.getNames()))
		{
			return false;
		}

		for(String s:v.getNames())
		{
			if(!table.get(0).addDeclaredVariable(s, v.getType()))
			{
				return false;
			}
		}
		return true;
	}

	public boolean isUsedFunctionName(final String name)
	{
		return table.get(0).getUsedGlobalVariables().findBy(name);
	}

	public void addRecord(final ASTFunctionRecord r)
	{
		table.add(r);
	}

	public ASTFunctionRecord getGlobalFunction()
	{
		return table.get(0);
	}

	public ArrayList<ASTFunctionRecord> getTable()
	{
		return table;
	}

	public ASTVariableType getVariableType(final String name)
	{
		ASTVariableTable.ASTVariableRecord r;
		if(table.size()>1)
		{
			r=table.get(table.size()-1).getRecordOf(name);
			if(null!=r)
			{
				return r.getVariableType();
			}
		}
		return (r=table.get(0).getRecordOf(name))!=null ? r.getVariableType() : null;
	}

	public String getScope(final ArrayDeque<String> scope, final String variableName)
	{
		ASTFunctionRecord r=table.stream().filter(s->(s.getName().equals(scope.getLast()) && s.findBy(variableName))).findAny().orElse(null);
		String sn;
		if(r!=null)
		{
			// local variable
			sn=scope.stream().reduce("", (joined, s)->joined+s+".")+variableName;
		}
		else
		{
			// global variable
			sn=table.get(0).getName()+"."+variableName;
			table.stream().filter(f->f.getName().equals(scope.getLast()))
					.findFirst().ifPresent(e->{
				e.addUsedGlobalVariable(sn, getVariableType(variableName));
				e.addGlobalAndLocalVariableCorrespondence(scope.getFirst()+"."+variableName, String.join(".", scope)+"."+variableName);
				e.addDeclaredVariable(variableName, getVariableType(variableName));
			});
		}
		return sn;
	}

	public ASTVariableTable getGlobalVariableUsedIn(final String functionName)
	{
		return table.stream().filter(f->f.getName().equals(functionName)).findFirst().get().getUsedGlobalVariables();
	}

	public String getLocalVariableOfGlobalVariableIn(final String functionName, final String global)
	{
		return table.stream().filter(f->f.getName().equals(functionName)).findFirst().get().getLocalVariableBy(global);
	}

	public boolean hasGlobalVariableCorrespondenceOf(final String local)
	{
		return table.stream().map(f->f.hasGlobalVariableCorrespondenceOf(local)).reduce(false, (before, b)->Boolean.logicalOr(before, b));
	}

	public ASTVariableTable getFunctionParameter(final String name)
	{
		Optional<ASTFunctionRecord> r=table.stream().filter(s->s.getName().equals(name)).findAny();
		return r.isPresent() ? r.get().getParameters() : null;
	}

	public void registerInvalidLabel(final String label)
	{
		if(labelAliasTable.stream().noneMatch(p->p.getKey().equals(label)))
		{
			String newLabel="STL"+labelAliasTable.size();
			labelAliasTable.add(new AbstractMap.SimpleEntry<>(label, newLabel));
		}
	}

	// if label is invalid label name, return new label name.
	// if label is valid label name, return label
	public String getLabelAlias(final String label)
	{
		//return label;
		return labelAliasTable.stream().filter(p->p.getKey().equals(label)).map(AbstractMap.SimpleEntry::getValue).findFirst().orElse(label);
	}

	public String toString()
	{
		String str="Main "+table.stream().map(ASTFunctionRecord::toString).reduce("", (joined, s)->joined+s+"\n");
		str+="Label Alias\n"+labelAliasTable.stream().map(AbstractMap.SimpleEntry::toString).reduce("", (joined, p)->joined+p+"\n")+"\n";
		return str;
	}
}
