package com.cdph.virtualrvm.db;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class VirtualRVMDatabase 
{
	private DatabaseHelper helper;
	
    public VirtualRVMDatabase(Context ctx)
	{
		helper = new DatabaseHelper(ctx);
	}
	
	public long insertUserData(String username, String password, String cent)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put(helper.COL_USER, username);
		content.put(helper.COL_PASS, password);
		content.put(helper.COL_CENT, cent);
		return (db.insert(helper.TB_USER, null, content));
	}
	
	public long insertItemData(String brandname, String weight, String itemId)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put(helper.COL_NAME, brandname);
		content.put(helper.COL_WEIGHT, weight);
		content.put(helper.COL_ID, itemId);
		return (db.insert(helper.TB_ITEM, null, content));
	}
	
	public int updateUserData(String colName, String newData, String username)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put(colName, newData);
		return (db.update(helper.TB_USER, content, "user_name = ?", new String[]{username}));
	}
	
	public int updateItemData(String colName, String newData, String itemId)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put(colName, newData);
		return (db.update(helper.TB_ITEM, content, "item_id = ?", new String[]{itemId}));
	}
	
	public int deleteUserData(String username)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		return (db.delete(helper.TB_USER, "user_name = ?", new String[]{username}));
	}
	
	public int deleteItemData(String itemId)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		return (db.delete(helper.TB_ITEM, "item_id = ?", new String[]{itemId}));
	}
	
	public String[] getItemData(String itemId)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		String[] columns = {helper.COL_NAME, helper.COL_WEIGHT, helper.COL_ID};
		Cursor cursor = db.query(helper.TB_USER, columns, "item_id = ?", new String[]{itemId}, null, null, null);
		
		if(cursor == null || cursor.getCount() <= 0)
			return null;
		cursor.moveToFirst();
		
		String[] data = new String[columns.length];
		for(int i = 0; i < data.length; i++)
			data[i] = cursor.getString(cursor.getColumnIndexOrThrow(columns[i]));
		return data;
	}
	
	public String[] getUserData(String username)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		String[] columns = {helper.COL_USER, helper.COL_PASS, helper.COL_CENT};
		Cursor cursor = db.query(helper.TB_USER, columns, "user_name = ?", new String[]{username}, null, null, null);
		
		if(cursor == null || cursor.getCount() <= 0)
			return null;
		cursor.moveToFirst();
		
		String[] data = new String[columns.length];
		for(int i = 0; i < data.length; i++)
			data[i] = cursor.getString(cursor.getColumnIndexOrThrow(columns[i]));
		return data;
	}
}
