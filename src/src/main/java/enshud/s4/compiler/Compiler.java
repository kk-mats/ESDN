package enshud.s4.compiler;

public class Compiler {
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
	public static final boolean debug=false;

	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args)
	{
		// Compilerを実行してcasを生成する
		//new Compiler().run("data/ts/normal08.ts", "lib/out.cas");
		new enshud.s1.lexer.Lexer().run("mytest.pas", "mytest.ts");
		new Compiler().run("mytest.ts", "lib/out.cas");

		// CaslSimulatorクラスを使ってコンパイルしたcasを，CASLアセンブラ & COMETシミュレータで実行する
		//CaslSimulator.run("tmp/out.cas", "tmp/out.ans");
	}
	public void run(final String inputFileName, final String outputFileName)
	{
		new ASTCompiler(inputFileName, outputFileName).compile();
	}
}
