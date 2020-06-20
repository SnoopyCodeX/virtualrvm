package com.cdph.virtualrvm.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
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
import com.cdph.virtualrvm.dialog.AdminEditUserDialog;
import com.cdph.virtualrvm.dialog.AdminVerifyCoinDialog;
import com.cdph.virtualrvm.model.UserModel;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;
import com.cdph.virtualrvm.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserListAdapter extends Adapter<UserListAdapter.UserListViewHolder> implements Filterable
{
	private AdminActivity activity;
	private List<UserModel> userList;
	private List<UserModel> userListFull;

	public UserListAdapter(@NonNull List<UserModel> users)
	{
		this.userList = users;
		this.userListFull = new ArrayList<>(users);
	}
	
	public void setActivity(AdminActivity activity)
	{
		this.activity = activity;
	}

	@NonNull
	@Override
	public UserListAdapter.UserListViewHolder onCreateViewHolder(ViewGroup parent, int type)
	{
		return (new UserListAdapter.UserListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item, parent, false)));
	}

	@Override
	public void onBindViewHolder(@NonNull final UserListAdapter.UserListViewHolder holder, int position)
	{
		final UserModel model = userList.get(position);
		String rank = (Integer.parseInt(model.userRank) == 0) ? "<font color=\"#FFBB33\">Member</font>" : "<font color=\"#99CC00\">Admin</font>";
		
		holder.tv_userName.setText(model.userName);
		
		if(model.userName.equals(holder.prefs.getString(Constants.KEY_USERNAME, "")))
		{
			rank = "<font color=\"#33B5E5\"><strong>[YOU]</strong></font> " + rank;
			holder.btn_delete.setEnabled(false);
		}
		
		holder.tv_userRank.setText(Html.fromHtml(String.format("%s", rank)));
		
		holder.parent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if(model.userName.equals(holder.prefs.getString(Constants.KEY_USERNAME, "")) || Integer.parseInt(model.userRank) == 1)
					return;
					
				if(!BaseApplication.conn.isConnected(holder.context))
					return;
					
				final SweetAlertDialog swal = new SweetAlertDialog(holder.context, SweetAlertDialog.PROGRESS_TYPE);
				swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
				swal.setTitleText("Fetching user data...");
				swal.setCancelable(false);
				swal.setCanceledOnTouchOutside(false);
				swal.show();
				
				HashMap<String, Object> data = new HashMap<>();
				data.put("action_getUserData", "");
				data.put("user_name", model.userName);
				
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
									AdminVerifyCoinDialog.init(
										holder.context, 
										UserModel.newUser(
											jobj.getString("user_name"),
											jobj.getString("user_pass"),
											jobj.getString("user_cent"),
											String.valueOf(jobj.getInt("user_rank")),
											jobj.getString("user_email"),
											jobj.getString("user_number")
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
					.setEndPoint("user/getUserData.php")
					.sendRequest(data);
			}
		});
		
		holder.btn_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				new SweetAlertDialog(holder.context, SweetAlertDialog.WARNING_TYPE)
					.setTitleText("Confirm Deletion")
					.setContentText("Are you sure you want to delete this user?")
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
							swal.setTitleText("Deleting user...");
							swal.setCancelable(false);
							swal.setCanceledOnTouchOutside(false);
							swal.show();
							
							HashMap<String, Object> data = new HashMap<>();
							data.put("action_deleteUserData", "");
							data.put("user_name", model.userName);
							
							VolleyRequest.newRequest(holder.context, Constants.BASE_URL)
								.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
									@Override
									public void onVolleyResponseReceived(String response)
									{
										swal.dismissWithAnimation();
										try {
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
													activity.loadAllUserData();
												}
											});
											swal.show();
										} catch(Exception e) {
											e.printStackTrace();
											android.util.Log.e(AdminActivity.class.toString(), e.getMessage());
										}
									}
								})
								.setEndPoint("user/deleteUserData.php")
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
				if(!BaseApplication.conn.isConnected(holder.context))
					return;
				
				final SweetAlertDialog swal = new SweetAlertDialog(holder.context, SweetAlertDialog.PROGRESS_TYPE);
				swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
				swal.setTitleText("Fetching user data...");
				swal.setCancelable(false);
				swal.setCanceledOnTouchOutside(false);
				swal.show();
				
				HashMap<String, Object> data = new HashMap<>();
				data.put("action_getUserData", "");
				data.put("user_name", model.userName);
				
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
									AdminEditUserDialog.init(
										holder.context, 
										UserModel.newUser(
											jobj.getString("user_name"),
											jobj.getString("user_pass"),
											jobj.getString("user_cent"),
											String.valueOf(jobj.getInt("user_rank")),
											jobj.getString("user_email"),
											jobj.getString("user_number")
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
								
								new SweetAlertDialog(holder.context, SweetAlertDialog.WARNING_TYPE)
									.setTitleText("Fetching Failed")
									.setContentText(e.getMessage())
									.setConfirmText("Okay")
									.show();
								
								android.util.Log.d(UserListAdapter.class.toString(), e.getMessage());
							}
						}
					})
					.setEndPoint("user/getUserData.php")
					.sendRequest(data);
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return userList.size();
	}

	@Override
	public Filter getFilter()
	{
		return filter;
	}

	public class UserListViewHolder extends ViewHolder
	{
		public Context context;
		public SharedPreferences prefs;
		public ImageButton btn_edit, btn_delete;
		public LinearLayout parent;
		public TextView tv_userName, tv_userRank;

		public UserListViewHolder(View view)
		{
			super(view);

			context = view.getContext();
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
			parent = (LinearLayout) view;
			btn_edit = view.findViewById(R.id.content_list_edit);
			btn_delete = view.findViewById(R.id.content_list_delete);
			tv_userName = view.findViewById(R.id.content_list_name);
			tv_userRank = view.findViewById(R.id.content_list_rank);
		}
	}

	private Filter filter = new Filter()
	{
		@Override
		protected Filter.FilterResults performFiltering(CharSequence constraint)
		{
			List<UserModel> filteredList = new ArrayList<>();

			if(constraint != null && constraint.length() > 0)
			{
				String filterPattern = constraint.toString().toLowerCase().trim();
				for(UserModel model : userListFull)
					if(model.userName.toLowerCase().contains(filterPattern))
						filteredList.add(model);
			}
			else
				filteredList.addAll(userListFull);

			Filter.FilterResults result = new Filter.FilterResults();
			result.values = filteredList;
			return result;
		}

		@Override
		protected void publishResults(CharSequence constraint, Filter.FilterResults result)
		{
			userList.clear();
			userList.addAll((List) result.values);
			notifyDataSetChanged();
		}
	};
}
