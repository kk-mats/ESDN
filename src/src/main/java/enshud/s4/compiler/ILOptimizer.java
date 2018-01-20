package enshud.s4.compiler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ILOptimizer
{
	public class BasicBlock
	{
		private String name="";
		private ArrayList<CASL.Code> code;
		private ArrayList<BasicBlock> next=new ArrayList<>();

		public BasicBlock(String name, ArrayList<CASL.Code> code)
		{
			this.name=name;
			this.code=code;
		}

		public BasicBlock(String name, ArrayList<CASL.Code> code, ArrayList<BasicBlock> next)
		{
			this.name=name;
			this.code=code;
			this.next=next;
		}

		BasicBlock(final ArrayList<CASL.Code> code)
		{
			this.code=code;
		}

		public String toString()
		{
			String s=name+"\n{\n";
			s+=code.stream().map(CASL.Code::toString).reduce("", (joined, t)->joined+t+"\n");
			s+="}->["+String.join(", ", next.stream().map(b->b.name).collect(Collectors.toList()))+"]\n";
			return s;
		}
	}

	public class ControlFlowGraph
	{
		private ArrayList<BasicBlock> graph;
		private int cur=0;
		ArrayList<AbstractMap.SimpleEntry<String, String>> path=new ArrayList<>();

		public ControlFlowGraph(final CASL casl)
		{
			graph=split(casl);
			connectBlocks();
		}

		private ArrayList<BasicBlock> split(final CASL casl)
		{
			ArrayList<BasicBlock> basicBlockList=new ArrayList<>();
			ArrayList<CASL.Code> code=new ArrayList<>();
			String name;
			while(cur<casl.getMain().size())
			{
				code.add(casl.getMain().get(cur));
				name=casl.getMain().get(cur).getLabel().isEmpty() ? String.valueOf(cur) : casl.getMain().get(cur).getLabel();
				for(; cur<casl.getMain().size()-1; ++cur)
				{
					if(casl.getMain().get(cur).getInst().isJump()/* || casl.getMain().get(cur).getInst()==CASL.Inst.CALL*/)
					{
						path.add(new AbstractMap.SimpleEntry<>(name, casl.getMain().get(cur).getOperand()[0]));
						if(casl.getMain().get(cur).getInst()!=CASL.Inst.JUMP)
						{
							path.add(new AbstractMap.SimpleEntry<>(name, casl.getMain().get(cur+1).getLabel().isEmpty() ? String.valueOf(cur+1) : casl.getMain().get(cur+1).getLabel()));
						}
						break;
					}

					if(!casl.getMain().get(cur+1).getLabel().isEmpty())
					{
						path.add(new AbstractMap.SimpleEntry<>(name, casl.getMain().get(cur+1).getLabel()));
						break;
					}
					code.add(casl.getMain().get(cur+1));
				}
				basicBlockList.add(new BasicBlock(name, code));
				code=new ArrayList<>();
				++cur;
			}
			return basicBlockList;
		}

		private void connectBlocks()
		{
			BasicBlock from=null, next=null;
			for(AbstractMap.SimpleEntry<String, String> p: path)
			{
				for(BasicBlock b:graph)
				{
					if(b.name.equals(p.getKey()))
					{
						from=b;
					}
					if(b.name.equals(p.getValue()))
					{
						next=b;
					}
					if(from!=null && next!=null)
					{
						from.next.add(next);
						break;
					}
				}
				if(from==null || next==null)
				{
					System.out.print("not found :["+p.getKey()+", "+p.getValue()+"]\n");
				}
				from=null;
				next=null;
			}
		}

		public String toString()
		{
			return graph.stream().map(BasicBlock::toString).reduce("", (joined, s)->joined+s+"\n");
		}
	}

	private ControlFlowGraph controlFlowGraph;

	public ILOptimizer(final CASL casl)
	{
		controlFlowGraph=new ControlFlowGraph(casl);
		System.out.print(controlFlowGraph.toString());
	}

	public String toString()
	{
		return controlFlowGraph.toString();
	}
}
