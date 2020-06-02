package com.cdph.virtualrvm.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filter.*;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.AdminActivity;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.R;

import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter extends Adapter<ItemListAdapter.ItemListViewHolder> implements Filterable
{
	private AdminActivity activity;
	private List<ItemModel> itemList;
	private List<ItemModel> itemListFull;
	
	public ItemListAdapter(@NonNull List<ItemModel> items)
	{
		this.itemList = items;
		this.itemListFull = new ArrayList<>(items);
	}
	
	@NonNull
	@Override
	public ItemListAdapter.ItemListViewHolder onCreateViewHolder(ViewGroup parent, int type)
	{
		return (new ItemListAdapter.ItemListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item, parent, false)));
	}
	
	public void setActivity(AdminActivity activity)
	{
		this.activity = activity;
	}
	
	@Override
	public void onBindViewHolder(@NonNull final ItemListAdapter.ItemListViewHolder holder, int position)
	{
		final ItemModel model = itemList.get(position);
		holder.tv_itemName.setText(model.itemName);
		
		String type = "<font color=\"#33B5E5\">%s %s</font>";
		holder.tv_itemType.setText(Html.fromHtml(String.format(type, model.itemWeight, firstLetterToUpperCase(model.itemType))));
		
		holder.btn_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				new SweetAlertDialog(holder.context, SweetAlertDialog.WARNING_TYPE)
					.setTitleText("Confirm Deletion")
					.setContentText("Are you sure you want to delete this item?")
					.setCancelText("Cancel")
					.setConfirmText("Delete")
					.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
						@Override
						public void onClick(SweetAlertDialog dlg)
						{
							dlg.dismissWithAnimation();
						}
					})
					.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
						@Override
						public void onClick(SweetAlertDialog dialog)
						{
							dialog.dismissWithAnimation();
							if(true)
							{
								SweetAlertDialog dlg = new SweetAlertDialog(holder.context, SweetAlertDialog.SUCCESS_TYPE);
								dlg.setTitleText("Delete Success");
								dlg.setContentText("Item has been deleted successfully!");
								dlg.setConfirmText("Okay");
								dlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
									@Override
									public void onClick(SweetAlertDialog dlg)
									{
										dlg.dismissWithAnimation();
									}
								});
								dlg.show();
								
								activity.loadAllItemData();
								return;
							}
							
							SweetAlertDialog dlg = new SweetAlertDialog(holder.context, SweetAlertDialog.SUCCESS_TYPE);
							dlg.setTitleText("Delete Failed");
							dlg.setContentText("Item deletion has failed!");
							dlg.setConfirmText("Okay");
							dlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
								@Override
								public void onClick(SweetAlertDialog dlg)
								{
									dlg.dismissWithAnimation();
								}
							});
							dlg.show();
						}
					})
					.show();
			}
		});
		
		holder.btn_edit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				
			}
		});
	}
	
	@Override
	public int getItemCount()
	{
		return itemList.size();
	}
	
	@Override
	public Filter getFilter()
	{
		return filter;
	}
	
	private String firstLetterToUpperCase(String text)
	{
		return (String.valueOf(text.charAt(0)).toUpperCase()) + text.substring(1, text.length());
	}
    
	public class ItemListViewHolder extends ViewHolder
	{
		public Context context;
		public ImageButton btn_edit, btn_delete;
		public LinearLayout parent;
		public TextView tv_itemName, tv_itemType;
		
		public ItemListViewHolder(View view)
		{
			super(view);
			
			context = view.getContext();
			parent = (LinearLayout) view;
			btn_edit = view.findViewById(R.id.content_list_edit);
			btn_delete = view.findViewById(R.id.content_list_delete);
			tv_itemName = view.findViewById(R.id.content_list_name);
			tv_itemType = view.findViewById(R.id.content_list_rank);
		}
	}
	
	private Filter filter = new Filter()
	{
		@Override
		protected Filter.FilterResults performFiltering(CharSequence constraint)
		{
			List<ItemModel> filteredList = new ArrayList<>();
			
			if(constraint != null && constraint.length() > 0)
			{
				String filterPattern = constraint.toString().toLowerCase().trim();
				for(ItemModel model : itemListFull)
					if(model.itemName.toLowerCase().contains(filterPattern))
						filteredList.add(model);
			}
			else
				filteredList.addAll(itemListFull);
			
			Filter.FilterResults result = new Filter.FilterResults();
			result.values = filteredList;
			return result;
		}
		
		@Override
		protected void publishResults(CharSequence constraint, Filter.FilterResults result)
		{
			itemList.clear();
			itemList.addAll((List) result.values);
			notifyDataSetChanged();
		}
	};
}
