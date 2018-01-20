package enshud.s4.compiler;

public class RegisterAllocator
{
	private ControlFlowGraph controlFlowGraph;
	String [] register=new String[8];

	public RegisterAllocator(final CASL casl)
	{
		controlFlowGraph=new ControlFlowGraph(casl);
		System.out.print(controlFlowGraph.toString());
	}


}
