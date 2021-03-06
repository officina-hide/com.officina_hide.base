package com.officina_hide.base.tools;

import java.text.DecimalFormat;
import java.util.Date;

import com.officina_hide.accounts.tools.CreateAccountPackage;
import com.officina_hide.base.common.FD_EnvData;
import com.officina_hide.base.common.FD_Logging;
import com.officina_hide.base.model.FD_DB;
import com.officina_hide.base.model.I_FD_Table;
import com.officina_hide.base.model.I_Fx_ViewItem;
import com.officina_hide.documents.tools.CreateDocumnetPackage;
import com.officina_hide.projects.tools.CreateProjectPackage;

/**
 * パッケージで使用する為の基本設定を行う<br>
 * @author ueno hideo
 * @version 1.20 新規作成<br>
 * @version 2.00 システム機能の作りこみを優先する。<br>
 * @version 2.10 ドキュメント管理機能の構築を開始する。<br>
 * @version 2.11 会計管理機能の構築を開始する<br>
 * @version 2.12 プロジェクト管理機能の構築を開始する。<br>
 * @since 2020/07/13
 * @param args 
 */
public class CreatePackageBase extends FD_DB {

	/**
	 * メイン処理<br>
	 * @author ueno hideo
	 * @since 1.20 2020/07/13
	 * @param args
	 */
	public static void main(String[] args) {
		Date StartDate = new Date();

		/*
		 * 環境情報設定
		 * FIXME データベースのログインの隠蔽化を早急に行うこと。(2020/09/14)
		 * ・環境情報ファイルの内、データベース接続、ログインについてはトークンファイルとして作成する。
		 * ・トークンファイルはローカル環境に置いて、FD_EnvData実体化毎に取り込むようにする。
		 */
		FD_EnvData env = new FD_EnvData();
		//開始メッセージ
		env.getLog().open(env, FD_Logging.LOG_INITIALIZE, FD_Logging.MODE_DEBAG);
//		env.getLog().open(env, FD_Logging.LOG_INITIALIZE, FD_Logging.MODE_NORMAL);
		env.getLog().add(env, FD_Logging.TYPE_MESSAGE, FD_Logging.MODE_NORMAL, "Start Package Base Creating");
		/*
		 * システムに関する機能を優先して構築していくこととした。(Ver 2.00 2020/08/27 ueno)
		 * ログインに必要なテーブルの作成
		 * ・ログ情報テーブル生成
		 * ・テーブル情報、テーブル項目情報、採番情報、リファレンス情報
		 * ・画面情報、画面項目情報
		 * ・ユーザー情報テーブル
		 * ・アクセスログ情報テーブル
		 */
		
		//テーブル情報
		FDTable table = new FDTable();
		table.createTable(env);
		table.addData(env, 101, "FD_Table", "テーブル情報", "");
		//採番情報
		FDNumbering num = new FDNumbering();
		num.createTable(env);
		num.add(env, I_FD_Table.Table_ID, 1000001, 0);
		//リファレンス情報
		FDReference ref = new FDReference();
		ref.createTable(env);
		ref.addBaseData(env);
		//テーブル項目情報
		FDTableColumn tableColumn = new FDTableColumn();
		tableColumn.createTable(env);
		//テーブル情報、採番情報、リファレンス情報のテーブル項目情報を登録する。
		table.addColumnData(env);
		num.addColumnData(env);
		ref.addColumnData(env);
		//テーブル項目リスト情報
		FDTableColumnList columnList = new FDTableColumnList();
		columnList.createTable(env);
		
		//クライアント情報
		FDClient client = new FDClient();
		client.createTable(env);
		
		//DB対応ログ情報
		FDLog log = new FDLog();
		log.createTable(env);
		//プロセス情報
		FDProcess process = new FDProcess();
		process.createTable(env);
		//新規プロセス情報追加
		process.startProcess(env, "SystemManage");
		
		//ログモードを変更。(テスト中のみ:初期構築はファイルとする。）
		env.getLog().open(env, FD_Logging.LOG_DB_OUT, FD_Logging.MODE_DEBAG);
		
		//画面情報
		FxView view = new FxView();
		view.createTable(env);
		//画面項目種別をリファレンス情報に登録する。
		ref.addData(env, I_Fx_ViewItem.VIEWTYPE_ID_FX_TEXT, "テキスト項目");
		ref.addData(env, I_Fx_ViewItem.VIEWTYPE_ID_FX_NUMBER, "数値項目");
		ref.addData(env, I_Fx_ViewItem.VIEWTYPE_ID_FX_TEXTFIELD, "複数行テキスト");
		ref.addData(env, I_Fx_ViewItem.VIEWTYPE_ID_FX_DATE, "日付");
		ref.addData(env, I_Fx_ViewItem.VIEWTYPE_ID_FX_TABLE, "テーブル");
		ref.addData(env, I_Fx_ViewItem.VIEWTYPE_ID_FX_LIST, "リスト");
		//画面項目情報
		FxViewItem viewItem = new FxViewItem();
		viewItem.createTable(env);
		
		
//		view.addData(env,  "Fx_Login", "ログイン画面");
//		view.addData(env,  "Fx_Menu", "総合メニュー画面"); 
//		int viewItemId = view.addData(env,  "Fx_TableInfoemation", "テーブル情報画面", 700, 300, I_FD_Table.Table_Name);
//		view.addData(env,  "Fx_View", "画面情報画面");
		
//		viewItem.addData(env, viewItemId, I_FD_Table.COLUMNNAME_TABLE_NAME, "テーブル名"
//				, I_Fx_ViewItem.VIEWTYPE_ID_FX_TEXT);
//		viewItem.addData(env, viewItemId, I_FD_Table.COLUMNNAME_FD_NAME, "テーブル表示名"
//				, I_Fx_ViewItem.VIEWTYPE_ID_FX_TEXT);
//		viewItem.addData(env, viewItemId, I_FD_Table.COLUMNNAME_FD_COMMENT, "説明"
//				, I_Fx_ViewItem.VIEWTYPE_ID_FX_TEXTFIELD);

		
		/*
		 * ドキュメント管理に必要な設定を行う。
		 * @sinse 2.10 2020/09/12
		 */
		new CreateDocumnetPackage(env);
		
		/*
		 * 会計管理に必要な設定を行う。
		 * @sinse 2.11 2020/09/12
		 * 今回のバージョンでは、現金出納を中心にパッケージを作成する。
		 */
		new CreateAccountPackage(env);
		
		/*
		 * プロジェクト管理に必要な設定を行う。
		 * @sinse 2.12 2020/09/28
		 */
		new CreateProjectPackage(env);
		
//		CreateUserTalbe createUserTable = new CreateUserTalbe();
//		createUserTable.createUserTable(env);
//		createUserTable.addData(env, 100, "System", "admin");
		
		//終了メッセージ
		env.getLog().add(env, FD_Logging.TYPE_MESSAGE, FD_Logging.MODE_NORMAL, "Package Base Creating is completed!!");
		double startTime = StartDate.getTime();
		double endTime = new Date().getTime();
		double elapseTime = (endTime - startTime) / 1000;
		DecimalFormat df = new DecimalFormat("0.000");
		env.getLog().add(env, FD_Logging.TYPE_MESSAGE, FD_Logging.MODE_NORMAL, "elapsed time " + df.format(elapseTime) + " Seconds");
		env.getLog().close();
	}

}
