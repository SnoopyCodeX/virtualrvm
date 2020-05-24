package com.cdph.virtualrvm.model;

public final class ItemModel 
{
    public static String itemId;
	public static String itemName;
	public static String itemWeight;
	public static String itemType;
	public static String itemWorth;
	
	private ItemModel()
	{}
	
	private ItemModel(String id, String name, String weight, String type, String worth)
	{
		itemId = id;
		itemName = name;
		itemWeight = weight;
		itemType = type;
		itemWorth = worth;
	}
	
	public static final ItemModel newItem(String id, String name, String weight, String type, String worth)
	{
		return (new ItemModel(id, name, weight, type, worth));
	}
}
