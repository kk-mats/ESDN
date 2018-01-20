package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

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

		public static boolean anyMatch(final String s)
		{
			return Arrays.stream(values()).anyMatch(e->e.name().equals(s));
		}

		public boolean isJump()
		{
			return this==JPL || this==JMI || this==JNZ || this==JZE || this==JOV || this==JUMP;
		}
	}

	public abstract class Operand{}

	public class RegisterOperand
	{
		private int r;
		public RegisterOperand(final int r)
		{
			this.r=r;
		}
		public String toString()
		{
			return "GR"+r;
		}
	}

	public class AddressOperand
	{
		private String adr;
		private RegisterOperand x=null;

		public AddressOperand(final String adr)
		{
			this.adr=adr;
		}

		public AddressOperand(final String adr, final RegisterOperand x)
		{
			this.adr=adr;
			this.x=x;
		}

		public String toString()
		{
			return adr+(x==null ? "" : x.toString());
		}
	}

	public static class Code
	{
		private String label="";
		private Inst inst;
		private String [] operand;
		public static final int _true=1;
		public static final int _false=0;

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
			s+=String.join(",", operand);
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

	public enum Library
	{
		MULT, DIV, MOD, RDINT, RDCH, RDSTR, RDLN, WRTINT, WRTCH, WRTSTR, WRTLN;

		private final static EnumMap<Library, CASL> library;

		static
		{
			library=new EnumMap<>(Library.class);
			library.put(MULT, new CASL());
			library.put(DIV, new CASL());
			library.put(MOD, new CASL());
			library.put(RDINT, new CASL());
			library.put(RDCH, new CASL());
			library.put(RDSTR, new CASL());
			library.put(RDLN, new CASL());
			library.put(WRTINT, new CASL());
			library.put(WRTCH, new CASL("WRTCH\tSTART\n"+"\tPOP\t\tGR3\t\t\t; 戻り先番地の内容をスタックに退避\n"+"\tPOP\t\tGR2\n"+"\tLD\t\tGR2,0,GR2\n"+"\tPOP\t\tGR5\t\t\t; LIBLENの番地\n"+"\tLD\t\tGR6,0,GR5\t; LIBLENの値\n"+"\tPOP\t\tGR7\n"+"\tLD\t\tGR1,GR7\n"+"\tADDA\tGR1,GR6\t\t; GR1に次の文字を格納する番地を代入\n"+"\tST\t\tGR2,0,GR1\n"+"\tLAD\t\tGR6,1,GR6\n"+"\tPUSH\t0,GR3\n"+"\tST\t\tGR6,0,GR5\t; LIBLENを更新\n"+"\tRET\n"+"\tEND"));
			library.put(WRTSTR, new CASL("WRTSTR\tSTART\n"+"\tPOP\tGR3\t\t; 戻り先番地の退避\n"+"\tPOP\tGR1\n"+"\tPOP\tGR2\n"+"\tPOP\tGR5\t\t; LIBLENの番地\n"+"\tLD\tGR6,0,GR5\t; LIBLENの値\n"+"\tPOP\tGR7\n"+"\tPUSH\t0,GR3\n"+"\tLAD\tGR3,0\t; GR3は制御変数として用いる\n"+"LOOP\tCPA\tGR3,GR1\n"+"\tJZE\tEND\n"+"\tLD\tGR4,GR2\n"+"\tADDA\tGR4,GR3\t; 出力する文字の格納番地を計算\n"+"\tLD\tGR0,0,GR4\t; 出力する文字をレジスタにコピー\n"+"\tLD\tGR4,GR7\n"+"\tADDA\tGR4,GR6\t; 出力先の番地を計算\n"+"\tST\tGR0,0,GR4\t; 出力装置に書き出し\n"+"\tLAD\tGR3,1,GR3\n"+"\tLAD\tGR6,1,GR6\n"+"\tJUMP\tLOOP\n"+"END\tST\tGR6,0,GR5\n"+"\tRET\n"+"\tEND"));
			library.put(WRTLN, new CASL("WRTLN\tSTART\n"+"\tPOP\tGR1\n"+"\tPOP\tGR5\t\t; LIBLENの番地\n"+"\tLD\tGR6,0,GR5\t; LIBLENの値\n"+"\tPOP\tGR7\n"+"\tPUSH\t0,GR1\n"+"\tST\tGR6,OUTLEN\n"+"\tLAD\tGR1,0\n"+"LOOP\tCPA\tGR1,OUTLEN\n"+"\tJZE\tEND\n"+"\tLD\tGR2,GR7\n"+"\tADDA\tGR2,GR1\n"+"\tLD\tGR3,0,GR2\n"+"\tST\tGR3,OUTSTR,GR1\n"+"\tLAD\tGR1,1,GR1\n"+"\tJUMP\tLOOP\n"+"END\tOUT\tOUTSTR,OUTLEN\n"+"\tLAD\tGR1,0\n"+"\tST\tGR1,0,GR5\t; 文字列を出力して，LIBLENを初期化\n"+"\tRET\n"+"OUTSTR\tDS\t256\n"+"OUTLEN\tDS\t1\n"+"\tEND"));
		};

		public CASL get()
		{
			return library.get(this);
		}
	}

	private ArrayList<Code> main=new ArrayList<>();
	private ArrayList<Code> storage=new ArrayList<>();
	private ArrayList<Code> constant=new ArrayList<>();

	public CASL(){}

	public CASL(final String s)
	{
		String [] list=s.split("\n");
		for(String line:list)
		{
			if(line.indexOf(';')>=0)
			{
				line=line.substring(0, line.indexOf(';')).trim();
			}
			String [] lineList=line.split("\\s", 2);
			String label="";
			Inst inst=null;
			String [] operand=new String[0];
			if(lineList.length<1)
			{
				continue;
			}
			if(lineList.length==1)
			{
				if(Inst.anyMatch(lineList[0]))
				{
					inst=Inst.valueOf(lineList[0]);
				}
			}
			else
			{
				if(Arrays.stream(Inst.values()).anyMatch(i->lineList[1].indexOf(i.name())==0))
				{
					label=lineList[0];
					String [] instOperand=lineList[1].split("\\s", 2);
					if(Inst.anyMatch(instOperand[0]))
					{
						inst=Inst.valueOf(instOperand[0]);
						if(instOperand.length>1)
						{
							operand=instOperand[1].split(",");
						}
					}
				}
				else if(Inst.anyMatch(lineList[0]))
				{
					inst=Inst.valueOf(lineList[0]);
					if(lineList.length>1)
					{
						operand=lineList[1].split(",");
					}
				}
			}

			switch(inst)
			{
				case DS:addStorage(label.trim(), Integer.valueOf(operand[0])); break;
				case DC:addConstant(label.trim(), operand[0]); break;
				case END:break;
				default:main.add(new Code(label.trim(), inst, operand)); break;
			}
		}
	}

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
