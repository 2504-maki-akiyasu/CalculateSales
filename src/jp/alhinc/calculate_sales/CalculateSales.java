package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File("C:\\Users\\trainee1343\\Desktop\\売り上げ集計課題").listFiles();

		//先にファイルの情報を格納する List(ArrayList) を宣言
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			//files[i].getName()　files[i]の名前を取得

			if((files[i].getName()).matches("^[0-9]{8}+.+rcd$")) {
				//"[0-9]{8}+.+rcd$"(0～9の8桁の数字+.+rcd)の名前になっているファイルのみ、List(ArrayList)に追加
				rcdFiles.add(files[i]);

			}

		}

		for(int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;

			try {
				FileReader fr;

				fr = new FileReader(rcdFiles.get(i));

				br = new BufferedReader(fr);

				List<String> list = new ArrayList<>();

				String line;

				while((line = br.readLine()) != null) {
					//読んだものをリストに入れる
					list.add(line);
				}

				//Q ここ時点での　リスト　って中身がどうなってるっけ
				//→売上ファイルに記述されている2行がString型で入っている
				//Q 次何をしたいんだっけ
				//→①計算出来るようにリストの売上金額を String → Long → iong の順に型変換
				//　②branchSalesの売上金額に売上ファイルの金額を加算
				//　③加算結果を再度branchSalesの売上金額に戻す

				long fileSale = Long.parseLong(list.get(1));

				Long saleAmount = (branchSales.get(list.get(0))) + (fileSale);

				branchSales.put(list.get(0), saleAmount);

			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				// e.printStackTrace();
				System.out.println(UNKNOWN_ERROR);
				return;

			}finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}

		}



		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile
	(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//行を","で区切る→items[0]には,までの支店コード、items[1]には,より後ろの支店名が格納
				String[] items = line.split(",");

				//items[0]:支店コード　items[1]:支店名　0L:Long型の売上金額の初期値0
				branchNames.put(items[0],items[1]);
				branchSales.put(items[0],0L);

				System.out.println(line);

			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile
	(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		//最初にファイルを作成　()内で作成するファイルのパス、名称を定義
		File writefile = new File(path,FILE_NAME_BRANCH_OUT);
		BufferedWriter bw = null;

			try {
				bw = new BufferedWriter(new FileWriter(writefile));

				//branchNamesに記載されているkeyの数だけファイル内に出力→改行を繰り返し
				for(String key:branchNames.keySet()) {
					bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
					bw.newLine();
				}

			}catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return false;

			}finally{
				if(bw != null) {
					try {
						// ファイルを閉じる
						bw.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return false;
					}
				}

			}
		return true;
	}

}
