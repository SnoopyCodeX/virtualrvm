package com.cdph.virtualrvm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import android.support.v7.app.AlertDialog;
import com.cdph.virtualrvm.util.Constants;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "db_virtualrvm";
	private static final String TB_ID = "_id";
	public static final String TB_USER = "tb_users";
	public static final String TB_ITEM = "tb_items";
	
	public static final String COL_USER = "user_name";
	public static final String COL_PASS = "user_pass";
	public static final String COL_RANK = "user_rank";
	public static final String COL_CENT = "user_cent";
	
	public static final String COL_NAME = "item_name";
	public static final String COL_WEIGHT = "item_weight";
	public static final String COL_TYPE = "item_type";
	public static final String COL_AMNT = "item_amount";
	public static final String COL_ID = "item_id";
	private Context ctx;
	
	public DatabaseHelper(Context ctx)
	{
		super(ctx, DB_NAME, null, DB_VERSION);
		this.ctx = ctx;
		
		SQLiteDatabase db = getWritableDatabase();
		String str_sql1 = "CREATE TABLE IF NOT EXISTS `%s`(`%s` INTEGER AUTO_INCREMENT PRIMARY KEY, `%s` INTEGER(1), `%s` VARCHAR(255), `%s` VARCHAR(255), `%s` VARCHAR(255))";
		String str_sql2 = "CREATE TABLE IF NOT EXISTS `%s`(`%s` INTEGER AUTO_INCREMENT PRIMARY KEY, `%s` VARCHAR(255), `%s` VARCHAR(255), `%s` VARCHAR(255), `%s` VARCHAR(255), `%s` VARCHAR(255))";
		
		try {
			if(Constants.DEBUG_MODE)
			{
				db.execSQL("DROP TABLE IF EXISTS `tb_users`");
				db.execSQL("DROP TABLE IF EXISTS `tb_items`");
			}
			db.execSQL(String.format(str_sql1, TB_USER, TB_ID, COL_RANK, COL_USER, COL_PASS, COL_CENT));
			db.execSQL(String.format(str_sql2, TB_ITEM, TB_ID, COL_ID, COL_NAME, COL_WEIGHT, COL_TYPE, COL_AMNT));
		} catch(Exception e) {
			e.printStackTrace();
			showAsPopup(e.getMessage());
		}
	}
	
    @Override
	public void onCreate(SQLiteDatabase db)
	{}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		String str_sql = "DROP TABLE IF EXISTS %s";
		
		try {
			db.execSQL(String.format(str_sql, TB_USER));
			db.execSQL(String.format(str_sql, TB_ITEM));
		} catch(Exception e) {
			e.printStackTrace();
			showAsPopup(e.getMessage());
		}
	}
	
	private void showAsPopup(String message)
	{
		AlertDialog dlg = new AlertDialog.Builder(ctx).create();
		dlg.setTitle("Crash Report");
		
		String _message = "Model: %s\nBrand: %s\nManufacturer: %s\nSdk Version: %s\n=====[Error]=====\n%s";
		dlg.setMessage(String.format(_message, Build.MODEL, Build.BRAND, Build.MANUFACTURER, Build.VERSION.SDK, message));
		dlg.setCanceledOnTouchOutside(false);
		dlg.setCancelable(true);
		dlg.show();
	}
}
