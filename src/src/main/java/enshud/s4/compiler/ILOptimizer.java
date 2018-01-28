package enshud.s4.compiler;

public class ILOptimizer
{
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
