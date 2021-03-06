package com.officina_hide.base.model;

import com.officina_hide.base.common.FD_EnvData;

/**
 * テーブル項目リスト情報IOクラス<br>
 * @author officine-hide.com
 * @version 2.11
 * @since 2020/09/26
 */
public class X_FD_TableColumnList extends FD_DB implements I_FD_TableColumnList {

	/**
	 * コンストラクター<br>
	 * <p>実体化時に、項目の初期化を行う。</p>
	 * @param env 環境情報
	 */
	public X_FD_TableColumnList(FD_EnvData env) {
		createItemList(env, getTableID(env, Table_Name));
	}

	/**
	 * 情報保存<br>
	 * @author officine-hide.com
	 * @since 2020/09/26
	 * @param env 環境情報
	 */
	public void save(FD_EnvData env) {
		save(env, Table_Name);
	}

}
