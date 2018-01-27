package enshud.s4.compiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class RegisterAllocator
{
	private CASL.OperandElement allocate(final String variable)
	{
		int lowestIndex=0;
		int priorityLowest=Integer.MAX_VALUE;

		// variable has been allocated to registers[i]
		for(int i=0; i<registers.length; i++)
		{
			if(registers[i].variable.equals(variable))
			{
				registers[i]=new Register(variable);
				return new CASL.OperandElement("GR"+(i+1), CASL.OperandElement.Attribute.register);
			}
			if(priorityLowest>registers[i].priority)
			{
				lowestIndex=i;
				priorityLowest=registers[i].priority;
			}
		}

		CASL.OperandElement retReg=new CASL.OperandElement("GR"+(lowestIndex+1), CASL.OperandElement.Attribute.register);

		int lowestMemoryIndex=-1;
		int variableMemoryIndex=-1;

		for(int i=0; i<memory.size(); ++i)
		{
			if(memory.get(i).equals(variable))
			{
				variableMemoryIndex=i;
			}

			if(memory.get(i).equals(registers[lowestIndex].variable))
			{
				lowestMemoryIndex=i;
			}

			if(lowestMemoryIndex!=-1 && variableMemoryIndex!=-1)
			{
				break;
			}
		}

		if(priorityLowest!=0)
		{
			if(lowestMemoryIndex==-1)
			{
				lowestMemoryIndex=memory.size();
				memory.add(registers[lowestIndex].variable);
			}

			casl.insertCode(cur, new CASL.Code(CASL.Inst.ST, retReg, new CASL.OperandElement("MEM"+lowestMemoryIndex, CASL.OperandElement.Attribute.address)));
			++cur;
		}

		if(variableMemoryIndex!=-1)
		{
			casl.insertCode(cur, new CASL.Code(CASL.Inst.LD, retReg, new CASL.OperandElement("MEM"+variableMemoryIndex, CASL.OperandElement.Attribute.address)));
			++cur;
		}

		registers[lowestIndex]=new Register(variable);
		return retReg;
	}

	private ControlFlowGraph controlFlowGraph;
	private ArrayList<String> memory=new ArrayList<>();
	private Register[] registers=new Register[7];
	private CASL casl;
	private int cur=0;
	private ArrayDeque<Register[]> registersStack=new ArrayDeque<>();


	public RegisterAllocator(final CASL casl)
	{
		Arrays.fill(registers, new Register());
		this.casl=casl;
		controlFlowGraph=new ControlFlowGraph(casl);
		//System.out.print(controlFlowGraph.toString());
	}

	public void run()
	{
		CASL.Operand operand;

		for(cur=0; cur<casl.getMain().size(); ++cur)
		{
			if(casl.getMain().get(cur).getInst()==CASL.Inst.RPUSH)
			{
				registersStack.addLast(registers);
				continue;
			}
			
			if(casl.getMain().get(cur).getInst()==CASL.Inst.RPOP)
			{
				registers=registersStack.getLast();
				continue;
			}
			operand=casl.getMain().get(cur).getOperand();

			for(int i=0; i<operand.length(); ++i)
			{
				if(operand.get(i).getAttribute()==CASL.OperandElement.Attribute.register)
				{
					casl.getMain().get(cur).getOperand().setElement(i, allocate(operand.get(i).getELementName()));
				}
			}

			if(operand.length()==3 && operand.get(0).getAttribute()==CASL.OperandElement.Attribute.register)
			{

			}
		}

		IntStream.range(0, memory.size()).forEach(i->casl.addStorage(new CASL.OperandElement("MEM"+i, CASL.OperandElement.Attribute.address), 1));
		return;
	}

	public CASL getCasl()
	{
		return casl;
	}
	
	private static class Register
	{
		private static int priorityCounter=0;
		private String variable="";
		private int priority=0;
		private boolean valueHasChanged=false;

		public Register(){}

		public Register(final String variable)
		{
			this.variable=variable;
			++priorityCounter;
			this.priority=priorityCounter;
		}

		public boolean isInUse()
		{
			return !variable.isEmpty();
		}
	}
}
