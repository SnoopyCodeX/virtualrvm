package com.cdph.virtualrvm.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.cdph.virtualrvm.util.Constants;

public class AccountManager 
{
    private Context ctx;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	
	private AccountManager()
	{}
	
	private AccountManager(Context ctx)
	{
		this.ctx = ctx;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		editor = prefs.edit();
	}
	
	public static synchronized AccountManager getInstance(Context ctx)
	{
		return (new AccountManager(ctx));
	}
	
	public AccountManager saveUserData(String username, String password, String cent, int rank, boolean remember)
	{
		editor.putString(Constants.KEY_USERNAME, username)
			.putString(Constants.KEY_PASSWORD, password)
			.putString(Constants.KEY_CENTS, cent)
			.putInt(Constants.KEY_RANK, rank)
			.putBoolean(Constants.KEY_REMEMBER, remember);
			
		return this;
	}
	
	public AccountManager removeUserData()
	{
		editor.putString(Constants.KEY_USERNAME, "")
			.putString(Constants.KEY_CENTS, "")
			.putInt(Constants.KEY_RANK, 0)
			.putBoolean(Constants.KEY_REMEMBER, false);

		return this;
	}
	
	public boolean commit()
	{
		return editor.commit();
	}
}
