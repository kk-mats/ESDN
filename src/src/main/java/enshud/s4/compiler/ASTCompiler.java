package enshud.s4.compiler;

import enshud.s3.checker.ASTChecker;
import enshud.s3.checker.ASTConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ASTCompiler
{

	private String inputFileName;

	public ASTCompiler(final String inputFileName)
	{
		this.inputFileName=inputFileName;
	}

	public void compile()
	{
		ASTConstructor constructor=new ASTConstructor(inputFileName);
		if(constructor.success())
		{
			ASTChecker checker=new ASTChecker();
			checker.run(constructor.getAST());
			AST2CASL translator=new AST2CASL();
			translator.run(constructor.getAST(), checker.getTable());
			//ILOptimizer optimizer=new ILOptimizer(translator.getCasl());

			try(FileWriter fw=new FileWriter(new File("tmp.txt")))
			{
				fw.write(checker.getTable().toString());

				RegisterAllocator registerAllocator=new RegisterAllocator(translator.getCasl());
				registerAllocator.run();
				fw.write(registerAllocator.getCasl().toString());
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

	public void optimize()
	{

	}

	public void IL2CASL()
	{

	}
}
