package com.cdph.virtualrvm.model;

public final class UserModel 
{
	public String userName;
	public String userPass;
	public String userCent;
	public String userRank;
	
	private UserModel()
	{}
	
	private UserModel(String name, String pass, String cent, String rank)
	{
		userName = name;
		userPass = pass;
		userCent = cent;
		userRank = rank;
	}
	
	public static final UserModel newUser(String name, String pass, String cent, String rank)
	{
		return (new UserModel(name, pass, cent, rank));
	}
}
