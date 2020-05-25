package com.cdph.virtualrvm.db;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class VirtualRVMDatabase 
{
	private DatabaseHelper helper;
	private Context ctx;
	
    public VirtualRVMDatabase(Context ctx)
	{
		helper = new DatabaseHelper(ctx);
		this.ctx = ctx;
	}
	
	public long insertUserData(String username, String password, int rank, String cent)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put(helper.COL_USER, username);
		content.put(helper.COL_PASS, password);
		content.put(helper.COL_CENT, cent);
		content.put(helper.COL_RANK, rank);
		return (db.insert(helper.TB_USER, null, content));
	}
	
	public long insertItemData(String brandname, String weight, String itemId, String itemType, String exchangeAmount)
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put(helper.COL_NAME, brandname);
		content.put(helper.COL_WEIGHT, weight);
		content.put(helper.COL_ID, itemId);
		content.put(helper.COL_AMNT, exchangeAmount);
		content.put(helper.COL_TYPE, itemType);
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
		String[] columns = {helper.COL_ID, helper.COL_NAME, helper.COL_WEIGHT, helper.COL_TYPE, helper.COL_AMNT};
		Cursor cursor = db.query(helper.TB_ITEM, columns, "item_id = ?", new String[]{itemId}, null, null, null);
		
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
		String[] columns = {helper.COL_USER, helper.COL_PASS, helper.COL_CENT, helper.COL_RANK};
		Cursor cursor = db.query(helper.TB_USER, columns, "user_name = ?", new String[]{username}, null, null, null);
		
		if(cursor == null || cursor.getCount() <= 0)
			return null;
		cursor.moveToFirst();
		
		String[] data = new String[columns.length];
		for(int i = 0; i < data.length; i++)
			if(i == data.length-1)
				data[i] = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(columns[i])));
			else
				data[i] = cursor.getString(cursor.getColumnIndexOrThrow(columns[i]));
			
		return data;
	}
	
	public List<ArrayList<String>> getAllUserData()
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(helper.TB_USER, null, null, null, null, null, null);
		List<ArrayList<String>> users = new ArrayList<>();
		
		if(cursor == null || cursor.getCount() < 1)
			return null;
		
		while(cursor.moveToNext())
		{
			ArrayList<String> userData = new ArrayList<>();
			userData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_USER)));
			userData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_PASS)));
			userData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_CENT)));
			userData.add(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(helper.COL_RANK))));
			users.add(userData);
		}
		
		cursor.close();
		return users;
	}
	
	public List<ArrayList<String>> getAllItemData()
	{
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(helper.TB_ITEM, null, null, null, null, null, null);
		List<ArrayList<String>> items = new ArrayList<>();
		
		if(cursor == null || cursor.getCount() < 1)
			return null;

		while(cursor.moveToNext())
		{
			ArrayList<String> itemData = new ArrayList<>();
			itemData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_ID)));
			itemData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_NAME)));
			itemData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_WEIGHT)));
			itemData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_TYPE)));
			itemData.add(cursor.getString(cursor.getColumnIndexOrThrow(helper.COL_AMNT)));
			items.add(itemData);
		}
		
		cursor.close();
		return items;
	}
}
