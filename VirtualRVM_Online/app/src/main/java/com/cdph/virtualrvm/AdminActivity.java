package com.cdph.virtualrvm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;

import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.adapter.ItemListAdapter;
import com.cdph.virtualrvm.adapter.UserListAdapter;
import com.cdph.virtualrvm.dialog.AdminAddUserDialog;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.model.UserModel;
import com.cdph.virtualrvm.net.InternetConnection.OnInternetConnectionChangedListener;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;

public class AdminActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, OnInternetConnectionChangedListener
{
	private SharedPreferences pref;
	private ItemListAdapter itemAdapter;
	private UserListAdapter userAdapter;
    private BottomNavigationView bottomNav;
	private FloatingActionButton fabAdd;
	private RecyclerView contentList;
	private Typeface flatFont;
    private EditText searchView;
	private TextView header, emptyText;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        
        initViews();
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        boolean ret = false;
        
        switch(item.getItemId())
        {
            case R.id.action_items:
				loadAllItemData();
				fabAdd.setImageResource(R.drawable.add_item);
            	header.setText(R.string.admin_items_header);
				searchView.setHint("Search items...");
				ret = true;
            break;
			
			case R.id.action_users:
				loadAllUserData();
				fabAdd.setImageResource(R.drawable.add_user);
				header.setText(R.string.admin_users_header);
				searchView.setHint("Search username...");
				ret = true;
			break;
        }
        
