package com.cdph.virtualrvm.model;

public final class UserModel 
{
	public String userName;
	public String userPass;
	public String userCent;
	public String userRank;
	public String userEmail;
	public String userNumber;
	
	private UserModel()
	{}
	
	private UserModel(String name, String pass, String cent, String rank, String email, String number)
	{
		userName = name;
		userPass = pass;
		userCent = cent;
		userRank = rank;
		userEmail = email;
		userNumber = number;
	}
	
	public static final UserModel newUser(String name, String pass, String cent, String rank, String email, String number)
	{
		return (new UserModel(name, pass, cent, rank, email, number));
	}
}
