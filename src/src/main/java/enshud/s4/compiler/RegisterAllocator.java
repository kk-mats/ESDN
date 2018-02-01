package enshud.s4.compiler;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class RegisterAllocator
{
	private static class Register
	{
		private static int priorityCounter=0;
		private String variable="";
		private int priority=0;
		
		public Register(){}
		
		public Register(final String variable)
		{
			this.variable=variable;
			++priorityCounter;
			this.priority=priorityCounter;
		}
		
		public String toString()
		{
			return variable+":"+priority;
		}
	}
	
	private ArrayList<CASL.Code> newMain=new ArrayList<>();
	//private int cur=0;
	private ArrayDeque<Register[]> registersStack=new ArrayDeque<>();
	private ControlFlowGraph controlFlowGraph;
	
	// Memory is map list of variable name stored in memory and flag the value has changed
	private ArrayList<AbstractMap.SimpleEntry<String, Boolean>> memory=new ArrayList<>();
	
	// Registers of CASL. This allocator uses GR1 to GR7
	private Register[] registers=new Register[7];
	
	private CASL casl;
	
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
				// reload register value when variable has changed by a function
				for(int j=0; j<memory.size(); ++j)
				{
					if(memory.get(j).getKey().equals(variable))
					{
						// variable has changed
						if(memory.get(j).getValue())
						{
							String label="";
							newMain.add(new CASL.Code(CASL.Inst.LD, new CASL.OperandElement("GR"+(i+1), CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+j, CASL.OperandElement.Attribute.address)));
						}
						break;
					}
				}
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
		
		// memory index where variable is stored
		int variableMemoryIndex=-1;
		for(int i=0; i<memory.size(); ++i)
		{
			if(memory.get(i).getKey().equals(variable))
			{
				variableMemoryIndex=i;
				break;
			}
		}
		
		if(priorityLowest!=0)
		{
			save(lowestIndex);
		}
		
		if(variableMemoryIndex!=-1)
		{
			newMain.add(new CASL.Code(CASL.Inst.LD, retReg, new CASL.OperandElement("MEM"+variableMemoryIndex, CASL.OperandElement.Attribute.address)));
			memory.get(variableMemoryIndex).setValue(false);
		}
		
		registers[lowestIndex]=new Register(variable);
		return retReg;
	}
	
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
			if(memory.get(j).getKey().equals(registers[i].variable))
			{
				newMain.add(new CASL.Code(CASL.Inst.ST, new CASL.OperandElement("GR"+(i+1), CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+j, CASL.OperandElement.Attribute.address)));
				memory.get(j).setValue(true);
				return;
			}
		}
		newMain.add(new CASL.Code(CASL.Inst.ST, new CASL.OperandElement("GR"+(i+1), CASL.OperandElement.Attribute.register), new CASL.OperandElement("MEM"+memory.size(), CASL.OperandElement.Attribute.address)));
		memory.add(new AbstractMap.SimpleEntry<>(registers[i].variable, false));
	}
	
	public void run()
	{
		ArrayDeque<ControlFlowGraph.BasicBlock> basicBlockStack=new ArrayDeque<>();
		basicBlockStack.push(controlFlowGraph.getRoot());
		CASL.Operand operand;
		ControlFlowGraph.BasicBlock current;
		Register[] regtmp;
		
		while(!basicBlockStack.isEmpty())
		{
			clearAll();
			current=basicBlockStack.pop();
			if(current.isVisited())
			{
				continue;
			}
			for(CASL.Code c : current.getCode())
			{
				c.setComment(c.toString());
				if(c.getInst()==CASL.Inst.RPUSH)
				{
					regtmp=Arrays.copyOf(registers, registers.length);
					registersStack.push(regtmp);
					newMain.add(c);
					continue;
				}
				
				if(c.getInst()==CASL.Inst.CALL)
				{
					clearAll();
					newMain.add(c);
					continue;
				}
				
				if(c.getInst()==CASL.Inst.RPOP)
				{
					saveAll();
					regtmp=registersStack.pop();
					registers=Arrays.copyOf(regtmp, regtmp.length);
					newMain.add(c);
					continue;
				}
				operand=c.getOperand();
				CASL.OperandElement reg;
				
				for(int i=0; i<operand.length(); ++i)
				{
					if(operand.get(i).getAttribute()==CASL.OperandElement.Attribute.register && operand.get(i).getELementName().startsWith("@"))
					{
						operand.setElement(i, allocate(operand.get(i).getELementName()));
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
			else if(newMain.get(newMain.size()-1).getInst()!=CASL.Inst.RET)
			{
				saveAll();
			}
			
			for(ControlFlowGraph.BasicBlock next : current.getChild())
			{
				basicBlockStack.push(next);
			}
		}
		
		controlFlowGraph.setUnvisited();
		
		IntStream.range(0, memory.size()).forEach(i->casl.addStorage(new CASL.OperandElement("MEM"+i, CASL.OperandElement.Attribute.address), 1));
		
		//System.out.print(String.join("\n", memory.stream().map(AbstractMap.SimpleEntry::getKey).collect(Collectors.toList())));
		//System.out.print("------------------------------------------");
		return;
	}
	
	public CASL getCasl()
	{
		casl.setMain(newMain);
		return casl;
	}
}
