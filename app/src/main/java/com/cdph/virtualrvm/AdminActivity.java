package com.cdph.virtualrvm;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.cdph.virtualrvm.adapter.ItemListAdapter;
import com.cdph.virtualrvm.adapter.UserListAdapter;
import com.cdph.virtualrvm.db.VirtualRVMDatabase;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.model.UserModel;
import com.cdph.virtualrvm.util.Constants;
import android.util.Log;

public class AdminActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener
{
	private VirtualRVMDatabase db;
	private ItemListAdapter itemAdapter;
	private UserListAdapter userAdapter;
    private BottomNavigationView bottomNav;
	private FloatingActionButton fabAdd;
	private RecyclerView contentList;
	private Typeface flatFont;
    private EditText searchView;
	private TextView header;
    
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
	public void onBackPressed() 
	{
		finish();
	}
    
    private void initViews()
    {
		db = new VirtualRVMDatabase(this);
		flatFont = Typeface.createFromAsset(getAssets(), "fonts/quicksand_light.ttf");
		searchView = findViewById(R.id.content_list_searchView);
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(this);
		
		contentList = findViewById(R.id.content_list);
		contentList.setLayoutManager(new LinearLayoutManager(this));
		
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
		contentList.setAdapter(itemAdapter);
		contentList.requestFocus();
	}
	
	public void loadAllUserData()
	{
		List<ArrayList<String>> users = db.getAllUserData();
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
 		contentList.setAdapter(userAdapter);
		contentList.requestFocus();
	}
}
