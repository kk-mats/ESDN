package enshud.s4.compiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class RegisterAllocator
{
	private ArrayList<CASL.Code> newMain=new ArrayList<>();
	//private int cur=0;
	private ArrayDeque<Register[]> registersStack=new ArrayDeque<>();
	
	public RegisterAllocator(final CASL casl)
	{
		clearAll();
		this.casl=casl;
		controlFlowGraph=new ControlFlowGraph(casl);
		//System.out.print(controlFlowGraph.toString());
	}
	
	private CASL.OperandElement allocate(final String variable)
	{
		int lowestIndex=0;
		int priorityLowest=Integer.MAX_VALUE;

		for(int i=0; i<registers.length; i++)
		{
			// variable has been allocated to registers[i]
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
			
			newMain.add(new CASL.Code(CASL.Inst.ST, retReg, new CASL.OperandElement("MEM"+lowestMemoryIndex, CASL.OperandElement.Attribute.address)));
			//casl.insertCode(cur, new CASL.Code(CASL.Inst.ST, retReg, new CASL.OperandElement("MEM"+lowestMemoryIndex, CASL.OperandElement.Attribute.address)));
			//++cur;
		}

		if(variableMemoryIndex!=-1)
		{
			newMain.add(new CASL.Code(CASL.Inst.LD, retReg, new CASL.OperandElement("MEM"+variableMemoryIndex, CASL.OperandElement.Attribute.address)));
			//casl.insertCode(cur, new CASL.Code(CASL.Inst.LD, retReg, new CASL.OperandElement("MEM"+variableMemoryIndex, CASL.OperandElement.Attribute.address)));
			//++cur;
		}

		registers[lowestIndex]=new Register(variable);
		return retReg;
	}

	private ControlFlowGraph controlFlowGraph;
	private ArrayList<String> memory=new ArrayList<>();
	private Register[] registers=new Register[7];
	private CASL casl;
	
	private void clearAll()
	{
		Arrays.fill(registers, new Register());
	}
	
	private void saveAll()
	{
		IntStream.range(0, registers.length).forEach(this::save);
	}
	
	private void save(final int i)
	{
		if(registers[i].variable.isEmpty())
		{
			return;
		}
		for(int j=0; j<memory.size(); ++j)
		{
			if(memory.get(j).equals(registers[i].variable))
			{
				newMain.add(new CASL.Code(CASL.Inst.ST, new CASL.OperandElement("GR"+(i+1), CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+j, CASL.OperandElement.Attribute.address)));
				return;
			}
		}
		newMain.add(new CASL.Code(CASL.Inst.ST, new CASL.OperandElement("GR"+(i+1), CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+memory.size(), CASL.OperandElement.Attribute.address)));
		memory.add(registers[i].variable);
	}
	
	public void run()
	{
		ArrayDeque<ControlFlowGraph.BasicBlock> basicBlockStack=new ArrayDeque<>();
		basicBlockStack.push(controlFlowGraph.getRoot());
		CASL.Operand operand;
		ControlFlowGraph.BasicBlock current;
		
		while(!basicBlockStack.isEmpty())
		{
			clearAll();
			current=basicBlockStack.pop();
			System.out.print("--------------------------------\n");
			if(current.isVisited())
			{
				continue;
			}
			for(CASL.Code c : current.getCode())
			{
				c.setComment(c.toString());
				if(c.getInst()==CASL.Inst.RPUSH)
				{
					Register[] tmp=Arrays.copyOf(registers, registers.length);
					registersStack.push(tmp);
					newMain.add(c);
					continue;
				}
				
				if(c.getInst()==CASL.Inst.RPOP)
				{
					Register[] tmp=registersStack.pop();
					registers=Arrays.copyOf(tmp, tmp.length);
					newMain.add(c);
					continue;
				}
				operand=c.getOperand();
				CASL.OperandElement reg;
				
				for(int i=0; i<operand.length(); ++i)
				{
					if(operand.get(i).getAttribute()==CASL.OperandElement.Attribute.register && operand.get(i).getELementName().startsWith("@"))
					{
						reg=allocate(operand.get(i).getELementName());
						operand.setElement(i, reg);
					}
				}
				
				if(operand.length()==3 && operand.get(0).getAttribute()==CASL.OperandElement.Attribute.register)
				{
				
				}
				newMain.add(c);
			}
			
			// inst r1,r2
			// jump
			// [clearAll]
			if(newMain.get(newMain.size()-1).getInst().isJump())
			{
				CASL.Code tmp=newMain.remove(newMain.size()-1);
				saveAll();
				newMain.add(tmp);
			}
			else
			{
				saveAll();
			}
			
			for(ControlFlowGraph.BasicBlock next : current.getNext())
			{
				basicBlockStack.push(next);
			}
		}
		
		IntStream.range(0, memory.size()).forEach(i->casl.addStorage(new CASL.OperandElement("MEM"+i, CASL.OperandElement.Attribute.address), 1));
		return;
	}
/*
	public void _run()
	{
		CASL.Operand operand;

		for(cur=0; cur<casl.getMain().size(); ++cur)
		{
			casl.getMain().get(cur).setComment(casl.getMain().get(cur).toString());
			if(casl.getMain().get(cur).getInst()==CASL.Inst.RPUSH)
			{
				Register[] tmp=Arrays.copyOf(registers, registers.length);
				registersStack.push(tmp);
				
				continue;
			}
			
			if(casl.getMain().get(cur).getInst()==CASL.Inst.RPOP)
			{
				Register[] tmp=registersStack.pop();
				registers=Arrays.copyOf(tmp, tmp.length);
				continue;
			}
			operand=casl.getMain().get(cur).getOperand();

			for(int i=0; i<operand.length(); ++i)
			{
				if(operand.get(i).getAttribute()==CASL.OperandElement.Attribute.register && operand.get(i).getELementName().startsWith("@"))
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
	}*/

	public CASL getCasl()
	{
		casl.setMain(newMain);
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
		
		public boolean isEmpty()
		{
			return !variable.isEmpty();
		}
		
		public String toString()
		{
			return variable+":"+priority;
		}
	}
}
