package com.officina_hide.accounts.fx;

import com.officina_hide.accounts.model.I_Fx_CashBook_View;
import com.officina_hide.base.common.FD_EnvData;
import com.officina_hide.base.common.FD_WhereData;
import com.officina_hide.base.model.I_Fx_View;
import com.officina_hide.base.model.X_Fx_View;
import com.officina_hide.base.tools.FDProcess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Fx金銭出納画面<br>
 * @author officine-hide.com
 * @version 1.00
 * @since 2020/09/15
 */
public class Fx_CashBook_View extends Application implements I_Fx_CashBook_View {

	/** 環境情報 */
	private FD_EnvData env;
	/** プロセス情報クラス */
	FDProcess process = new FDProcess();
	
	/**
	 * コンストラクター<br>
	 * <p>実体化時に、環境情報を生成する。</p>
	 * @author officine-hide.com
	 * @since 1.00 2020/09/15
	 */
	public Fx_CashBook_View() {
		env = new FD_EnvData(View_Name);
	}

	@Override
	public void start(Stage stage) throws Exception {
		//画面情報取得
		FD_WhereData where = new FD_WhereData(I_Fx_View.COLUMNNAME_VIEW_NAME, View_Name);
		X_Fx_View view = new X_Fx_View(env, where);
		
		//ルート設定
		VBox root = new VBox(5);
		
		//画面表示
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
