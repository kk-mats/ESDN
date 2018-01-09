package enshud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import enshud.casl.CaslSimulator;
import enshud.s1.lexer.Lexer;
import enshud.s2.parser.Parser;
import enshud.s3.checker.Checker;
import enshud.s4.compiler.Compiler;

/**
 * !!! このコードは編集禁止 !!!
 * 
 */
public class Main {
	private static final String TMP_OUT_CAS = "tmp/out.cas";

	/**
   * トラブル対策用のmain実行用クラス （サーバ用）． 
   * 
	 * 実行例：
	 * $ cd $WORKSPACE%/enshud
	 * $ java -cp build/classes:lib/* enshud.Main lexer    data/pas/normal01.pas tmp/out.ts
	 * $ java -cp build/classes:lib/* enshud.Main parser   data/ts/normal01.ts
	 * $ java -cp build/classes:lib/* enshud.Main checker  data/ts/normal01.ts
	 * $ java -cp build/classes:lib/* enshud.Main compiler data/ts/normal01.ts tmp/out.ans
	 * $ java -cp build/classes:lib/* enshud.Main compiler data/ts/normal10.ts tmp/out.ans 36 48
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			throw new IllegalArgumentException("specify [lexer|parser|checker|compiler]");
		}
		final String component = args[0];
		final String inputFileName = args[1];

		if ("lexer".equals(component)) {
			if (args.length < 3) {
				throw new IllegalArgumentException("specify output file name");
			}
			final String outputFileName = args[2];
			new Lexer().run(inputFileName, outputFileName);
		} else if ("parser".equals(component)) {
			new Parser().run(inputFileName);
		} else if ("checker".equals(component)) {
			new Checker().run(inputFileName);
		} else if ("compiler".equals(component)) {
			if (args.length < 3) {
				throw new IllegalArgumentException("specify output file name");
			}
			final String outputFileName = args[2];
			final String[] params = shiftArray(args, 2);
			new Compiler().run(inputFileName, TMP_OUT_CAS);
			CaslSimulator.run(TMP_OUT_CAS, outputFileName, params);
		}
	}

	private static String[] shiftArray(final String[] args, final int n) {
		final List<String> list = new ArrayList<String>(Arrays.asList(args));
		for (int i = 0; i <= n; i++) {
			list.remove(0);
		}
		return list.toArray(new String[list.size()]);
	}
}
