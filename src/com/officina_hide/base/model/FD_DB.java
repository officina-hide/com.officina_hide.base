package com.officina_hide.base.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.officina_hide.base.common.FD_EnvData;
import com.officina_hide.base.common.FD_Item;
import com.officina_hide.base.common.FD_JavaDocParam;
import com.officina_hide.base.common.FD_Logging;
import com.officina_hide.base.common.FD_WhereData;


/**
 * データベース基本操作<br>
 * <p>本クラスでは、データベースの基本的な操作に関する機能を提供します。<br>
 * This class provides functions related to basic database operations.</p>
 * @author ueno hideo
 * @version 1.20 新規作成
 * @version 2.11 SetValueに環境情報(FD_envData)を追加、getTableColumnID追加
 * @since 2020/07/15
 */
public class FD_DB implements I_DB {
	
	/** 項目リスト */
	protected List<FD_Item> itemList = new ArrayList<>();
	
	/**
	 * データベース接続情報
	 */
	protected static Connection conn;
	/**
	 * 日付フォーマット
	 */
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public FD_DB() {
	}
	
	/**
	 * コンストラクタ－<br>
	 * <p>インスタンス時にデータベースの操作に必要な環境情報を取得します。<br>
	 * Acquires the environment information required for database operations during instance.</p>
	 * @author ueno hideo
	 * @since 1.20 2020/07/15
	 * @param env 環境情報
	 */
	public FD_DB(FD_EnvData env) {
//		this.env = env;
	}

	/**
	 * 項目リストにデータを登録する。
	 * @sinse 2.11 2020/09/19 環境情報をSetValueに追加する。
	 * @param env 環境情報
	 * @param itemName 項目名
	 * @param data データ
	 */
	public void setValue(FD_EnvData env, String itemName, Object data) {
		boolean see = false;
		for(FD_Item item : itemList) {
			if(item.getItemName().equals(itemName)) {
				see = true;
				item.setItemData(data);
				break;
			}
		}
		
		if(see == false) {
			String message = "table not found column name : "+itemName;
			env.getLog().add(env, FD_Logging.TYPE_ERROR, FD_Logging.MODE_NORMAL, message);
			new Exception(message);
		}
	}
	
