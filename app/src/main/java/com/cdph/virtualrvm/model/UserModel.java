package com.cdph.virtualrvm.model;

public final class UserModel 
{
	public static String userName;
	public static String userPass;
	public static String userCent;
	public static String userRank;
	
	private UserModel()
	{}
	
	public static final UserModel newUser(String name, String pass, String cent, String rank)
	{
		UserModel model = new UserModel();
		model.userName = name;
		model.userPass = pass;
		model.userCent = cent;
		model.userRank = rank;
		
		return model;
	}
}