        return ret;
    }
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.fab_add:
				switch(header.getText().toString())
				{
					case "Items List":
						
					break;
					
					case "Users List":
						AdminAddUserDialog.init(this)
							.setActivity(this)
							.show();
					break;
				}
			break;
		}
	}
	
	@Override
	public void onInternetConnectionChanged(boolean isConnected)
	{
		SweetAlertDialog swal = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
		
		if(!isConnected)
		{
			swal.setCancelable(false);
			swal.setCanceledOnTouchOutside(false);
			swal.setTitleText("Warning");
			swal.setContentText("No internet connection, please turn on your internet connection");
			swal.setConfirmText("Okay");
			swal.setCancelText("No");

			swal.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
				@Override
				public void onClick(SweetAlertDialog dlg)
				{
					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				}
			});

			swal.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
				@Override
				public void onClick(SweetAlertDialog dlg)
				{
					finish();
				}
			});

			swal.show();
			
			return;
		}
		
		if(swal.isShowing())
			swal.dismissWithAnimation();
	}
	
	@Override
	public void onBackPressed() 
	{
		finishAndRemoveTask();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.admin_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId())
		{
			case R.id.admin_signout:
				new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
					.setTitleText("Confirm Sign Out")
					.setContentText("Are you sure you want to sign out?")
					.setCancelText("Cancel")
					.setConfirmText("Sign Out")
					.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
						@Override
						public void onClick(SweetAlertDialog dlg)
						{
							dlg.dismissWithAnimation();
							
							pref.edit()
								.putString(Constants.KEY_CENTS, "")
								.putString(Constants.KEY_USERNAME, "")
								.putBoolean(Constants.KEY_REMEMBER, false)
								.putInt(Constants.KEY_RANK, 0)
								.commit();
								
							startActivity(new Intent(AdminActivity.this, LoginRegisterActivity.class));
							AdminActivity.this.finish();
						}
					}).show();
			break;
		}
		
		return false;
	}
    
    private void initViews()
    {
		BaseApplication.conn.addOnInternetConnectionChangedListener(this);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		flatFont = Typeface.createFromAsset(getAssets(), "fonts/quicksand_light.ttf");
		searchView = findViewById(R.id.content_list_searchView);
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(this);
		
		emptyText = findViewById(R.id.content_list_empty);
		emptyText.setTypeface(flatFont, Typeface.BOLD);
		
		contentList = findViewById(R.id.content_list);
		contentList.setLayoutManager(new LinearLayoutManager(this));
		
		fabAdd = findViewById(R.id.fab_add);
		fabAdd.setOnClickListener(this);
		
		header = findViewById(R.id.admin_list_header);
		header.setTypeface(flatFont, Typeface.BOLD);
		bottomNav.setSelectedItemId(R.id.action_items);
		
		searchView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence cs, int p1, int p2, int p3)
			{}
			
			@Override
			public void afterTextChanged(Editable e)
			{}
			
			@Override
			public void onTextChanged(CharSequence cs, int p1, int p2, int p3)
			{
				if(itemAdapter != null)
					itemAdapter.getFilter().filter(cs);
					
				if(userAdapter != null)
					userAdapter.getFilter().filter(cs);
			}
		});
		
		contentList.setVisibility(View.GONE);
		emptyText.setVisibility(View.VISIBLE);
		emptyText.requestFocus();
    }
	
	public void loadAllItemData()
	{
		if(!BaseApplication.conn.isConnected(this))
			return;
		
		HashMap<String, Object> data = new HashMap<>();
		data.put("action_getAllItems", "");
		
		final SweetAlertDialog pd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		pd.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
		pd.setTitleText("Loading items...");
		pd.setCancelable(false);
		pd.show();
		
		VolleyRequest.newRequest(this, Constants.BASE_URL)
			.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
				@Override
				public void onVolleyResponseReceived(String response)
				{
					pd.dismissWithAnimation();
					
					try {
						JSONArray jar = new JSONArray(response);
						JSONObject job = jar.getJSONObject(0);
						
						JSONArray jdat = job.getJSONArray("data");
						String message = job.getString("message");
						boolean hasError = job.getBoolean("hasError");
						
						if(!hasError)
						{
							List<ItemModel> items = new ArrayList<>();
							for(int i = 0; i < jdat.length() && jdat.length() > 0; i++)
							{
								JSONObject obj = jdat.getJSONObject(i);
								String id = obj.getString("item_id");
								String name = obj.getString("item_name");
								String weight = obj.getString("item_weight");
								String type = obj.getString("item_type");
								String worth = obj.getString("item_worth");
								
								items.add(ItemModel.newItem(id, name, weight, type, worth));
							}
							
							if(items.size() > 0)
							{
								ItemListAdapter adapter = new ItemListAdapter(items);
								adapter.setActivity(AdminActivity.this);
								emptyText.setVisibility(View.GONE);
								contentList.setVisibility(View.VISIBLE);
								contentList.setAdapter(adapter);
								return;
							}
							
							contentList.setVisibility(View.GONE);
							emptyText.setVisibility(View.VISIBLE);
							emptyText.setText("No items found...");
							return;
						}
						
						contentList.setVisibility(View.GONE);
						emptyText.setVisibility(View.VISIBLE);
						emptyText.setText("No items found...");
						
						pd.changeAlertType(SweetAlertDialog.ERROR_TYPE);
						pd.setTitleText("Operation Failed");
						pd.setContentText(message);
						pd.setCancelable(false);
						pd.setCanceledOnTouchOutside(false);
						pd.setConfirmText("Okay");
						pd.show();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			})
			.setEndPoint("item/getAllItems.php")
			.sendRequest(data);
	}
	
	public void loadAllUserData()
	{
		if(!BaseApplication.conn.isConnected(this))
			return;
		
		HashMap<String, Object> data = new HashMap<>();
		data.put("action_getAllUsers", "");
		
		final SweetAlertDialog pd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		pd.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
		pd.setTitleText("Loading users...");
		pd.setCancelable(false);
		pd.show();
		
		VolleyRequest.newRequest(this, Constants.BASE_URL)
			.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
				@Override
				public void onVolleyResponseReceived(String response)
				{
					pd.dismissWithAnimation();
					
					try {
						JSONArray jar = new JSONArray(response);
						JSONObject job = jar.getJSONObject(0);
						
						JSONArray jdat = job.getJSONArray("data");
						String message = job.getString("message");
						boolean hasError = job.getBoolean("hasError");

						if(!hasError)
						{
							List<UserModel> users = new ArrayList<>();
							for(int i = 0; i < jdat.length() && jdat.length() > 0; i++)
							{
								JSONObject obj = jdat.getJSONObject(i);
								String name = obj.getString("user_name");
								String pass = obj.getString("user_pass");
								String cent = obj.getString("user_cent");
								String rank = obj.getString("user_rank");

								users.add(UserModel.newUser(name, pass, cent, rank));
							}

							if(users.size() > 0)
							{
								UserListAdapter adapter = new UserListAdapter(users);
								adapter.setActivity(AdminActivity.this);
								emptyText.setVisibility(View.GONE);
								contentList.setVisibility(View.VISIBLE);
								contentList.setAdapter(adapter);
								return;
							}

							contentList.setVisibility(View.GONE);
							emptyText.setVisibility(View.VISIBLE);
							emptyText.setText("No users found...");
							return;
						}
						
						contentList.setVisibility(View.GONE);
						emptyText.setVisibility(View.VISIBLE);
						emptyText.setText("No users found...");

						pd.changeAlertType(SweetAlertDialog.ERROR_TYPE);
						pd.setTitleText("Operation Failed");
						pd.setContentText(message);
						pd.setCancelable(false);
						pd.setCanceledOnTouchOutside(false);
						pd.setConfirmText("Okay");
						pd.show();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			})
			.setEndPoint("user/getAllUsers.php")
			.sendRequest(data);
	}
}
