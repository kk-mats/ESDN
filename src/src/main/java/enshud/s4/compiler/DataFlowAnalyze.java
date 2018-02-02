package enshud.s4.compiler;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class DataFlowAnalyze
{
	HashMap<CASL.Code, HashMap<CASL.Code, String>> gen=new HashMap<>();
	HashMap<CASL.Code, HashMap<CASL.Code, String>> kill=new HashMap<>();
	HashMap<CASL.Code, HashMap<CASL.Code, String>> in=new HashMap<>();
	HashMap<CASL.Code, HashMap<CASL.Code, String>> out=new HashMap<>();
	private ArrayList<BitSet> steps=new ArrayList<>();
	
	public DataFlowAnalyze(final CASL casl)
	{
		// gen
		for(CASL.Code c : casl.getMain())
		{
			if(c.getInst().isGenKill())
			{
				HashMap<CASL.Code, String> tmp=new HashMap<>();
				tmp.put(c, c.getOperand().get(0).getELementName());
				gen.put(c, tmp);
			}
		}
		
		// kill
		for(CASL.Code c : gen.keySet())
		{
			for(CASL.Code d : gen.keySet())
			{
				// c kills d, d kills c
				if(c!=d && gen.get(c).equals(gen.get(d)))
				{
					if(kill.containsKey(c))
					{
						kill.get(c).putAll(gen.get(d));
					}
					else
					{
						HashMap<CASL.Code, String> h=new HashMap<>();
						h.putAll(gen.get(d));
						kill.put(c, h);
					}
					
					if(kill.containsKey(d))
					{
						kill.get(d).putAll(gen.get(c));
					}
					else
					{
						HashMap<CASL.Code, String> h=new HashMap<>();
						h.putAll(gen.get(c));
						kill.put(d, h);
					}
				}
			}
		}
		
		ControlFlowGraph cfg=new ControlFlowGraph(casl);
		ArrayDeque<ControlFlowGraph.BasicBlock> basicBlockStack=new ArrayDeque<>();
		basicBlockStack.addLast(cfg.getRoot());
		ControlFlowGraph.BasicBlock current;
		boolean change=false;
		
		do
		{
			change=false;
			while(!basicBlockStack.isEmpty())
			{
				current=basicBlockStack.getLast();
				HashMap<CASL.Code, String> cin=new HashMap<>();
				// set in of first line in current block
				if(current.getParent().size()>0)
				{
					for(ControlFlowGraph.BasicBlock p : current.getParent())
					{
						cin.putAll(out.get(p.getCode().size()-1));
						if(in.containsKey(current.getCode().get(0)))
						{
							for(CASL.Code c : cin.keySet())
							{
								if(!in.get(current.getCode().get(0)).containsKey(c))
								{
									change=true;
								}
							}
							in.get(current.getCode().get(0)).putAll(cin);
						}
						else
						{
							in.put(current.getCode().get(0), cin);
						}
					}
				}
				else
				{
					in.put(current.getCode().get(0), new HashMap<>());
				}
				
				// set out of first line in current block
				if(gen.containsKey(gen.get(current.getCode().get(0))))
				{
					if(out.containsKey(current.getCode().get(0)))
					{
						out.get(current.getCode().get(0)).putAll(gen.get(current.getCode().get(0)));
					}
					else
					{
						out.put(current.getCode().get(0), gen.get(current.getCode().get(0)));
					}
				}
				HashMap<CASL.Code, String> tmp;
				if(in.containsKey(current.getCode().get(0)))
				{
					tmp=(HashMap<CASL.Code, String>)in.get(current.getCode().get(0)).clone();
				}
				else
				{
					tmp=new HashMap<>();
				}
				
				if(kill.containsKey(current.getCode().get(0)))
				{
					for(CASL.Code c : kill.get(current.getCode().get(0)).keySet())
					{
						tmp.remove(c);
					}
				}
				
				if(out.containsKey(current.getCode().get(0)))
				{
					out.get(current.getCode().get(0)).putAll(tmp);
				}
				else
				{
					out.put(current.getCode().get(0), tmp);
				}
				
				for(int i=1; i<current.getCode().size(); ++i)
				{
					if(!current.getCode().get(i).getInst().isGenKill())
					{
						continue;
					}
					if(in.containsKey(current.getCode().get(i)))
					{
						for(CASL.Code c : out.get(current.getCode().get(i)).keySet())
						{
							if(!in.get(current.getCode().get(i)).containsKey(c))
							{
								change=true;
							}
						}
						in.get(current.getCode().get(i)).putAll(out.get(current.getCode().get(i-1)));
					}
					else
					{
						in.put(current.getCode().get(i), out.get(current.getCode().get(i-1)));
					}
					
					if(gen.containsKey(current.getCode().get(i)))
					{
						tmp=(HashMap<CASL.Code, String>)gen.get(current.getCode().get(i)).clone();
					}
					
					if(kill.containsKey(current.getCode().get(0)))
					{
						for(CASL.Code c : kill.get(current.getCode().get(0)).keySet())
						{
							tmp.remove(c);
						}
					}
					if(out.containsKey(current.getCode().get(i)))
					{
						for(CASL.Code c : out.get(current.getCode().get(i)).keySet())
						{
							if(!out.get(current.getCode().get(i)).containsKey(c))
							{
								change=true;
							}
						}
						out.get(current.getCode().get(i)).putAll(tmp);
					}
					else
					{
						out.put(current.getCode().get(i), tmp);
					}
				}
				
				current.getChild().forEach(basicBlockStack::add);
			}
		}
		while(change);
	}
	
	public void print()
	{
		System.out.println("gen");
		for(CASL.Code c : gen.keySet())
		{
			System.out.println(c.toString()+" : "+gen.get(c));
		}
		System.out.println();
		System.out.println("kill");
		for(CASL.Code c : kill.keySet())
		{
			System.out.print(c.toString()+" kills ");
			for(CASL.Code d : kill.get(c).keySet())
			{
				System.out.print(d.toString());
			}
			System.out.print("\n");
		}
		
		System.out.println();
		System.out.println("in");
		for(CASL.Code c : kill.keySet())
		{
			System.out.print(c.toString()+" : ");
			for(CASL.Code d : in.get(c).keySet())
			{
				System.out.print(d.toString());
			}
			System.out.print("\n");
		}
		
		System.out.println();
		System.out.println("out");
		for(CASL.Code c : kill.keySet())
		{
			System.out.print(c.toString()+" : ");
			for(CASL.Code d : out.get(c).keySet())
			{
				System.out.print(d.toString());
			}
			System.out.print("\n");
		}
	}
}
