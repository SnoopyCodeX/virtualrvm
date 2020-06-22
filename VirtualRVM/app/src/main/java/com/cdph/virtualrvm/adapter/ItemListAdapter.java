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
import com.cdph.virtualrvm.BaseApplication;
import com.cdph.virtualrvm.dialog.AdminEditItemDialog;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;
import com.cdph.virtualrvm.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

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
							
							if(!BaseApplication.conn.isConnected(holder.context))
								return;
								
							final SweetAlertDialog swal = new SweetAlertDialog(holder.context, SweetAlertDialog.PROGRESS_TYPE);
							swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
							swal.setTitleText("Deleting item...");
							swal.setCancelable(false);
							swal.setCanceledOnTouchOutside(false);
							swal.show();
							
							HashMap<String, Object> data = new HashMap<>();
							data.put("action_deleteItemData", "");
							data.put("item_id", model.itemId);
							
							VolleyRequest.newRequest(holder.context, Constants.BASE_URL)
								.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
									@Override
									public void onVolleyResponseReceived(String response)
									{
										try {
											swal.dismissWithAnimation();
											
											JSONArray jar = new JSONArray(response);
											JSONObject job = jar.getJSONObject(0);

											boolean hasError = job.getBoolean("hasError");
											String message = job.getString("message");

											SweetAlertDialog swal = new SweetAlertDialog(holder.context, ((hasError) ? SweetAlertDialog.ERROR_TYPE : SweetAlertDialog.SUCCESS_TYPE));
											swal.setCancelable(false);
											swal.setCanceledOnTouchOutside(false);
											swal.setContentText(message);
											swal.setTitleText((hasError) ? "Delete Failed" : "Delete Success");
											swal.setConfirmText("Okay");
											swal.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
												@Override
												public void onClick(SweetAlertDialog dlg)
												{
													dlg.dismissWithAnimation();
													activity.loadAllItemData();
												}
											});
											swal.show();
										} catch(Exception e) {
											e.printStackTrace();
										}
									}
								})
								.setEndPoint("item/deleteItemData.php")
								.sendRequest(data);
						}
					})
					.show();
			}
		});
		
		holder.btn_edit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				final SweetAlertDialog swal = new SweetAlertDialog(holder.context, SweetAlertDialog.PROGRESS_TYPE);
				swal.setCancelable(false);
				swal.setCanceledOnTouchOutside(false);
				swal.setTitleText("Fetching item data...");
				swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
				swal.show();
				
				HashMap<String, Object> data = new HashMap<>();
				data.put("action_getItemData", "");
				data.put("item_id", model.itemId);
				
				VolleyRequest.newRequest(holder.context, Constants.BASE_URL)
					.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
						@Override
						public void onVolleyResponseReceived(String response)
						{
							swal.dismissWithAnimation();
							try {
								JSONArray jar = new JSONArray(response);
								JSONObject job = jar.getJSONObject(0);

								JSONArray jdat = job.getJSONArray("data");
								JSONObject jobj = jdat.getJSONObject(0);
								boolean hasError = job.getBoolean("hasError");
								String message = job.getString("message");

								if(!hasError)
								{
									AdminEditItemDialog.init(
										holder.context, 
										ItemModel.newItem(
											jobj.getString("item_id"),
											jobj.getString("item_name"),
											jobj.getString("item_weight"),
											jobj.getString("item_type"),
											jobj.getString("item_worth")
										)
									).setActivity(activity).show();
									return;
								}

								new SweetAlertDialog(holder.context, SweetAlertDialog.ERROR_TYPE)
									.setTitleText("Fetching Failed")
									.setContentText(message)
									.setConfirmText("Okay")
									.show();
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
					})
					.setEndPoint("item/getItemData.php")
					.sendRequest(data);
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
		String[] texts = text.split(" ");
		String ups = "";
		
		for(String txt : texts)
			ups += (String.valueOf(txt.charAt(0)).toUpperCase() + txt.substring(1, txt.length())) + " ";
		
		ups = ups.substring(0, ups.length()-1);
		return ups;
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
