package enshud.s4.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class RegisterAllocator
{
	public static class Register
	{
		private String variable="";
		private int priority=0;
		private static int priorityCounter=0;

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

	private ControlFlowGraph controlFlowGraph;
	private ArrayList<String> memory=new ArrayList<>();
	private Register[] registers=new Register[7];
	private CASL casl;
	private int cur=0;


	public RegisterAllocator(final CASL casl)
	{
		Arrays.fill(registers, new Register());
		this.casl=casl;
		controlFlowGraph=new ControlFlowGraph(casl);
		System.out.print(controlFlowGraph.toString());
	}

	public void run()
	{
		CASL.Operand operand;
		for(cur=0; cur<casl.getMain().size(); ++cur)
		{
			operand=casl.getMain().get(cur).getOperand();
			for(int i=0; i<operand.length(); ++i)
			{
				if(operand.get(i).getAttribute()==CASL.OperandElement.Attribute.register)
				{
					casl.getMain().get(cur).getOperand().setElement(i, allocate(operand.get(i).getELementName()));
				}
			}
		}

		IntStream.range(0, memory.size()).forEach(i->casl.addStorage(new CASL.OperandElement("MEM"+i, CASL.OperandElement.Attribute.address), 1));
	}

	public CASL getCasl()
	{
		return casl;
	}

	private CASL.OperandElement allocate(final String variable)
	{
		int minIndex=0;
		int minPriority=Integer.MAX_VALUE;

		// variable has been allocated to registers[i]
		for(int i=0; i<registers.length; i++)
		{
			if(registers[i].variable.equals(variable))
			{
				return new CASL.OperandElement("GR"+(i+1), CASL.OperandElement.Attribute.register);
			}
			if(minPriority>registers[i].priority)
			{
				minIndex=i;
				minPriority=registers[i].priority;
			}
		}

		String retReg="GR"+(minIndex+1);

		// register[minIndex] has not been allocated
				// registers[minIndex] is empty
		if(!registers[minIndex].isInUse())
		{
			registers[minIndex]=new Register(variable);
			return new CASL.OperandElement(retReg, CASL.OperandElement.Attribute.register);
		}

		// registers[minIndex] has been allocated
		/*
		IntStream.range(0, memory.size())
				.filter(registers[minIndex]::equals)
				.findFirst()
				.ifPresentOrElse(
						i->casl.insertCode(cur, new CASL.Code(CASL.Inst.ST, new String[]{"GR"+minIndex, "MEM"+i})),
						()->
						{
							casl.insertCode(cur, new CASL.Code(CASL.Inst.ST, new String[]{"GR"+minIndex, "MEM"+memory.size()}));
							memory.add(registers[minIndex].variable);
						}
				);
		*/

		boolean memExists=false;
		for(int i=0; i<memory.size(); i++)
		{
			// MEM[i] is defined
			if(memory.get(i).equals(registers[minIndex]))
			{
				casl.insertCode(cur, new CASL.Code(CASL.Inst.ST, new CASL.OperandElement(retReg, CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+i, CASL.OperandElement.Attribute.address)));
				++cur;
				memExists=true;
				break;
			}
		}

		if(!memExists)
		{
			casl.insertCode(cur, new CASL.Code(CASL.Inst.ST, new CASL.OperandElement(retReg, CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+memory.size(), CASL.OperandElement.Attribute.address)));
			++cur;
			memory.add(registers[minIndex].variable);
		}

		for(int i=0; i<memory.size(); i++)
		{
			if(memory.get(i).equals(variable))
			{
				casl.insertCode(cur, new CASL.Code(CASL.Inst.LD, new CASL.OperandElement(retReg, CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+i, CASL.OperandElement.Attribute.address)));
				++cur;
			}
		}

		registers[minIndex]=new Register(variable);
		return new CASL.OperandElement(retReg, CASL.OperandElement.Attribute.register);
	}



}
