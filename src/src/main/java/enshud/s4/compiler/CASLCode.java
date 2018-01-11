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
			String s=label!=null ? label+(label.length()>3 ? "\t" : "\t\t") : "\t\t";
			s+=inst.toString()+(inst.toString().length()<4 ? "\t\t" : "\t");
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

	public void addLine(final String label, final CASLInst inst)
	{
		lines.add(new CASLRecord(label, inst, new String[]{}));
	}

	public void addLine(final String label, final CASLInst inst, final String r)
	{
		lines.add(new CASLRecord(label, inst, new String[]{r}));
	}
	public void addLine(final String label, final CASLInst inst, final String r1, final String r2)
	{
		lines.add(new CASLRecord(label, inst, new String[]{r1, r2}));
	}
	public void addLine(final String label, final CASLInst inst, final String r, final String adr, final String x)
	{
		lines.add(new CASLRecord(label, inst, new String[]{r, adr, x}));
	}

	public String toString()
	{
		return lines.stream().sequential().reduce("", (joined, r)->joined+r.toString()+"\n", (joined, r)->null);
	}

	public int size()
	{
		return lines.size();
	}

}
