package enshud.s4.compiler;

import enshud.s3.checker.ASTChecker;
import enshud.s3.checker.ASTConstructor;
import enshud.s3.checker.ASTPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ASTCompiler
{
	private String inputFileName;
	private String outputFileName;

	public ASTCompiler(final String inputFileName, final String outputFileName)
	{
		this.inputFileName=inputFileName;
		this.outputFileName=outputFileName;
	}

	public void compile()
	{
		ASTConstructor constructor=new ASTConstructor(inputFileName);
		if(constructor.success())
		{
			ASTChecker checker=new ASTChecker();
			checker.run(constructor.getAST());
			if(checker.success())
			{
				ASTOptimizer optimizer=new ASTOptimizer(constructor.getAST());
				ASTPrinter printer=new ASTPrinter();
				printer.run(optimizer.getAST());
				AST2CASL translator=new AST2CASL();
				translator.run(constructor.getAST(), checker.getTable());
				//ILOptimizer optimizer=new ILOptimizer(translator.getCasl());

				try(FileWriter fw=new FileWriter(new File(outputFileName)))
				{
					System.out.print(checker.getTable().toString());
					if(Compiler.debug)
					{
						//fw.write(checker.getTable().toString());
						//DataFlowAnalyze analyzer=new DataFlowAnalyze(translator.getCaslList().get(0));
						//analyzer.print();
					}
					for(CASL casl : translator.getCaslList())
					{
						RegisterAllocator registerAllocator=new RegisterAllocator(casl);
						if(!Compiler.debug)
						{
							registerAllocator.run();
							PeepHoleOptimizer peepHoleOptimizer=new PeepHoleOptimizer(registerAllocator.getCasl());
							fw.write(peepHoleOptimizer.getCasl().toString());
						}
						else
						{
							fw.write(casl.toString());
						}
						fw.write("\n");
					}
					fw.write(translator.getLibraries());
				}
				catch(IOException e)
				{
					System.out.print(e);
				}

				//System.out.print(translator.getIL().toString());
				//ILCompiler compiler=new ILCompiler(checker.getTable());
				//compiler.run(constructor.getAST());
				//System.out.print(compiler.toString());
			}
		}
	}

	public void optimize()
	{

	}

	public void IL2CASL()
	{

	}
}
