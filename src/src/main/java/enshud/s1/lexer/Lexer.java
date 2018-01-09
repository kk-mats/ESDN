package enshud.s1.lexer;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Character;
import java.util.Arrays;

public class Lexer
{	
	String line;
	int cur;
	int lineNumber;
	String output;
	
	/**
	 * サンプルmainメソッド．
	 * 単体テストの対象ではないので自由に改変しても良い．
	 */
	public static void main(final String[] args)
	{
		// normalの確認
		new Lexer().run("data/pas/noy3.pas", "tmp/out1.ts");
		//new Lexer().run("data/pas/normal04.pas", "tmp/out2.ts");
	}
	
	public void addOutput(String string, TSToken TSToken)
	{
		String s=string+"\t"+TSToken.toString()+"\t"+TSToken.ordinal()+"\t"+lineNumber+"\n";
		//System.out.print(s);
		output+=s;
	}
	
	/**
	 * TODO
	 * 
	 * 開発対象となるLexer実行メソッド．
	 * 以下の仕様を満たすこと．
	 * 
	 * 仕様:
	 * 第一引数で指定されたpasファイルを読み込み，トークン列に分割する．
	 * トークン列は第二引数で指定されたtsファイルに書き出すこと．
	 * 正常に処理が終了した場合は標準出力に"OK"を，
	 * 入力ファイルが見つからない場合は標準エラーに"File not found"と出力して終了すること．
	 * 
	 * @param inputFileName 入力pasファイル名
	 * @param outputFileName 出力tsファイル名
	 */
	public void run(final String inputFileName, final String outputFileName)
	{
		lineNumber=0;
		output="";
		boolean found;

		
		try
		{
			File in=new File(inputFileName);
			File out=new File(outputFileName);
			
			FileReader fr=new FileReader(in);
			BufferedReader br=new BufferedReader(fr);
			
			FileWriter fw=new FileWriter(out);
			BufferedWriter bw=new BufferedWriter(fw);
			
			
			String buffer;
			
			while((line=br.readLine())!=null)
			{
				++lineNumber;
				//System.out.println(lineNumber+": |"+line) ;
				
				for(cur=0; cur<line.length();)
				{
					buffer="";
					if(line.charAt(cur)=='{')
					{
						++cur;
						while(cur<line.length() && line.charAt(cur)!='}')
						{
							++cur;
						}
						++cur;
					}
					else if(line.charAt(cur)=='\'')
					{
						buffer+="\'";
						++cur;
						
						while(cur<line.length() && line.charAt(cur)!='\'')
						{
							buffer+=line.charAt(cur);
							++cur;
						}
						
						if(cur<line.length() && line.charAt(cur)=='\'')
						{
							buffer+="\'";
						}
						
						addOutput(buffer, TSToken.SSTRING);
						++cur;
					}
					else if(Character.isDigit(line.charAt(cur)))
					{
						while(cur<line.length() && Character.isDigit(line.charAt(cur)))
						{
							buffer+=line.charAt(cur);
							++cur;
						}
						addOutput(buffer, TSToken.SCONSTANT);
					}
					else if(!Character.isWhitespace(line.charAt(cur)))
					{	
						found=false;
						for(TSToken TSToken : Arrays.copyOfRange(TSToken.values(), 0, TSToken.SDOT.ordinal()+1))
						{
							if(line.indexOf(TSToken.label(), cur)==cur)
							{
								if(!((TSToken==TSToken.SLESS || TSToken==TSToken.SGREAT || TSToken==TSToken.SCOLON) && line.charAt(cur+1)=='=') &&
										(cur+TSToken.label().length()>=line.length() ||
										TSToken.ordinal()>TSToken.SWRITELN.ordinal() ||
										!Character.isLetterOrDigit(line.charAt(cur+TSToken.label().length()))
								))
								{
									//System.out.print("match ["+line.charAt(cur)+"]"+TSToken.toString());
									addOutput(TSToken.label(), TSToken);
									found=true;
									cur+=TSToken.label().length();
									break;
								}
							}
							else if(line.indexOf("/", cur)==cur)
							{
								addOutput("/", TSToken.SDIVD);
								found=true;
								++cur;
								break;
							}
						}
						
						if(!found)
						{
							while(cur<line.length() && Character.isLetterOrDigit(line.charAt(cur)))
							{
								buffer+=line.charAt(cur);
								++cur;
							}
							addOutput(buffer, TSToken.SIDENTIFIER);
						}
					}
					else
					{
						++cur;
					}
				}
			}
			
			bw.write(output);
			System.out.println("OK");
			br.close();
			fr.close();
			bw.close();
			fw.close();
		}
		catch(FileNotFoundException e)
		{
			System.err.println("File not found");
		}
		catch(IOException e)
		{
			System.err.println(e);
		}

	}
}