	/**
	 * SQL実行<br>
	 * <p>更新系のSQL文を実行します。</p>
	 * @author ueno hideo
	 * @since 1.20 2020/07/15
	 * @param env 環境情報
	 * @param sql SQL文
	 */
	public int execute(FD_EnvData env, String sql) {
		int chk = 0;
		Statement stmt = null;
		try {
			connection(env);
			stmt = conn.createStatement();
			chk = stmt.executeUpdate(sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(stmt);
		}
		return chk;
	}

	/**
	 * テーブル情報を保存する。<br>
	 * @author officina-hide.com ueno
	 * @since 2.00 2020/08/30
	 * @param env 環境情報
	 */
	public void save(FD_EnvData env, String tableName) {
		StringBuffer sql = new StringBuffer();
		StringBuffer setitem = new StringBuffer();
		FD_WhereData where = new FD_WhereData();

		//登録日、更新日設定
		if(getDateOfValue(COLUMNNAME_FD_CREATE) == null) {
			setValue(env, COLUMNNAME_FD_CREATE, new Date());
			setValue(env, COLUMNNAME_FD_UPDATE, new Date());
			setValue(env, COLUMNNAME_FD_CREATED, env.getLoginUserID());
			setValue(env, COLUMNNAME_FD_UPDATED, env.getLoginUserID());
		} else {
			setValue(env, COLUMNNAME_FD_UPDATE, new Date());
			setValue(env, COLUMNNAME_FD_UPDATED, env.getLoginUserID());
		}
		//情報ID登録チェック
		int id = getIntOfValue(tableName+"_ID");
		if(id > 0 && InformationIDExists(env, tableName, id) == true) {
			//更新
			sql.append("UPDATE ").append(tableName).append(" SET ");
			where.getWhere().append(tableName).append("_ID = ").append(getIntOfValue(tableName+"_ID"));
		} else {
			//新規登録
			if(id == 0) {
				//情報ID発行
				setValue(env, tableName+"_ID", getNewID(env, getTableID(env, tableName)));
			}
			sql.append("INSERT INTO ").append(tableName).append(" SET ");
			where.clear();
		}

		for(FD_Item item : itemList) {
			if(setitem.toString().length() > 0) {
				setitem.append(",");
			}
			if(item.getItemData() != null) {
				switch(item.getItemType()) {
				case COLUMN_TYPE_INFORMATION_ID:
				case COLUMN_TYPE_NUMBER:
					setitem.append(item.getItemName()).append(" = ").append(getIntOfValue(item.getItemName()));
					break;
				case COLUMN_TYPE_TEXT:
				case COLUMN_TYPE_FIELD_TEXT:
					setitem.append(item.getItemName()).append(" = ").append(FD_SQ).append(getStringOfValue(item.getItemName())).append(FD_SQ);
					break;
				case COLUMN_TYPE_DATE:
					setitem.append(item.getItemName()).append(" = ")
						.append(FD_SQ).append(dateFormat.format(getDateOfValue(item.getItemName()).getTime())).append(FD_SQ);
					break;
				case COLUMN_TYPE_YESNO:
					if(item.getStringOfData().equals("YES")) {
						setitem.append(item.getItemName()).append(" = 1");
					} else {
						setitem.append(item.getItemName()).append(" = -1");
					}
					break;
				}
			} else {
				setitem.append(item.getItemName()).append(" = null");
			}
		}
		sql.append(setitem.toString()).append(" ");
		if(where.toString().length() > 0) {
			sql.append("WHERE ").append(where.toString());
		}
//		System.out.println(sql.toString());

		execute(env, sql.toString());
	}

	/**
	 * 情報ID登録チェック<br>
	 * <p>指定されたテーブルに指定された情報IDを持つ情報が登録されているか判定する。</p>
	 * @author officina-hide.com
	 * @sinse 2.11 2020/09/21
	 * @param env 環境情報
	 * @param tableName テーブル名
	 * @param id 情報ID
	 * @return 判定結果 true - 登録済、false - 未登録
	 */
	public boolean InformationIDExists(FD_EnvData env, String tableName, int id) {
		boolean chk = false;
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			sql.append("SELECT ").append(tableName).append("_ID FROM ").append(tableName).append(" ");
			sql.append("WHERE ").append(tableName).append("_ID = ").append(id).append(" ");
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				chk = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		return chk;
	}

	/**
	 * 情報抽出<br>
	 * <p>指定され条件で、指定されたテーブルから情報を行う抽出する。<br>
	 * もし、情報が複数件のときは、最初にヒットした情報を行う１件のみ返す。<br>
	 * １件も対象の情報がないときは、すべての項目の内容は不定となる。</p>
	 * @author officina-hide.com ueno
	 * @since 2.00 2020/09/07
	 * @param env 環境情報
	 * @param where 抽出条件
	 * @param tableName テーブル名称
	 */
	public void load(FD_EnvData env, FD_WhereData where, String tableName) {
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			sql.append("SELECT * FROM ").append(tableName).append(" ");
			if(where != null) {
				sql.append("WHERE ").append(where.toString()).append(" ");
			}
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				setItem(rs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
	}

	/**
	 * 情報抽出<br>
	 * <p>指定されたテーブルから指定された情報IDを持つ情報を抽出する。<br>
	 * もし、対象の情報IDを持つ情報が登録されていないときは、falseを返す。</p>
	 * @param env 環境情報
	 * @param tableName テーブル名
	 * @param id 情報ID
	 * @return 抽出結果 true - 成功、false - 失敗
	 */
	public boolean load(FD_EnvData env, String tableName, int id) {
		boolean status = false;
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			sql.append("SELECT * FROM ").append(tableName).append(" ");
			sql.append("WHERE ").append(tableName).append("_ID = ").append(id).append(" ");
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				setItem(rs);
				status = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		return status;
	}

	/**
	 * 週出結果からテーブル項目に情報を抽出セットする。<br>
	 * @author officina-hide.com
	 * @sinse 2.11 2020/09/21
	 * @param rs 抽出結果
	 */
	private void setItem(ResultSet rs) {
		for(FD_Item item : itemList) {
			try {
				switch(item.getItemType()) {
				case COLUMN_TYPE_INFORMATION_ID:
				case COLUMN_TYPE_NUMBER:
					item.setItemData(rs.getInt(item.getItemName()));
					break;
				case COLUMN_TYPE_TEXT:
				case COLUMN_TYPE_FIELD_TEXT:
					item.setItemData(rs.getString(item.getItemName()));
					break;
				case COLUMN_TYPE_DATE:
					item.setItemData(rs.getDate(item.getItemName()));
					break;
				case COLUMN_TYPE_YESNO:
					if(rs.getInt(item.getItemName()) == 1) {
						item.setItemData("YES");
					} else {
						item.setItemData("NO");
					}
				}
			} catch (SQLException e) {
				item.setItemData(null);
			}
		}
	}

	/**
	 * 項目リスト初期化<br>
	 * @author officine-hide.com ueno
	 * @since 2.00 2020/09/05
	 * @param env 環境情報
	 * @param tableId テーブル情報ID
	 */
	public void createItemList(FD_EnvData env, int tableId) {
		itemList.clear();
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			sql.append("SELECT * FROM ").append(I_FD_TableColumn.Table_Name).append(" ");
			sql.append("WHERE ").append(I_FD_TableColumn.COLUMNNAME_FD_TABLE_ID).append(" = ").append(tableId).append(" ");
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			while(rs.next()) {
				X_FD_Reference ref = new X_FD_Reference(env, rs.getInt(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_TYPE_ID));
				itemList.add(new FD_Item(rs.getString(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME)
						, null, ref.getStringOfValue(I_FD_Reference.COLUMNNAME_REFERENCE_NAME)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
	}

	/**
	 * テーブル項目情報を取得する。(int型)<br>
	 * <p>もし、項目が見つからないときもしくは数値型と違うときは、0を返す。</p>
	 * @param itemName 項目名
	 * @return 項目情報(int型)
	 */
	public int getIntOfValue(String itemName) {
		try {
			int data = 0;
			if(getItemData(itemName) != null) {
				data = (int) getItemData(itemName);
			}
			return data;
		} catch (ClassCastException e) {
			try {
				int data = Integer.parseInt((String) getItemData(itemName));
				return data;
			} catch (NumberFormatException | ClassCastException e1) {
				return 0;
			}
		}
	}

	/**
	 * テーブル項目情報を取得する。(String型)<br>
	 * <p>もし、項目が見つからないときはnullを返す</p>
	 * @param itemName 項目名
	 * @return
	 */
	public String getStringOfValue(String itemName) {
		return (String) getItemData(itemName);
	}

	/**
	 * @param itemName
	 * @return
	 */
	public Date getDateOfValue(String itemName) {
		return (Date) getItemData(itemName);
	}

	/**
	 * 指定された項目名から項目情報を抽出する。<br>
	 * @param itemName
	 * @return
	 */
	public Object getItemData(String itemName) {
		for(FD_Item item : itemList) {
			if(item.getItemName().equals(itemName)) {
				return item.getItemData();
			}
		}
		return null;
	}

	/**
	 * データベース接続<br>
	 * @author ueno hideo
	 * @param env 環境情報
	 * @since 1.20 2020/07/15
	 */
	public void connection(FD_EnvData env) {
		if(conn == null) {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				StringBuffer url  = new StringBuffer().append("jdbc:mysql://")
						.append(env.getDB_Host())
						.append(":3306/")
						.append(env.getDB_Name());
				conn = DriverManager.getConnection(url.toString(), env.getDB_User(), env.getDB_Password());
				env.getLog().add(env, FD_Logging.TYPE_MESSAGE, FD_Logging.MODE_DEBAG, "Database Connected.");
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * テーフル情報ID取得<br>
	 * <p>指定されたテーブル名のテーブル情報IDを取得する。<br></p>
	 * @author ueno hideo
	 * @since 2020/07/16
	 * @param tableName テーブル名
	 * @return テーフル情報ID
	 */
	public int getTableID(FD_EnvData env, String tableName) {
		int id = 0;
		StringBuffer sql = new StringBuffer();
		Statement stmt = null;
		ResultSet rs = null;
		sql.append("SELECT FD_Table_ID FROM FD_Table ")
			.append("WHERE Table_Name = '").append(tableName).append("'");
		try {
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				id = rs.getInt("FD_Table_ID");
			} else {
				env.getLog().add(env, FD_Logging.TYPE_ERROR, FD_Logging.MODE_NORMAL, "Table ID not found ["+tableName+"]");
				new Exception();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		return id;
	}

	/**
	 * テーブル名取得<br>
	 * @author officine-hide.com
	 * @since 2.11 2020/09/24
	 * @param env 環境情報
	 * @param tableId テーブル情報ID
	 * @return テーブル名
	 */
	public String getTableName(FD_EnvData env, int tableId) {
		String tableName = null;
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			sql.append("SELECT ").append(I_FD_Table.COLUMNNAME_TABLE_NAME).append(" FROM ").append(I_FD_Table.Table_Name).append(" ");
			sql.append("WHERE ").append(I_FD_Table.COLUMNNAME_FD_TABLE_ID).append(" = ").append(tableId);
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				tableName = rs.getString(I_FD_Table.COLUMNNAME_TABLE_NAME);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		return tableName;
	}

	/**
	 * テーブル項目情報ID取得<br>
	 * @author officina-hide.com
	 * @sinse 2.11 2020/09/21
	 * @param env 環境情報
	 * @param tableName テーブル名
	 * @param columnName テーブル項目名
	 * @return テーブル項目情報ID
	 */
	public int getTableColumnID(FD_EnvData env, String tableName, String columnName) {
		int columnId = 0;
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			sql.append("SELECT ").append(I_FD_TableColumn.COLUMNNAME_FD_TABLECOLUMN_ID).append(" FROM ").append(I_FD_TableColumn.Table_Name).append(" ");
			sql.append("LEFT JOIN ").append(I_FD_Table.Table_Name).append(" ON ")
				.append(I_FD_Table.COLUMNNAME_TABLE_NAME).append(" = ").append(FD_SQ).append(tableName).append(FD_SQ).append(" ");
			sql.append("WHERE ").append(I_FD_TableColumn.Table_Name).append(".").append(I_FD_TableColumn.COLUMNNAME_FD_TABLE_ID).append(" = ")
				.append(I_FD_Table.Table_Name).append(".").append(I_FD_Table.COLUMNNAME_FD_TABLE_ID).append(" ");
			sql.append("AND ").append(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME).append(" = ").append(FD_SQ).append(columnName).append(FD_SQ);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				columnId = rs.getInt(I_FD_TableColumn.COLUMNNAME_FD_TABLECOLUMN_ID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		
		return columnId;
	}

	/**
	 * テーブル項目情報IDよりテーブル項目名を取得する。<br>
	 * @param env 環境情報
	 * @param columnId テーブル項目情報ID
	 * @return テーブル項目名
	 */
	public String getTableColumnName(FD_EnvData env, int columnId) {
		String columnName = null;
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try {
			sql.append("SELECT ").append(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME)
				.append(" FROM ").append(I_FD_TableColumn.Table_Name).append(" ");
			sql.append("WHERE ").append(I_FD_TableColumn.COLUMNNAME_FD_TABLECOLUMN_ID).append(" = ").append(columnId);
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				columnName = rs.getString(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		return columnName;
	}

	/**
	 * データベースクローズ処理<br>
	 * @param stmt 処理ステートメント
	 */
	public void close(Statement stmt) {
		if(stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * データベースクローズ処理<br>
	 * @param rs　検索結果
	 * @param stmt 処理ステートメント
	 */
	public void close(ResultSet rs, Statement stmt) {
		try {
			if(rs != null) {
				rs.close();
			}
			if(stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * リファレンスID取得<br>
	 * @param env 環境情報
	 * @param referenceName リファレンス名
	 * @return リファレンス情報ID
	 */
	public int getReferenceID(FD_EnvData env, String referenceName) {
		int id = 0;
		Statement stmt = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT FD_Reference_ID FROM FD_Reference ");
		sql.append("Where Reference_Name = '").append(referenceName).append("'");
		try {
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				id = rs.getInt("FD_Reference_ID");
			} else {
				env.getLog().add(env, FD_Logging.TYPE_ERROR, FD_Logging.MODE_NORMAL, "Reference not found ["+referenceName+"]");
				new Exception();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		return id;
	}

	/**
	 * 新規情報ID取得<br>
	 * @author officina-hide.com ueno
	 * @since 2.00 2020/09/05
	 * @param env 環境情報
	 * @param tableId テーブル情報ID
	 * @return 情報ID
	 */
	public int getNewID(FD_EnvData env,  int tableId) {
		int number = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT Current_Number, Start_Number FROM FD_Numbering ");
		sql.append("Where FD_Table_ID = ").append(tableId).append(" ");
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if(rs.next()) {
				if(rs.getInt("Current_Number") == 0) {
					number = rs.getInt("Start_Number");
				} else {
					number = rs.getInt("Current_Number") + 1;
				}
			} else {
				env.getLog().add(env, FD_Logging.TYPE_ERROR, FD_Logging.MODE_NORMAL, "Numbering Table not found ["+tableId+"]");
				new Exception();
			}
			//採番情報更新
			sql = new StringBuffer();
			sql.append("UPDATE FD_Numbering SET ");
			sql.append("Current_Number = ").append(number).append(",");
			sql.append("FD_Update = '").append(dateFormat.format(new Date())).append("'").append(",");
			sql.append("FD_Updated = ").append(env.getSystemUserID()).append(" ");
			sql.append("WHERE FD_Table_ID = ").append(tableId);
			execute(env, sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		return number;
	}

	/**
	 * コメント編集<br>
	 * @author ueno hideo
	 * @since 1.10 2020-04-12
	 * @param title タイトル
	 * @param tabCount タブ位置
	 * @return コメント文字列
	 */
	public String editComment(String title, int tabCount) {
		return editComment(title, tabCount, null);
	}

	/**
	 * コメント編集<br>
	 * @author ueno hideo
	 * @since 1.11 2020/07/02
	 * @param title タイトル
	 * @param tabCount タブ位置
	 * @param param パラメータ
	 * @return コメント文字列
	 */
	public String editComment(String title, int tabCount, FD_JavaDocParam param) {
		StringBuffer source = new StringBuffer();
		source.append(setTab(tabCount)).append("/**").append(FD_RETURN);
		source.append(setTab(tabCount)).append(" * ").append(title).append(".<br>").append(FD_RETURN);
		//パラメータ追加
		if(param != null && param.length() > 0) {
			source.append(param.getJavadocOfParam(tabCount));
		}
		source.append(setTab(tabCount)).append(" */").append(FD_RETURN);
		return source.toString();
	}

	public String setTab(int tabCount) {
		StringBuffer source = new StringBuffer();
		for(int ix = 0 ; ix < tabCount; ix++) {
			source.append(FD_TAB);
		}
		return source.toString();
	}

	/**
	 * インポートクラスを追加する。<br>
	 * @param importList インポートクラスリスト
	 * @param className 追加クラス名
	 */
	public void addImportClass(List<String> importList, String className) {
		boolean isSee = false;		//追加スイッチ
		for(String clazz : importList) {
			if(clazz.equals(className)) {
				isSee = true;
				break;
			}
		}
		if(isSee == false) {
			importList.add(className);
		}
	}

	/**
	 * インポートクラス編集<br>
	 * <p>リストの対象クラスは昇順に並び替えて出力される。</p>
	 * @author ueno hideo
	 * @since 1.20 2020/07/16
	 * @param importClassList　インサート対象クラスリスト
	 * @return インサート文字列
	 */
	public StringBuffer editImportClass(List<String> importClassList) {
		StringBuffer source = new StringBuffer();
		Collections.sort(importClassList);
		for(String clazz : importClassList) {
			source.append("import ").append(clazz).append(";").append(FD_RETURN);
		}
		if(source.length() > 0) {
			source.append(FD_RETURN);
		}
		return source;
	}

	/**
	 * エスケープ処理<br>
	 * <p>SQLインジェクション対策の為、指定されたデータのコードを表示するエスケープ処理する。</p>
	 * @author officina-hide.com ueno
	 * @since 2020/08/29
	 * @param data 処理対象情報
	 * @return エスケープ処理済情報
	 */
	public String changeEscape(String data) {
		String out = data;
		out = out.replaceAll("\'", "\"");
		out = out.replaceAll("\\\\", "\\\\\\\\");
//		System.out.println(out);
		return out;
	}

	/**
	 * DBステートメント生成<br>
	 * @author officine-hide.com ueno
	 * @since 2.00 2020/09/01
	 * @return DBステートメント
	 */
	public Statement createStatement() {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stmt;
	}

	/**
	 * データベーステーブルの生成<br>
	 * @author officina-hide.com
	 * @sinse 1.00 2020/09/12
	 * @param env 環境情報
	 */
	public void createDBTable(FD_EnvData env, String TableName) {
		ResultSet rs = null;
		Statement stmt = null;
		StringBuffer sql = new StringBuffer();
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		//テーブル情報ID取得
		int tableId = getTableID(env, TableName);
		X_FD_Table table = new X_FD_Table(env, tableId);
		//既にテーブルが存在するときは削除する。
		sql.append("DROP TABLE IF EXISTS ").append(TableName);
		execute(env, sql.toString());
		//テーブルの新規作成
		try {
			//テーブル項目情報一覧取得
			sql = new StringBuffer();
			sql.append("SELECT * FROM FD_TableColumn ");
			sql.append("LEFT JOIN FD_Reference ON FD_Reference.FD_Reference_ID = FD_TableColumn.")
				.append(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_TYPE_ID).append(" ");
			sql.append("WHERE ").append(I_FD_TableColumn.COLUMNNAME_FD_TABLE_ID).append(" = ").append(tableId).append(" ");
			sql.append("ORDER BY Column_Sort_Order");
			connection(env);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql.toString());
			while(rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME, rs.getString(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME));
				map.put("Column_Type_Name", rs.getString(I_FD_Reference.COLUMNNAME_REFERENCE_NAME));
				map.put(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_SIZE, rs.getString(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_SIZE));
				map.put(I_FD_TableColumn.COLUMNNAME_FD_NAME, rs.getString(I_FD_TableColumn.COLUMNNAME_FD_NAME));
				if(rs.getInt(I_FD_TableColumn.COLUMNNAME_IS_PRIMARY) == 1) {
					map.put(I_FD_TableColumn.COLUMNNAME_IS_PRIMARY, I_FD_TableColumn.IS_PRIMARY_YES);
				} else {
					map.put(I_FD_TableColumn.COLUMNNAME_IS_PRIMARY, I_FD_TableColumn.IS_PRIMARY_NO);
				}
				if(rs.getInt(I_FD_TableColumn.COLUMNNAME_IS_NULL) == 1) {
					map.put(I_FD_TableColumn.COLUMNNAME_IS_NULL, I_FD_TableColumn.IS_NULL_YES);
				} else {
					map.put(I_FD_TableColumn.COLUMNNAME_IS_NULL, I_FD_TableColumn.IS_NULL_NO);
				}
				list.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, stmt);
		}
		//テーブル生成
		sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS ").append(TableName);
		sql.append("(");
		StringBuffer items = new StringBuffer();	//項目用SQL文
		for(Map<String, String> map : list) {
			if(items.length() > 0) {
				items.append(", ");
			}
			items.append(map.get(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME).toString()).append(" ");
			switch(map.get("Column_Type_Name").toString()) {
			case COLUMN_TYPE_INFORMATION_ID:
			case COLUMN_TYPE_NUMBER:
			case COLUMN_TYPE_LIST:
				items.append("INT UNSIGNED").append(" ");
				break;
			case COLUMN_TYPE_TEXT:
				items.append("Varchar(").append(map.get(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_SIZE).toString()).append(")").append(" ");
				break;
			case COLUMN_TYPE_DATE:
				items.append("Datetime").append(" ");
				break;
			case COLUMN_TYPE_FIELD_TEXT:
				items.append("Text").append(" ");
				break;
			}
			if(map.get(I_FD_TableColumn.COLUMNNAME_IS_NULL).toString().equals(I_FD_TableColumn.IS_NULL_YES)){
				items.append("NOT NULL").append(" ");
			}
			if(map.get(I_FD_TableColumn.COLUMNNAME_IS_PRIMARY).toString().equals(I_FD_TableColumn.IS_PRIMARY_YES)){
				items.append("PRIMARY KEY").append(" ");
			}
			if(map.get(I_FD_TableColumn.COLUMNNAME_FD_NAME) != null &&
					map.get(I_FD_TableColumn.COLUMNNAME_FD_NAME).toString().length() > 0) {
				items.append("COMMENT ")
					.append(FD_SQ).append(map.get(I_FD_TableColumn.COLUMNNAME_FD_NAME).toString()).append(FD_SQ).append(" ");
			}
		}
		sql.append(items.toString()).append(")");
		if(table.getStringOfValue(I_FD_TableColumn.COLUMNNAME_FD_NAME).length() > 0) {
			sql.append(" COMMENT ").append(FD_SQ)
				.append(table.getStringOfValue(I_FD_TableColumn.COLUMNNAME_FD_NAME)).append(FD_SQ).append(" ");
		}
		
		//生成処理実行
		execute(env, sql.toString());
	}

}
