package enshud.s4.compiler;

import java.util.ArrayList;
import java.util.HashMap;

public class PeepHoleOptimizer
{
	ArrayList<CASL.Code> newmain;
	ArrayList<CASL.Code> main;
	private CASL casl;
	private HashMap<String, String> labelAlias=new HashMap<>();
	private boolean change=false;
	
	public PeepHoleOptimizer(final CASL casl)
	{
		this.casl=casl;
		main=casl.getMain();
		do
		{
			newmain=new ArrayList<>();
			change=false;
			labelRename();
			main=newmain;
			System.out.print("t");
		}
		while(change);
		System.out.println(labelAlias.toString());
	}
	
	public CASL getCasl()
	{
		casl.setMain(newmain);
		return casl;
	}
	
	public void labelRename()
	{
		for(int i=0; i<main.size(); ++i)
		{
			if(!main.get(i).getLabel().isEmpty() && main.get(i).getInst()==CASL.Inst.NOP)
			{
				String to=main.get(i).getLabel();
				while(!main.get(i+1).getLabel().isEmpty() && main.get(i).getInst()==CASL.Inst.NOP)
				{
					++i;
					labelAlias.put(main.get(i).getLabel(), to);
				}
				++i;
				main.get(i).setLabel(to);
				change=true;
			}
			
			if(main.get(i).getInst().isJump() && labelAlias.containsKey(main.get(i).getOperand().get(0).getELementName()))
			{
				System.out.println(main.get(i).getOperand().get(0).getELementName()+"->"+labelAlias.get(main.get(i).getOperand().get(0).getELementName()));
				main.get(i).getOperand().setElement(0, new CASL.OperandElement(labelAlias.get(main.get(i).getOperand().get(0).getELementName()), CASL.OperandElement.Attribute.address));
				change=true;
			}
			
			newmain.add(main.get(i));
		}
	}
}
