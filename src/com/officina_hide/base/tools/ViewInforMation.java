package com.officina_hide.base.tools;

import com.officina_hide.base.common.FD_EnvData;
import com.officina_hide.base.common.FD_Logging;
import com.officina_hide.base.model.FD_DB;
import com.officina_hide.base.model.I_Fx_View;
import com.officina_hide.base.model.X_Fx_View;

/**
 * 画面情報管理クラス
 * @author officina-hide.com
 * @version 2.00
 * @since 2020/08/31
 */
public class ViewInforMation extends FD_DB implements I_Fx_View {

	/**
	 * 画面情報テーブル生成<br>
	 * @author officine-hide.com ueno
	 * @since 2.00 2020/08/31
	 * @param env 環境情報
	 */
	public void createTable(FD_EnvData env) {
		StringBuffer sql = new StringBuffer();
		//既に登録されている画面情報を削除する。
		sql.append("DROP TABLE IF EXISTS Fx_View");
		execute(env, sql.toString());
		//画面情報テーブルを生成する。
		sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS Fx_View  (");
		sql.append("FX_View_ID INT UNSIGNED NOT NULL PRIMARY KEY COMMENT '画面情報ID'").append(",");
		sql.append("View_Name Varchar(100) COMMENT ").append(FD_SQ).append("画面名").append(FD_SQ).append(",");
		sql.append("FD_Name Varchar(100) COMMENT ").append(FD_SQ).append("画面表示名").append(FD_SQ).append(",");
		sql.append("FD_Create DATETIME  COMMENT '登録日'").append(",");
		sql.append("FD_Created INT UNSIGNED  COMMENT '登録者ID'").append(",");
		sql.append("FD_Update DATETIME  COMMENT '更新日'").append(",");
		sql.append("FD_Updated INT UNSIGNED  COMMENT '更新者ID'");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='画面情報'");
		execute(env, sql.toString());
		env.getLog().add(FD_Logging.TYPE_MESSAGE, FD_Logging.MODE_NORMAL, "画面情報構築");		
	}

	/**
	 * 画面情報登録<br>
	 * @author officine-hide.com ueno
	 * @since 2.00 2020/08/25
	 * @param env 環境情報
	 * @param ViewId 画面情報ID
	 * @param ViewName 画面名
	 * @param name 画面表示名
	 */
	public void addData(FD_EnvData env, int viewId, String ViewName, String name) {
		X_Fx_View view = new X_Fx_View(env);
		view.setValue(COLUMNNAME_FX_VIEW_ID, viewId);
		view.setValue(COLUMNNAME_VIEW__NAME, ViewName);
		view.setValue(COLUMNNAME_FD_NAME, name);
		
	}

}
