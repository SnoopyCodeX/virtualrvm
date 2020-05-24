package com.cdph.virtualrvm.adapter;

import android.content.Context;
import android.graphics.Typeface;
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
import com.cdph.virtualrvm.model.UserModel;
import com.cdph.virtualrvm.R;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends Adapter<UserListAdapter.UserListViewHolder> implements Filterable
{
	private List<UserModel> userList;
	private List<UserModel> userListFull;

	public UserListAdapter(@NonNull List<UserModel> userList)
	{
		this.userList = userList;
		this.userListFull = new ArrayList<>(userList);
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

		holder.tv_userLabel.setText("Username");
		holder.tv_userName.setText(model.userName);

		holder.parent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{

			}
		});

		holder.btn_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{

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
		public ImageButton btn_edit, btn_delete;
		public LinearLayout parent;
		public TextView tv_userName, tv_userLabel;

		public UserListViewHolder(View view)
		{
			super(view);

			context = view.getContext();
			parent = (LinearLayout) view;
			btn_edit = view.findViewById(R.id.content_list_edit);
			btn_delete = view.findViewById(R.id.content_list_delete);
			tv_userName = view.findViewById(R.id.content_list_name);
			tv_userLabel = view.findViewById(R.id.content_list_label);
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
					if(model.userName.contains(filterPattern))
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
