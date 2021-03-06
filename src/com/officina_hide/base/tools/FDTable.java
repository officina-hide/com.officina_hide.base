package com.officina_hide.base.tools;

import com.officina_hide.base.common.FD_EnvData;
import com.officina_hide.base.common.FD_Logging;
import com.officina_hide.base.model.FD_DB;
import com.officina_hide.base.model.I_FD_Table;
import com.officina_hide.base.model.I_FD_TableColumn;
import com.officina_hide.base.model.X_FD_Table;

/**
 * テーブル情報クラス<br>
 * @author ueno hideo
 * @version 2.00 新規作成
 * @since 2020/08/29
 */
public class FDTable extends FD_DB implements I_FD_Table {

	/**
	 * テーブル情報生成<br>
	 * @author ueno hideo
	 * @since 2.00 2020/08/29
	 * @param env 環境情報
	 */
	public void createTable(FD_EnvData env) {
		StringBuffer sql = new StringBuffer();
		//既に登録されているテーフル情報を削除する。
		sql.append("DROP TABLE IF EXISTS FD_Table");
		execute(env, sql.toString());
		//テーフル情報を生成する。
		sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS FD_Table  (");
		sql.append("FD_Table_ID INT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'テーブル情報ID'").append(",");
		sql.append("Table_Name Varchar(100)  COMMENT 'テーブル物理名'").append(",");
		sql.append("FD_Name Varchar(100)  COMMENT 'テーブル論理名'").append(",");
		sql.append("fD_COMMENT Text  COMMENT '説明'").append(",");
		sql.append("FD_Create DATETIME  COMMENT '登録日'").append(",");
		sql.append("FD_Created INT UNSIGNED  COMMENT '登録者ID'").append(",");
		sql.append("FD_Update DATETIME  COMMENT '更新日'").append(",");
		sql.append("FD_Updated INT UNSIGNED  COMMENT '更新者ID'");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='テーブル情報'");
		execute(env, sql.toString());
		
		env.getLog().add(env, FD_Logging.TYPE_MESSAGE, FD_Logging.MODE_NORMAL, "テーブル情報構築");
	}

	/**
	 * テーブル情報登録<br>
	 * <p>version 2.10 テーブル情報IDを返す。</p>
	 * @author ueno hideo
	 * @since 2.00 2020/08/29
	 * @param env 環境情報
	 * @param tableId テーブル情報ID
	 * @param tableName テーブル物理名
	 * @param name テーブル論理名
	 * @param comment テーブル説明
	 * @return テーブル情報ID
	 */
	public int addData(FD_EnvData env, int tableId, String tableName, String name, String comment) {
		if(tableId == 0) {
			tableId = getNewID(env, Table_ID);
		}
		X_FD_Table table = new X_FD_Table(env);
		table.setValue(env, COLUMNNAME_FD_TABLE_ID, tableId);
		table.setValue(env, COLUMNNAME_TABLE_NAME, tableName);
		table.setValue(env, COLUMNNAME_FD_NAME, name);
		table.setValue(env, COLUMNNAME_FD_COMMENT, comment);
		table.save(env);
		
		return tableId;
	}

	/**
	 * テーブル項目情報登録<br>
	 * @author officine-hide.com ueno
	 * @since 2020/09/07
	 * @param env 
	 */
	public void addColumnData(FD_EnvData env) {
		FDTableColumn column = new FDTableColumn();
		column.add(env, Table_ID, COLUMNNAME_FD_TABLE_ID, "テーブル情報ID", "テーブル情報を識別するための情報ID"
				, COLUMN_TYPE_INFORMATION_ID, 0, 10, I_FD_TableColumn.COLUMNNAME_IS_PRIMARY);
		column.add(env, Table_ID, COLUMNNAME_TABLE_NAME, "テーブル名", "テーブルの物理名称"
				, COLUMN_TYPE_TEXT, 100, 20, I_FD_TableColumn.IS_PRIMARY_NO);
		column.add(env, Table_ID, COLUMNNAME_FD_NAME, "テーブル表示名", "テーブルの論理名称"
				, COLUMN_TYPE_FIELD_TEXT, 100, 30, I_FD_TableColumn.IS_PRIMARY_NO);
		column.add(env, Table_ID, COLUMNNAME_FD_COMMENT, "テーブル説明", "テーブルの説明"
				, COLUMN_TYPE_FIELD_TEXT, 0, 40, I_FD_TableColumn.IS_PRIMARY_NO);
		column.add(env, Table_ID, COLUMNNAME_FD_CREATE, "登録日", "情報の登録日"
				, COLUMN_TYPE_DATE, 0, 900, I_FD_TableColumn.IS_PRIMARY_NO);
		column.add(env, Table_ID, COLUMNNAME_FD_CREATED, "登録者ID", "情報の登録者ID（ユーザー情報ID）"
				, COLUMN_TYPE_INFORMATION_ID, 0, 920, I_FD_TableColumn.IS_PRIMARY_NO);
		column.add(env, Table_ID, COLUMNNAME_FD_UPDATE, "更新日", "情報の更新日"
				, COLUMN_TYPE_DATE, 0, 900, I_FD_TableColumn.IS_PRIMARY_NO);
		column.add(env, Table_ID, COLUMNNAME_FD_UPDATED, "更新者ID", "情報の更新者ID（ユーザー情報ID）"
				, COLUMN_TYPE_INFORMATION_ID, 0, 920, I_FD_TableColumn.IS_PRIMARY_NO);
	}
	
 }
