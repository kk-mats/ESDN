package enshud.s4.compiler;

import java.util.ArrayList;
import java.util.Arrays;

public class CASLCode
{
	public class Label
	{
		private String name;
		private int index;

		public Label(final String name, final int index)
		{
			this.name=name;
			this.index=index;
		}
	}

	public class CASLRecord
	{
		private String label;
		private CASLInst inst;
		private ArrayList<String> operand;

		public CASLRecord(final String label, final CASLInst inst, final String [] operand)
		{
			this.label=label;
			this.inst=inst;
			this.operand=new ArrayList<>(Arrays.asList(operand));
		}
		public String toString()
		{
			String s=label!=null ? label+" " : "";
			s+=inst.toString();
			s+=" ";
			s+=String.join(", ", operand);
			return s;
		}
	}

	private ArrayList<Label> label=new ArrayList<>();
	private ArrayList<CASLRecord> lines=new ArrayList<>();

	public void addLabel(final String name, final int index)
	{
		label.add(new Label(name, index));
	}

	public void addLine(final String label, final CASLInst inst, final String [] operand)
	{
		lines.add(new CASLRecord(label, inst, operand));
	}

	public String toString()
	{
		return lines.stream().sequential().reduce("", (joined, r)->joined+r.toString()+"\n", (joined, r)->null);
	}

}
