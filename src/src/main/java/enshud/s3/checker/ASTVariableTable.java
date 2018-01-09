package enshud.s3.checker;

import java.util.ArrayList;

public class ASTVariableTable
{
	public class ASTVariableRecord
	{
		private String name;
		private ASTVariableType variableType;

		ASTVariableRecord(String name, ASTVariableType variableType)
		{
			this.name=name;
			this.variableType=variableType;
		}

		public String getName()
		{
			return name;
		}

		public ASTEvalType getEvalType()
		{
			return variableType.getEvalType();
		}

		public ASTVariableType getVariableType()
		{
			return variableType;
		}
	}

	private ArrayList<ASTVariableRecord> records;

	ASTVariableTable()
	{
		records=new ArrayList<>();
	}

	public int size()
	{
		return records.size();
	}

	public ASTVariableRecord get(final int i)
	{
		return records.get(i);
	}

	public boolean add(final String name, final ASTVariableType variableType)
	{
		for(ASTVariableRecord r:records)
		{
			if(r.getName().equals(name))
			{
				return false;
			}
		}
		records.add(new ASTVariableRecord(name, variableType));
		return true;
	}

	public boolean add(final ASTVariableDeclaration v)
	{
		for(String n:v.getNames())
		{
			for(ASTVariableRecord r:records)
			{
				if(r.getName().equals(n))
				{
					return false;
				}
			}
			records.add(new ASTVariableRecord(n, v.getType()));
		}
		return true;
	}

	public boolean findBy(final String name)
	{
		for(ASTVariableRecord r:records)
		{
			if(r.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}

	public ASTVariableRecord getRecordOf(final String name)
	{
		for(ASTVariableRecord r:records)
		{
			if(r.getName().equals(name))
			{
				return r;
			}
		}
		return null;
	}

	public ArrayList<ASTVariableRecord> getRecords()
	{
		return records;
	}
}
