package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private static final String FILE_NOT_SERIALNUMBER = "売上ファイル名が連番になっていません";
	private static final String OVER_TEN_DIGIT = "合計⾦額が10桁を超えました";
	private static final String INVALID_CODE = "の支店コードが不正です";
	private static final String NOT_TWO_LINES = "のフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//コマンドライン引数が1つでない場合エラー処理
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();

		//先にファイルの情報を格納する List(ArrayList) を宣言
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {

			//files[i].getName():files[i]の名前を取得
			if(files[i].isFile() && (files[i].getName()).matches("^[0-9]{8}.rcd$")) {
				//files[i]がファイルであり、かつ"[0-9]{8}.rcd$"(0～9の8桁の数字.rcd)の名前になっているファイルのみ
				//List(ArrayList)に追加
				rcdFiles.add(files[i]);
			}

		}

		//売上ファイルを保持しているリストをソート
		Collections.sort(rcdFiles);

		for(int i = 0; i < rcdFiles.size() - 1 ; i++) {

			//i番目(former)とその次(latter)のファイル名の先頭から8文字をint型に変換して定義
			int former = Integer.parseInt((rcdFiles.get(i).getName()).substring(0, 8));
			int latter = Integer.parseInt((rcdFiles.get(i + 1).getName()).substring(0, 8));

			//売上ファイル名が連番でない場合エラー処理
			if((latter - former) != 1) {
				System.out.println(FILE_NOT_SERIALNUMBER);
				return;
			}
		}

		for(int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;

			try {
				FileReader fr;
				fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);

				List<String> sales = new ArrayList<>();

				String line;

				while((line = br.readLine()) != null) {
					//読んだものをリストに入れる
					sales.add(line);
				}

				//売上ファイルの中身が2行でない場合エラー処理
				if(sales.size() != 2) {
					System.out.println(rcdFiles.get(i).getName() + NOT_TWO_LINES);
					return;
				}

				//支店に該当がない場合エラー処理
				if(!branchNames.containsKey(sales.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + INVALID_CODE);
					return;
				}

				//売上ファイルの金額が数字でない場合エラー処理
				if(!sales.get(1).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				long fileSale = Long.parseLong(sales.get(1));

				Long saleAmount = branchSales.get(sales.get(0)) + fileSale;
				//売上金額を加算した後、10桁を越えたらエラー処理
				if(saleAmount >= 10000000000L) {
					System.out.println(OVER_TEN_DIGIT);
					return;
				}

				branchSales.put(sales.get(0), saleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
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
			//branch.lstが該当パスに存在しない場合エラー処理
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//行を","で区切る→items[0]には,までの支店コード、items[1]には,より後ろの支店名が格納
				String[] items = line.split(",");
				//支店定義ファイルのフォーマットが不正な場合エラー処理
				if(items.length != 2 || !items[0].matches("^[0-9]{3}")) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				//items[0]:支店コード　items[1]:支店名　0L:Long型の売上金額の初期値0
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
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
		BufferedWriter bw = null;
			try {
				//最初にファイルを作成　()内で作成するファイルのパス、名称を定義
				File writefile = new File(path, fileName);
				bw = new BufferedWriter(new FileWriter(writefile));

				//branchNamesに記載されているkeyの数だけファイル内に出力→改行を繰り返し
				for(String key:branchNames.keySet()) {
					bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
					bw.newLine();
				}

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return false;

			} finally {
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
