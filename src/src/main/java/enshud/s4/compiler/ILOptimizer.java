package enshud.s4.compiler;

import java.util.ArrayList;

public class ILOptimizer
{
	CASL casl;

	public class BasicBlock
	{
		private String name;
		private ArrayList<CASL.Code> code;
		private BasicBlock next;

		BasicBlock(final ArrayList<CASL.Code> code)
		{
			this.code=code;
		}
	}

	public class ControlFlowGraph
	{
		private BasicBlock root;
		private int cur=0;

		public ControlFlowGraph()
		{
		}

		private BasicBlock construct()
		{
			ArrayList<CASL.Code> code=new ArrayList<>();
			for(; cur<casl.getMain().size(); ++cur)
			{
				if(!casl.getMain().get(cur).getLabel().isEmpty())
				{
				}

				code.add(casl.getMain().get(cur));

				if(casl.getMain().get(cur).getInst().isJump())
				{

				}
			}
		}
	}

	public ILOptimizer(final CASL casl)
	{
		this.casl=casl;
	}
}
