package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;

import java.util.ArrayList;

public class CASL
{
	public enum Inst
	{
		LD, ST, LAD,
		ADDA, ADDL, SUBA, SUBL, AND, OR, XOR, CPA, CPL, SLA, SRA, SLL, SRL,
		JPL, JMI, JNZ, JZE, JOV, JUMP,
		PUSH, POP, RPUSH, RPOP,
		CALL, RET,
		SVC, NOP,
		START, END, DS, DC, IN, OUT;

		public static Inst of(final TSToken token)
		{
			switch(token)
			{
				case SPLUS:return ADDA;
				case SMINUS:return SUBA;
				case SOR:return OR;
				case SAND:return AND;
			}
			return null;
		}

		public boolean isJump()
		{
			return this==JPL || this==JMI || this==JNZ || this==JZE || this==JOV || this==JUMP;
		}
	}

	public static class Code
	{
		private String label="";
		private Inst inst;
		private String [] operand;

		public Code(final Inst inst, final String [] operand)
		{
			this.inst=inst;
			this.operand=operand;
		}

		public Code(final String label, final Inst inst, final String [] operand)
		{
			this.label=label;
			this.inst=inst;
			this.operand=operand;
		}

		public String toString()
		{
			String s=label!=null ? label+(label.length()<4 ? "\t\t" : "\t") : "\t\t";
			s+=inst.toString()+(operand.length>0 ? inst.toString().length()<4 ? "\t\t" : "\t" : "");
			s+=String.join(", ", operand);
			return s;
		}

		public String getLabel()
		{
			return label;
		}

		public Inst getInst()
		{
			return inst;
		}

		public String[] getOperand()
		{
			return operand;
		}
	}

	private ArrayList<Code> main=new ArrayList<>();
	private ArrayList<Code> storage=new ArrayList<>();
	private ArrayList<Code> constant=new ArrayList<>();

	public void addCode(final Inst inst)
	{
		main.add(new Code(inst, new String[]{}));
	}

	public void addCode(final Inst inst, final String r)
	{
		main.add(new Code(inst, new String[]{r}));
	}

	public void addCode(final Inst inst, final String r1, final String r2)
	{
		main.add(new Code(inst, new String[]{r1, r2}));
	}

	public void addCode(final Inst inst, final String r, final String adr, final String x)
	{
		main.add(new Code(inst, new String[]{r, adr, x}));
	}

	public void addCode(final String label)
	{
		main.add(new Code(label, Inst.NOP, new String[]{}));
	}

	public void addCode(final String label, final Inst inst)
	{
		main.add(new Code(label, inst, new String[]{}));
	}

	public void addCode(final String label, final Inst inst, final String r)
	{
		main.add(new Code(label, inst, new String[]{r}));
	}
	public void addCode(final String label, final Inst inst, final String r1, final String r2)
	{
		main.add(new Code(label, inst, new String[]{r1, r2}));
	}
	public void addCode(final String label, final Inst inst, final String r, final String adr, final String x)
	{
		main.add(new Code(label, inst, new String[]{r, adr, x}));
	}

	public void addStorage(final String label, final int n)
	{
		storage.add(new Code(label, Inst.DS, new String[]{String.valueOf(n)}));
	}

	public void addConstant(final String label, final String s)
	{
		constant.add(new Code(label, Inst.DC, new String[]{s}));
	}

	public ArrayList<Code> getMain()
	{
		return main;
	}

	public ArrayList<Code> getCodeSet()
	{
		ArrayList<Code> c=main;
		c.add(new Code("@M_END", Inst.RET, new String[]{}));
		c.addAll(storage);
		c.addAll(constant);
		c.add(new Code(Inst.END, new String[]{}));
		return c;
	}

	public String toString()
	{
		return getCodeSet().stream().map(Code::toString).reduce("", (joined, s)->joined+s+"\n");
	}

	public int size()
	{
		return main.size();
	}

}
