package com.officina_hide.base.model;

import java.util.List;
import java.util.Map;

/**
 * 「情報ID」項目クラス<br>
 * @author ueno hideo
 * @version 1.20
 * @since 2020/07/16
 */
public class FD_ImformationID extends FD_DB implements I_DB {

	/**
	 * インポートクラスリスト
	 */
	private List<Map<String, String>> importList;
	
	public FD_ImformationID(List<Map<String, String>> importList) {
		this.importList = importList;
	}

	/**
	 * 項目定義文字列作成<br>
	 * @author ueno hideo
	 * @since 1.20 2020/07/16
	 * @param columnName 項目物理名
	 * @param name 項目論理名
	 * @return 項目定義文字列
	 */
	public String toVariableDefinitions(String columnName, String name) {
		StringBuffer source = new StringBuffer();
		source.append(editComment(name, 1));
		source.append(setTab(1)).append("private int ").append(columnName.substring(0, 1).toLowerCase())
			.append(columnName.substring(1)).append(";").append(FD_RETURN);
		return source.toString();
	}
	
	/**
	 * クラスのgetter定義用文字列を編集する。
	 * @author ueno hideo
	 * @since 1.20 2020/07/20
	 * @param columnName 変数名
	 * @param name 変数説明
	 * @return 定義用文字列
	 */
	public String toGetterDefinition(String columnName, String name) {
		StringBuffer source = new StringBuffer();
		source.append(editComment(name+"を取得する。", 1));
		source.append(setTab(1)).append("public int get").append(columnName.substring(0, 1).toUpperCase())
			.append(columnName.substring(1)).append("() {").append(FD_RETURN);
		source.append(setTab(2)).append("return ").append(columnName.substring(0, 1).toLowerCase())
			.append(columnName.substring(1)).append(";").append(FD_RETURN);
		source.append(setTab(1)).append("}").append(FD_RETURN);
		return source.toString();		
	}
	
	/**
	 * クラスのsetter定義用文字列を編集する。
	 * @author ueno hideo
	 * @since 1.20 2020/07/20
	 * @param columnName テーフル項目名
	 * @param name 説明
	 * @return 定義用文字列
	 */
	public String toSetterDefinition(String columnName, String name) {
		StringBuffer source = new StringBuffer();
		String variable = columnName.substring(0, 1).toLowerCase()+columnName.substring(1);
		source.append(editComment(name+"をセットする。", 1));
		source.append(setTab(1)).append("public void set").append(columnName.substring(0, 1).toUpperCase())
			.append(columnName.substring(1)).append("( int ").append(variable).append(") {").append(FD_RETURN);
		source.append(setTab(2)).append("this.").append(variable).append(" = ").append(variable).append(";").append(FD_RETURN);
		source.append(setTab(1)).append("}").append(FD_RETURN);
		return source.toString();		
	}
	
	/**
	 * 保存メソッドSQL用項目セット文字列を使うと返す。<br>
	 * @author ueno hideo
	 * @since 2020-04-25 
	 * @param tableName テーブル名
	 * @param columnName テーブル項目名
	 * @return 定義用文字列
	 */
	public String toSaveSQL (String tableName, String columnName) {
		StringBuffer source = new StringBuffer();
		source.append(setTab(2)).append("sql.append(I_").append(tableName).append(".")
			.append("COLUMNNAME_").append(columnName.toUpperCase()).append(")")
			.append(".append(").append(FD_DQ).append(" = ").append(FD_DQ).append(")")
			.append(".append(").append("get").append(columnName.substring(0, 1).toUpperCase())
			.append(columnName.substring(1)).append("()")
			.append(")");
		return source.toString();
	}

	/**
	 * テーブルからの情報取得用SQL文を返す<br>
	 * @author ueno hideo
	 * @since 2020/05/04
	 * @param map テーブル項目情報
	 * @param tabCnt タブ数
	 * @return Load用定義文
	 */
	public String toLoadSQL(Map<String, String>  map, int tabCnt) {
		StringBuilder source = new StringBuilder();
		String columnName = map.get(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME).toString();
		source.append(setTab(tabCnt)).append("set").append(columnName.substring(0, 1).toUpperCase())
			.append(map.get(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME).substring(1)).append("(")
			.append("rs.getInt(").append("COLUMNNAME_"+columnName.toUpperCase()).append("));").append(FD_RETURN);
		return source.toString();
	}
	
	/**
	 * テーブル生成用SQL文を返す。<br>
	 * @author ueno hideo
	 * @since 2020/04/30
	 * @param map テーブル項目情報
	 * @return SQL文字列
	 */
	public String toTableCreateSQL(Map<String, String>  map) {
		StringBuffer source = new StringBuffer();
		source.append(map.get(I_FD_TableColumn.COLUMNNAME_TABLECOLUMN_NAME).toString());
		source.append(" INT");
		if(map.get(I_FD_Reference.COLUMNNAME_REFERENCE_NAME).equals("情報ID")
				||map.get(I_FD_Reference.COLUMNNAME_REFERENCE_NAME).equals("自然数")) {
			source.append(" UNSIGNED");
		}
		if(map.get(I_FD_TableColumn.COLUMNNAME_IS_PRIMARY).equals("YES")) {
			source.append(" PRIMARY KEY");
		}
		if(map.get(I_FD_TableColumn.COLUMNNAME_FD_NAME).length() > 0) {
			source.append(" COMMENT ").append(FD_SQ).append(map.get(I_FD_TableColumn.COLUMNNAME_FD_NAME)).append(FD_SQ).append(" ");
		}
		// TODO NOT NULL の設定を追加予定(2020-04-30)
		return source.toString();
	}
}
