package com.cdph.virtualrvm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.cdph.virtualrvm.db.VirtualRVMDatabase;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.model.UserModel;
import com.cdph.virtualrvm.util.Constants;

public class AdminActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener
{
	private SharedPreferences pref;
	private VirtualRVMDatabase db;
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
            	header.setText(R.string.admin_items_header);
				searchView.setHint("Search items...");
				ret = true;
            break;
			
			case R.id.action_users:
				loadAllUserData();
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
						
					break;
				}
			break;
		}
	}
	
	@Override
	public void onBackPressed() 
	{
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
							signout();
						}
					}).show();
			break;
		}
		
		return false;
	}
    
    private void initViews()
    {
		db = new VirtualRVMDatabase(this);
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
		contentList.requestFocus();
    }
	
	public void loadAllItemData()
	{
		List<ArrayList<String>> items = db.getAllItemData();
		
		if(items == null)
		{
			new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
				.setTitleText("Warning")
				.setContentText("There seems to be no items stored on the database.")
				.setConfirmText("Okay")
				.show();
			
			contentList.setVisibility(View.GONE);
			emptyText.setVisibility(View.VISIBLE);
			emptyText.setText("No items to display");
			return;
		}
		
		List<ItemModel> itemModels = new ArrayList<>();
		
		for(ArrayList<String> itemData : items)
		{
			String id = itemData.get(0);
			String name = itemData.get(1);
			String weight = itemData.get(2);
			String type = itemData.get(3);
			String worth = itemData.get(4);
			
			itemModels.add(ItemModel.newItem(id, name, weight, type, worth));
		}
		
		itemAdapter = new ItemListAdapter(itemModels);
		itemAdapter.setActivity(this);
		userAdapter = null;
		
		contentList.setVisibility(View.VISIBLE);
		emptyText.setVisibility(View.GONE);
		contentList.setAdapter(itemAdapter);
		contentList.requestFocus();
	}
	
	public void loadAllUserData()
	{
		List<ArrayList<String>> users = db.getAllUserData();
		
		if(users == null)
		{
			new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
				.setTitleText("Warning")
				.setContentText("There seems to be no users registered on the database.")
				.setConfirmText("Okay")
				.show();
			
			contentList.setVisibility(View.GONE);
			emptyText.setVisibility(View.VISIBLE);
			emptyText.setText("No users to display");
				
			return;
		}
		
		List<UserModel> userModels = new ArrayList<>();

		for(ArrayList<String> userData : users)
		{
			String name = userData.get(0);
			String pass = userData.get(1);
			String cent = userData.get(2);
			String rank = userData.get(3);

			userModels.add(UserModel.newUser(name, pass, cent, rank));
		}

		userAdapter = new UserListAdapter(userModels);
		userAdapter.setActivity(this);
		itemAdapter = null;
		
		contentList.setVisibility(View.VISIBLE);
		emptyText.setVisibility(View.GONE);
 		contentList.setAdapter(userAdapter);
		contentList.requestFocus();
	}
	
	private void signout()
	{
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(Constants.KEY_CENTS, "")
			.putString(Constants.KEY_RANK, "")
			.putBoolean(Constants.KEY_REMEMBER, false)
			.putString(Constants.KEY_USERNAME, "").commit();

		startActivity(new Intent(this, LoginRegisterActivity.class));
		finish();
	}
}
