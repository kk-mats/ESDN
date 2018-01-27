package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.stream.Collectors;

public class CASL
{
	public void addMain(final String label, final Inst inst, final Operand operand)
	{
		// convert integer and literal to appropriate text

		if(inst.hasNoOperand())
		{
			main.add(new Code(label, inst, operand));
			return;
		}

		Operand newOperand=new Operand(Arrays.copyOf(operand.elements, operand.elements.length));
		// if [Inst Adr,x] form
		if(inst.isOperandTypeAdrX())
		{
			if(newOperand.elements[0].attribute==OperandElement.Attribute.literal)
			{
				newOperand.elements[0].elementName="="+newOperand.elements[0].elementName;
				newOperand.elements[0].attribute=OperandElement.Attribute.address;
			}
			else if(newOperand.elements[0].attribute==OperandElement.Attribute.register)
			{
				newOperand.elements=new OperandElement[]{new OperandElement("0", OperandElement.Attribute.address), newOperand.elements[0]};
			}
		}
		else
		{
			// if [Inst R1,R2] or [Inst R,Adr] form
			if(inst.isOperandTypeR1R2AndRAdrX() && newOperand.length()==2)
			{
				// if [Inst R,Adr] form
				if(newOperand.elements[1].attribute==OperandElement.Attribute.literal)
				{
					newOperand.elements[1].elementName="="+newOperand.elements[1].elementName;
					newOperand.elements[1].attribute=OperandElement.Attribute.address;
				}
				else if(operand.elements[1].attribute==OperandElement.Attribute.integer)
				{
					newOperand.elements[1].elementName="="+newOperand.elements[1].elementName;
					newOperand.elements[1].attribute=OperandElement.Attribute.address;
				}
			}

			if(inst.isOperandTypeRAdrX() && newOperand.elements[1].attribute==OperandElement.Attribute.literal)
			{
				newOperand.elements[1].elementName="="+newOperand.elements[1].elementName;
				newOperand.elements[1].attribute=OperandElement.Attribute.address;
			}
		}

		main.add(new Code(label, inst, newOperand));
	}

	public static final String TrueString="1";//(true)";

	public static class Operand
	{
		private OperandElement[] elements;

		public Operand(final OperandElement... elements)
		{
			this.elements=elements;
		}

		public Operand join(final OperandElement... elements)
		{
			OperandElement[] newElements=new OperandElement[this.elements.length+elements.length];
			System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
			System.arraycopy(elements, 0, newElements, this.elements.length, elements.length);
			return new Operand(newElements);
		}

		public Operand join(final Operand operand)
		{
			OperandElement[] newElements=new OperandElement[elements.length+operand.elements.length];
			System.arraycopy(elements, 0, newElements, 0, elements.length);
			System.arraycopy(operand.elements, 0, newElements, elements.length, operand.elements.length);
			return new Operand(newElements);
		}

		public int length()
		{
			return elements.length;
		}

		public OperandElement get(final int i)
		{
			return elements[i];
		}

		public OperandElement[] getElements()
		{
			return elements;
		}

		public void setElement(final int i, final OperandElement element)
		{
			elements[i]=element;
		}

		public String toString()
		{
			return String.join(",", Arrays.stream(elements).map(OperandElement::toString).collect(Collectors.toList()));
		}
	}

	public static final String FalseString="0";//(false)";
	private boolean libraryBufferFlag=false;

	private ArrayList<Code> main=new ArrayList<>();
	private ArrayList<Code> storage=new ArrayList<>();
	private ArrayList<Code> constant=new ArrayList<>();

	public enum Inst
	{
		LD, ST, LAD, ADDA, ADDL, SUBA, SUBL, AND, OR, XOR, CPA, CPL, SLA, SRA, SLL, SRL, JPL, JMI, JNZ, JZE, JOV, JUMP, PUSH, POP, RPUSH, RPOP, CALL, RET, SVC, NOP, START, END, DS, DC, IN, OUT;

		public static Inst of(final TSToken token)
		{
			switch(token)
			{
				case SPLUS:
					return ADDA;
				case SMINUS:
					return SUBA;
				case SOR:
					return OR;
				case SAND:
					return AND;
			}
			return null;
		}

