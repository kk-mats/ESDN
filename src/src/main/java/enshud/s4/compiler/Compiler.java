package enshud.s4.compiler;

import enshud.s3.checker.ASTChecker;
import enshud.s3.checker.ASTConstructor;

public class Compiler {
	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args) {
		// Compilerを実行してcasを生成する
		new Compiler().run("data/ts/normal01.ts", "tmp/out.cas");
		
		// CaslSimulatorクラスを使ってコンパイルしたcasを，CASLアセンブラ & COMETシミュレータで実行する
		//CaslSimulator.run("tmp/out.cas", "tmp/out.ans", "36", "48");
	}

	/**
	 * TODO
	 * 
	 * 開発対象となるCompiler実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたtsファイルを読み込み，CASL IIプログラムにコンパイルする．
	 * コンパイル結果のCASL IIプログラムは第二引数で指定されたcasファイルに書き出すこと．
	 * 構文的もしくは意味的なエラーを発見した場合は標準エラーにエラーメッセージを出力すること．
	 * （エラーメッセージの内容はChecker.run()の出力に準じるものとする．）
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力tsファイル名
	 * @param outputFileName 出力casファイル名
	 */
	public void run(final String inputFileName, final String outputFileName)
	{
		ASTConstructor c=new ASTConstructor(inputFileName);
		if(c.success())
		{
			ASTChecker checker=new ASTChecker();
			checker.run(c.getAST());
			AST2CASL translator=new AST2CASL();
			System.out.println(checker.getTable().toString());
			translator.run(c.getAST());
			//System.out.print(translator.getIL().toString());
			//ILCompiler compiler=new ILCompiler(checker.getTable());
			//compiler.run(c.getAST());
			//System.out.print(compiler.toString());
		}

	}
}
