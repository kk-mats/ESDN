package enshud.s4.compiler;

import enshud.s1.lexer.TSToken;

import java.util.ArrayList;

public class IL
{
	public enum Inst
	{
		Assign, Goto, IfGoto, Param, Call, Return, Label, Fun, Def
	}

	public enum Op
	{
		Add("+"), Sub("-"), Or("or"),
		Mul("*"), Div("/"), Mod("%"), And("and"),
		not("not"),
		Equal("="), NotEqual("!="), Less("<"), LessEq("<="), Greater(">"), GreaterEq(">=");

		private String text;

		Op(final String text)
		{
			this.text=text;
		}

		public static Op of(final TSToken token)
		{
			switch(token)
			{
				case SPLUS:return Add;
				case SMINUS:return Sub;
				case SOR:return Or;
				case SSTAR:return Mul;
				case SDIVD:return Div;
				case SMOD:return Mod;
				case SEQUAL:return Equal;
				case SNOTEQUAL:return NotEqual;
				case SLESS:return Less;
				case SLESSEQUAL:return LessEq;
				case SGREAT:return Greater;
				case SGREATEQUAL:return GreaterEq;
			}
			return null;
		}
	}

	public static class Code
	{
		private Inst inst;
		private String x, y, z;
		private Op op;

		public Code()
		{
		}

		public static Code Assign(final String dest, final String src)
		{
			Code c=new Code();
			c.inst=Inst.Assign;
			c.x=dest;
			c.y=src;
			return c;
		}

		public static Code Assign(final String dest, final Op op, final String src)
		{
			Code c=new Code();
			c.inst=Inst.Assign;
			c.x=dest;
			c.op=op;
			c.y=src;
			return c;
		}

		public static Code Assign(final String dest, final String left, final Op op, final String right)
		{
			Code c=new Code();
			c.inst=Inst.Assign;
			c.x=dest;
			c.y=left;
			c.op=op;
			c.z=right;
			return c;
		}

		public static Code Goto(final String label)
		{
			Code c=new Code();
			c.inst=Inst.Goto;
			c.x=label;
			return c;
		}

		public static Code IfGoto(final String left, final Op rop, final String right, final String label)
		{
			Code c=new Code();
			c.inst=Inst.IfGoto;
			c.x=left;
			c.op=rop;
			c.y=right;
			c.z=label;
			return c;
		}

		public static Code Param(final String p)
		{
			Code c=new Code();
			c.inst=Inst.Param;
			c.x=p;
			return c;
		}

		public static Code Call(final String name, final String nparam)
		{
			Code c=new Code();
			c.inst=Inst.Call;
			c.x=name;
			c.y=nparam;
			return c;
		}

		public static Code Return()
		{
			Code c=new Code();
			c.inst=Inst.Return;
			return c;
		}

		public static Code Label(final String label)
		{
			Code c=new Code();
			c.inst=Inst.Label;
			c.x=label;
			return c;
		}

		public static Code Fun(final String name)
		{
			Code c=new Code();
			c.inst=Inst.Fun;
			c.x=name;
			return c;
		}

		public static Code Def(final String label, final String constant)
		{
			Code c=new Code();
			c.inst=Inst.Def;
			c.x=label;
			c.y=constant;
			return c;
		}

		private static String tab2="\t\t";

		public String toString()
		{
			switch(inst)
			{
				case Assign:
				{
					if(op==null) return tab2+x+" := "+y;
					if(z==null) return tab2+x+" := "+op.text+y;
					return tab2+x+" := "+y+" "+op.text+" "+z;
				}
				case Goto:
				case Param: return tab2+inst.toString().toLowerCase()+" "+x;
				case Call: return tab2+"call "+x+", "+y;
				case IfGoto: return tab2+"if "+x+" "+op.text+" "+y+" goto "+z;
				case Return: return tab2+"return"+(x!=null ? " "+x : "");
				case Label: return "\t"+x+":";
				case Fun: return "fun "+x+" :";
				case Def: return "def "+x+", "+y;
			}
			return "(null)";
		}
	}

	private ArrayList<Code> code=new ArrayList<>();
	private ArrayList<Code> constant=new ArrayList<>();
	private ArrayList<Code> storage=new ArrayList<>();

	public void addCode(final Code line)
	{
		code.add(line);
	}

	public void addConstant(final Code line)
	{
		constant.add(line);
	}

	public void addStorage(final Code line)
	{
		storage.add(line);
	}

	public ArrayList<Code> getCode()
	{
		return code;
	}

	public ArrayList<Code> getConstant()
	{
		return constant;
	}

	public ArrayList<Code> getStorage()
	{
		return storage;
	}

	public String toString()
	{
		ArrayList<Code> c=code;
		c.addAll(constant);
		c.addAll(storage);
		return c.stream().map(Code::toString).reduce("", (joined, s)->joined+s+"\n");
	}
}