		public boolean isOperandTypeR1R2AndRAdrX()
		{
			return this==LD || this==ADDA || this==ADDL || this==SUBA || this==SUBL || this==AND || this==OR || this==XOR || this==CPA || this==CPL;
		}

		public boolean isOperandTypeRAdrX()
		{
			return this==ST || this==LAD || this==SLA || this==SRA || this==SLL || this==SRL;
		}

		public boolean isOperandTypeAdrX()
		{
			return this==JPL || this==JMI || this==JNZ || this==JZE || this==JOV || this==JUMP || this==PUSH;
		}

		public boolean hasNoOperand()
		{
			return this==Inst.RPOP || this==Inst.RPUSH || this==Inst.RET || this==Inst.NOP || this==Inst.START || this==Inst.END;
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

	public void addCode(final Inst inst, final OperandElement r1, final OperandElement r2, final OperandElement r3)
	{
		addMain("", inst, new Operand(r1, r2, r3));
	}

	public void addStorage(final OperandElement label, final int n)
	{
		if(storage.stream().noneMatch(c->c.getLabel().equals(label)))
		{
			storage.add(new Code(label.elementName, Inst.DS, new OperandElement(String.valueOf(n), OperandElement.Attribute.literal)));
		}
	}


	public CASL(){}

	public CASL(final String s)
	{

	}

	public void useLibraryBuffer()
	{
		this.libraryBufferFlag=true;
	}

	public void addCode(final Inst inst)
	{
		addMain("", inst, new Operand());
	}

	public void addCode(final Inst inst, final OperandElement r)
	{
		addMain("", inst, new Operand(r));
	}

	public void addCode(final Inst inst, final Operand r)
	{
		addMain("", inst, r);
	}

	public void addCode(final Inst inst, final OperandElement r1, final OperandElement r2)
	{
		addMain("", inst, new Operand(r1, r2));
	}

	public ArrayList<Code> getConstant()
	{
		return constant;
	}

	public void addCode(final Inst inst, final OperandElement r1, final Operand r2)
	{
		addMain("", inst, new Operand(r1).join(r2));
	}

	public void addCode(final Inst inst, final Operand r1, final OperandElement r2)
	{
		addMain("", inst, r1.join(r2));
	}

	public void addCode(final Inst inst, final Operand r1, final Operand r2)
	{
		addMain("", inst, r1.join(r2));
	}

	public void addCode(final OperandElement label)
	{
		addMain(label.elementName, Inst.NOP, new Operand());
	}

	public void addCode(final OperandElement label, final Inst inst)
	{
		addMain(label.elementName, inst, new Operand());
	}

	public void insertCode(final int index, final Code code)
	{
		main.add(index, code);
	}

	public ArrayList<Code> getStorage()
	{
		return storage;
	}

	public void addConstant(final OperandElement label, final OperandElement s)
	{
		constant.add(new Code(label.elementName, Inst.DC, s));
	}

	public boolean requiresLibraryBuffer()
	{
		return libraryBufferFlag;
	}

	public ArrayList<Code> getMain()
	{
		return main;
	}

	public enum Library
	{
		MULT, DIV, MOD, RDINT, RDCH, RDSTR, RDLN, WRTINT, WRTCH, WRTSTR, WRTLN;

		private final static EnumMap<Library, String> library;
		private final static BitSet inUse=new BitSet(values().length);
		private static boolean useRETV=false;

		static
		{
			library=new EnumMap<>(Library.class);
			library.put(MULT, "MULT\tSTART\n"+"\t\tPOP\t\tGR5\t\t; 戻り先番地\n"+"\t\tPOP\t\tGR1\n"+"\t\tPOP\t\tGR2\n"+"\t\tPOP\t\tGR6\t\t; 戻り値用領域アドレス\n"+"\t\tPUSH\t0,GR5\n"+"\t\tLAD\t\tGR3,0\t; GR3を初期化\n"+"\t\tLD\t\tGR4,GR2\n"+"\t\tJPL\t\tLOOP\n"+"\t\tXOR\t\tGR4,=#FFFF\n"+"\t\tADDA\tGR4,=1\n"+"LOOP\tSRL\t\tGR4,1\n"+"\t\tJOV\t\tONE\n"+"\t\tJUMP\tZERO\n"+"ONE\t\tADDL\tGR3,GR1\n"+"ZERO\tSLL\t\tGR1,1\n"+"\t\tAND\t\tGR4,GR4\n"+"\t\tJNZ\t\tLOOP\n"+"\t\tCPA\t\tGR2,=0\n"+"\t\tJPL\t\tEND\n"+"\t\tXOR\t\tGR3,=#FFFF\n"+"\t\tADDA\tGR3,=1\n"+"END\t\tST\t\tGR3,0,GR6\n"+"\t\tRET\n"+"\t\tEND");
			library.put(DIV, "DIV\t\tSTART\n"+"\t\tPOP\t\tGR5\t\t; 戻り先番地\n"+"\t\tPOP\t\tGR1\n"+"\t\tPOP\t\tGR2\n"+"\t\tPOP\t\tGR6\t\t; 戻り値用領域アドレス\n"+"\t\tPUSH\t0,GR5\n"+"\t\tLD\t\tGR4,GR1\n"+"\t\tLD\t\tGR5,GR2\n"+"\t\tCPA\t\tGR1,=0\n"+"\t\tJPL\t\tSKIPA\n"+"\t\tXOR\t\tGR1,=#FFFF\n"+"\t\tADDA\tGR1,=1\n"+"SKIPA\tCPA\t\tGR2,=0\n"+"\t\tJZE\t\tSKIPD\n"+"\t\tJPL\t\tSKIPB\n"+"\t\tXOR\t\tGR2,=#FFFF\n"+"\t\tADDA\tGR2,=1\n"+"SKIPB\tLD\t\tGR3,=0\n"+"LOOP\tCPA\t\tGR1,GR2\n"+"\t\tJMI\t\tSTEP\n"+"\t\tSUBA\tGR1,GR2\n"+"\t\tLAD\t\tGR3,1,GR3\n"+"\t\tJUMP\tLOOP\n"+"STEP\tLD\t\tGR2,GR3\n"+"\t\tLD\t\tGR3,GR4\n"+"\t\tCPA\t\tGR3,=0\n"+"\t\tJPL\t\tSKIPC\n"+"\t\tXOR\t\tGR1,=#FFFF\n"+"\t\tADDA\tGR1,=1\n"+"SKIPC\tXOR\t\tGR3,GR5\n"+"\t\tCPA\t\tGR3,=0\n"+"\t\tJZE\t\tSKIPD\n"+"\t\tJPL\t\tSKIPD\n"+"\t\tXOR\t\tGR2,=#FFFF\n"+"\t\tADDA\tGR2,=1\n"+"SKIPD\tST\t\tGR2,0,GR6\n"+"\t\tRET\n"+"\t\tEND");
			library.put(MOD, "MOD\t\tSTART\n"+"\t\tPOP\t\tGR5\t\t; 戻り先番地\n"+"\t\tPOP\t\tGR1\n"+"\t\tPOP\t\tGR2\n"+"\t\tPOP\t\tGR6\t\t; 戻り値用領域アドレス\n"+"\t\tPUSH\t0,GR5\n"+"\t\tLD\t\tGR4,GR1\n"+"\t\tLD\t\tGR5,GR2\n"+"\t\tCPA\t\tGR1,=0\n"+"\t\tJPL\t\tSKIPA\n"+"\t\tXOR\t\tGR1,=#FFFF\n"+"\t\tADDA\tGR1,=1\n"+"SKIPA\tCPA\t\tGR2,=0\n"+"\t\tJZE\t\tSKIPD\n"+"\t\tJPL\t\tSKIPB\n"+"\t\tXOR\t\tGR2,=#FFFF\n"+"\t\tADDA\tGR2,=1\n"+"SKIPB\tLD\t\tGR3,=0\n"+"LOOP\tCPA\t\tGR1,GR2\n"+"\t\tJMI\t\tSTEP\n"+"\t\tSUBA\tGR1,GR2\n"+"\t\tLAD\t\tGR3,1,GR3\n"+"\t\tJUMP\tLOOP\n"+"STEP\tLD\t\tGR2,GR3\n"+"\t\tLD\t\tGR3,GR4\n"+"\t\tCPA\t\tGR3,=0\n"+"\t\tJPL\t\tSKIPD\n"+"\t\tXOR\t\tGR1,=#FFFF\n"+"\t\tADDA\tGR1,=1\n"+"SKIPD\tST\t\tGR1,0,GR6\n"+"\t\tRET\n"+"\t\tEND");
			library.put(RDINT, "RDINT\tSTART\n"+"\t\tPOP\t\tGR5\t\t\t\t; 戻り先番地\n"+"\t\tPOP\t\tGR2\t\t\t\t; 戻り値用アドレス\n"+"\t\tPUSH\t0,GR5\n"+"\t\tLD\t\tGR5,GR2\t\t\t; GR2が指す番地をGR5にコピー\n"+"\t\tLD\t\tGR2,=0\t\t\t; GR2を初期化\n"+"\t\tLD\t\tGR3,=0\t\t\t; GR3を初期化\n"+"\t\tIN\t\tINAREA,INLEN\t; 入力を受け取る\n"+"\t\t; 入力がnullかどうかのチェック\n"+"\t\tCPA\t\tGR3,INLEN\n"+"\t\tJZE\t\tERROR\n"+"\t\t; 最初の文字が'-'かどうかのチェック\n"+"\t\tLD\t\tGR4,INAREA,GR3\n"+"\t\tLAD\t\tGR3,1,GR3\n"+"\t\tLD\t\tGR6,GR4\t\t\t; GR6に入力された先頭の文字を保存\n"+"\t\tCPL\t\tGR4,=#002D\t\t; '-'かどうか\n"+"\t\tJZE\t\tLOOP\n"+"\t\tCPL\t\tGR4,='0'\t\t; 数値かどうかのチェック\n"+"\t\tJMI\t\tERROR\n"+"\t\tCPL\t\tGR4,='9'\n"+"\t\tJPL\t\tERROR\n"+"\t\tXOR\t\tGR4,=#0030\t\t; 数値だったら変換\n"+"\t\tADDA\tGR2,GR4\n"+"\t; 「すでに読み込んだ数値を10倍して，新しく読み込んだ数値と足す」を繰り返す\n"+"LOOP\tCPA\t\tGR3,INLEN\n"+"\t\tJZE\t\tCODE\t\t\t; 入力された文字数とGR3が同じであればループを抜ける\n"+"\t\tLD\t\tGR1,GR2\n"+"\t\tSLA\t\tGR1,1\n"+"\t\tSLA\t\tGR2,3\n"+"\t\tADDA\tGR2,GR1\n"+"\t\tLD\t\tGR4,INAREA,GR3\n"+"\t\tCPL\t\tGR4,='0'\t\t; 数値かどうかのチェック\n"+"\t\tJMI\t\tERROR\n"+"\t\tCPL\t\tGR4,='9'\n"+"\t\tJPL\t\tERROR\n"+"\t\tXOR\t\tGR4,=#0030\t\t; GR4の内容を数値に変換\n"+"\t\tADDA\tGR2,GR4\t\t\t; GR2にGR1の内容を足す\n"+"\t\tLAD\t\tGR3,1,GR3\t\t; GR3(ポインタ)をインクリメント\n"+"\t\tJUMP\tLOOP\n"+"\t\t; 最初の文字が'-'であった場合は-1倍する\n"+"CODE\tCPL\t\tGR6,=#002D\n"+"\t\tJNZ\t\tEND\n"+"\t\tXOR\t\tGR2,=#FFFF\n"+"\t\tLAD\t\tGR2,1,GR2\n"+"\t\tJUMP\tEND\n"+"\t\t; エラーを出力する\n"+"ERROR\tOUT\t\tERRSTR,ERRLEN\n"+"END\t\tST\t\tGR2,0,GR5\t\t; GR2の内容をGR5が指す番地に格納する\n"+"\t\tRET\n"+"ERRSTR\tDC\t'illegal input'\n"+"ERRLEN\tDC\t13\n"+"INAREA\tDS\t6\n"+"INLEN\tDS\t1\n"+"\tEND");
			library.put(RDCH, "RDCH\tSTART\n"+"\tPOP\t\tGR5\n"+"\tPOP\t\tGR2\n"+"\tPUSH\t0,GR5\n"+"\tIN\t\tINCHAR,INLEN\n"+"\tLD\t\tGR1,INCHAR\n"+"\tST\t\tGR1,0,GR2\n"+"\tRET\n"+"INCHAR\tDS\t1\n"+"INLEN\tDS\t1\n"+"\tEND");
			library.put(RDSTR, "RDSTR\tSTART\n"+"\t\tPOP\t\tGR5\n"+"\t\tPOP\t\tGR1\n"+"\t\tPOP\t\tGR2\n"+"\t\tPUSH\t0,GR5\n"+"\t\tLAD\t\tGR4,0\t; GR4を初期化\n"+"\t\tIN\t\tINSTR,INLEN\n"+"LOOP\tCPA\t\tGR4,GR1\n"+"\t\tJZE\t\tEND\t; GR1で指定された文字数を超えたら終わり\n"+"\t\tCPA\t\tGR4,INLEN\n"+"\t\tJZE\t\tEND\t; 入力された文字数を超えたら終わり\n"+"\t\tLD\t\tGR5,GR2\n"+"\t\tADDA\tGR5,GR4\t; 文字の格納先番地を計算\n"+"\t\tLD\t\tGR3,INSTR,GR4\n"+"\t\tST\t\tGR3,0,GR5\n"+"\t\tLAD\t\tGR4,1,GR4\n"+"\t\tJUMP\tLOOP\n"+"END\t\tRET\n"+"INSTR\tDS\t\t256\n"+"INLEN\tDS\t\t1\n"+"\t\tEND");
			library.put(RDLN, "RDLN\tSTART\n"+"\tIN\tINAREA,INLEN\n"+"\tRET\n"+"INAREA\tDS\t256\n"+"INLEN\tDS\t1\n"+"\tEND");
			library.put(WRTINT, "WRTINT\tSTART\n"+"\t\tPOP\t\tGR5\n"+"\t\tPOP\t\tGR2\n"+"\t\tPOP\t\tGR4\t\t\t; LIBLENのアドレス\n"+"\t\tLD\t\tGR6,0,GR4\t; LIBLENの値\n"+"\t\tPOP\t\tGR7\t\t\t; LIBBUF\n"+"\t\tPUSH\t0,GR5\n"+"\t\tLD\t\tGR3,=0\t\t; GR3はインデックスとして用いる\n"+"\t\tLD\t\tGR5,GR2\n"+"\t\t; 数値データが負数である場合は，正の数に変換\n"+"\t\tCPA\t\tGR2,=0\n"+"\t\tJPL\t\tLOOP1\n"+"\t\tXOR\t\tGR2,=#FFFF\n"+"\t\tADDA\tGR2,=1\n"+"\t\t; 数値データを変換しながら，バッファに格納\n"+"LOOP1\tRPUSH\n"+"\t\tPUSH\tRETV\n"+"\t\tPUSH\t10\n"+"\t\tPUSH\t0,GR2\n"+"\t\tCALL\tMOD\n"+"\t\tRPOP\n"+"\t\tLD\t\tGR1,RETV\n"+"\t\tXOR\t\tGR1,=#0030\n"+"\t\tST\t\tGR1,BUFFER,GR3\n"+"\t\tLAD\t\tGR3,1,GR3\n"+"\t\tRPUSH\n"+"\t\tPUSH\tRETV\n"+"\t\tPUSH\t10\n"+"\t\tPUSH\t0,GR2\n"+"\t\tCALL\tDIV\n"+"\t\tRPOP\n"+"\t\tLD\t\tGR2,RETV\n"+"\t\tCPA\t\tGR2,=0\n"+"\t\tJNZ\t\tLOOP1\n"+"\t\t; 数値データが負数であれば，'-'を追加\n"+"\t\tCPA\t\tGR5,=0\n"+"\t\tJZE\t\tLOOP2\n"+"\t\tJPL\t\tLOOP2\n"+"\t\tLD\t\tGR1,='-'\n"+"\t\tST\t\tGR1,BUFFER,GR3\n"+"\t\tLAD\t\tGR3,1,GR3\n"+"\t\t; BUFFERを逆順にたどりながら，出力用バッファに格納\n"+"LOOP2\tLAD\t\tGR3,-1,GR3\n"+"\t\tLD\t\tGR1,BUFFER,GR3\n"+"\t\tLD\t\tGR2,GR7\n"+"\t\tADDA\tGR2,GR6\n"+"\t\tST\t\tGR1,0,GR2\n"+"\t\tLAD\t\tGR6,1,GR6\n"+"\t\tCPA\t\tGR3,=0\n"+"\t\tJNZ\t\tLOOP2\n"+"END\t\tST\t\tGR6,0,GR4\n"+"\t\tRET\n"+"BUFFER\tDS\t\t6\n"+"RETV\tDS\t\t1\n"+"\t\tEND");
			library.put(WRTCH, "WRTCH\tSTART\n"+"\tPOP\t\tGR5\n"+"\tPOP\t\tGR1\t\t\t; 文字のアドレス\n"+"\tLD\t\tGR2,0,GR1\n"+"\tPOP\t\tGR4\t\t\t; LIBLENのアドレス\n"+"\tLD\t\tGR6,0,GR4\t; LIBLEN\n"+"\tPOP\t\tGR7\t\t\t; LIBBUF\n"+"\tPUSH\t0,GR5\n"+"\tADDA\tGR7,GR6\t\t; GR7に次の文字を格納する番地を代入\n"+"\tST\t\tGR2,0,GR7\n"+"\tLAD\t\tGR6,1,GR6\n"+"\tST\t\tGR6,0,GR4\n"+"\tRET\n"+"\tEND");
			library.put(WRTSTR, "WRTSTR\tSTART\n"+"\t\tPOP\t\tGR3\n"+"\t\tPOP\t\tGR1\t\t\t; LEN\n"+"\t\tPOP\t\tGR2\t\t\t; STR\n"+"\t\tPOP\t\tGR4\t\t\t; LIBLENのアドレス\n"+"\t\tLD\t\tGR6,0,GR4\t; LIBLENの値\n"+"\t\tPOP\t\tGR7\t\t\t; LIBBUF\n"+"\t\tPUSH\t0,GR3\n"+"\t\tADDA\tGR7,GR6\t\t; GR7=&LIBBUF+LIBLEN\n"+"\t\tADDA\tGR6,GR1\t\t; GR6=LIBLEN+LEN\n"+" \t\tST\t\tGR6,0,GR4\t; LIBLENを更新\n"+"\t\tLD\t\tGR3,GR7\n"+"\t\tADDA\tGR3,GR1\t\t; GR3=&LIBBUF+LIBLEN+LEN\n"+"LOOP\tCPA\t\tGR3,GR7\n"+"\t\tJZE\t\tEND\n"+"\t\tLD\t\tGR4,0,GR2\n"+"\t\tST\t\tGR4,0,GR7\n"+"\t\tLAD\t\tGR2,1,GR2\n"+"\t\tLAD\t\tGR7,1,GR7\n"+"\t\tJUMP\tLOOP\n"+"END\t\tRET\n"+"\t\tEND");
			library.put(WRTLN, "WRTLN\tSTART\n"+"\t\tPOP\t\tGR5\n"+"\t\tPOP\t\tGR4\t\t\t\t; LIBLENのアドレス\n"+"\t\tLD\t\tGR6,0,GR4\t\t; LIBLEN\n"+"\t\tPOP\t\tGR7\t\t\t\t; LIBBUF\n"+"\t\tPUSH\t0,GR5\n"+"\t\tST\t\tGR6,OUTLEN\n"+"\t\tLAD\t\tGR1,0\n"+"LOOP\tCPA\t\tGR1,OUTLEN\n"+"\t\tJZE\t\tEND\n"+"\t\tLD\t\tGR2,GR7\n"+"\t\tADDA\tGR2,GR1\n"+"\t\tLD\t\tGR3,0,GR2\n"+"\t\tST\t\tGR3,OUTSTR,GR1\n"+"\t\tLAD\t\tGR1,1,GR1\n"+"\t\tJUMP\tLOOP\n"+"END\t\tOUT\t\tOUTSTR,OUTLEN\n"+"\t\tLAD\t\tGR6,0\t\t\t; 文字列を出力して，GR6を初期化\n"+"\t\tST\t\tGR6,0,GR4\n"+"\t\tRET\n"+"OUTSTR\tDS\t\t256\n"+"OUTLEN\tDS\t\t1\n"+"\tEND");
		}

		public static boolean isEmpty()
		{
			return inUse.isEmpty();
		}

		public boolean isInUse()
		{
			return inUse.get(this.ordinal());
		}

		public static boolean useRETV()
		{
			boolean r=useRETV;
			useRETV=false;
			return r;
		}

		public static void setRETV()
		{
			useRETV=true;
		}

		public static void use(final Library lib)
		{
			inUse.set(lib.ordinal());
			switch(lib)
			{
				case WRTINT:
					inUse.set(DIV.ordinal());
					inUse.set(MOD.ordinal());
					break;
				case MULT:
				case DIV:
				case MOD:
				case RDINT:
				case RDCH:
					useRETV=true;
			}
		}

		public String get()
		{
			return library.get(this);
		}
	}

	public static class OperandElement
	{
		public enum Attribute
		{
			register("Reg"), address("Adr"), literal("Lit"), integer("Int"), empty("Emp");

			private String alias;

			Attribute(final String alias)
			{
				this.alias=alias;
			}

			public String toString()
			{
				return Compiler.debug ? "<"+alias+">" : "";
			}
		}

		private String elementName;
		private Attribute attribute;

		public OperandElement()
		{
			this.elementName="";
			this.attribute=Attribute.empty;
		}

		public OperandElement(final String elementName, final Attribute attribute)
		{
			this.elementName=elementName;
			this.attribute=attribute;
		}

		public String getELementName()
		{
			return elementName;
		}

		public Attribute getAttribute()
		{
			return attribute;
		}

		public String toString()
		{
			return elementName+attribute.toString();
		}
	}

	public ArrayList<Code> getCodeSet()
	{
		ArrayList<Code> c=main;
		c.addAll(storage);
		c.addAll(constant);
		c.add(new Code(Inst.END, new Operand()));
		return c;
	}

	public static class Code
	{
		private String label="";
		private Inst inst;
		private Operand operand;

		public Code(final Inst inst)
		{
			this.inst=inst;
		}

		public Code(final Inst inst, final Operand operand)
		{
			this.inst=inst;
			this.operand=operand;
		}

		public Code(final Inst inst, final OperandElement... elements)
		{
			this.inst=inst;
			this.operand=new Operand(elements);
		}

		public Code(final String label, final Inst inst, final OperandElement... elements)
		{
			this.label=label;
			this.inst=inst;
			this.operand=new Operand(elements);
		}

		public Code(final String label, final Inst inst, final Operand operand)
		{
			this.label=label;
			this.inst=inst;
			this.operand=operand;
		}

		public String toString()
		{
			String s=label;
			s=s+(s.length()<4 ? "\t\t" : "\t");
			s+=inst.name()+(inst.name().length()<4 ? "\t\t" : "\t");
			return s+operand.toString();
		}

		public String getLabel()
		{
			return label;
		}

		public Inst getInst()
		{
			return inst;
		}

		public Operand getOperand()
		{
			return operand;
		}

		public void setLabel(final String label)
		{
			this.label=label;
		}
	}


	public String toString()
	{
		return getCodeSet().stream().map(Code::toString).reduce("", (joined, s)->joined+s+"\n");
	}
}
