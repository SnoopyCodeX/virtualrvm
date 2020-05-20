package com.cdph.virtualrvm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.cdph.virtualrvm.db.VirtualRVMDatabase;
import com.cdph.virtualrvm.util.Constants;

public class LoginRegisterActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
	private VirtualRVMDatabase db;
	private SharedPreferences sp;
	private Typeface flatFont;
	private LinearLayout form_signin, form_signup;
	private Button btn_signin, btn_signup;
	private EditText input_signin_username, input_signin_password, input_signup_username, input_signup_password;
	private TextView tv_signin, tv_signup, tv_bottom_nav, tv_bottom_info;
	private CheckBox cb_signin_remember;
	private boolean signUpFormShown = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_register);
		
		initViews();
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		if(sp.getBoolean(Constants.KEY_FIRST_LAUNCH, true))
			initData();
		
		if(sp.getBoolean(Constants.KEY_REMEMBER, false) && sp.getInt(Constants.KEY_RANK, 0) == 0)
			startActivity(new Intent(this, MainActivity.class));
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.logreg_bottom_nav:
				signUpFormShown = !signUpFormShown;
				
				if(signUpFormShown)
					showSignUpForm();
				else
					showSignInForm();
			break;
			
			case R.id.logreg_signin_btn:
				String str_signin_username = input_signin_username.getText().toString();
				String str_signin_password = input_signin_password.getText().toString();
				boolean rememberLogin = cb_signin_remember.isChecked();
				
				signin(str_signin_username, str_signin_password, rememberLogin);
			break;
			
			case R.id.logreg_signup_btn:
				String str_signup_username = input_signup_username.getText().toString();
				String str_signup_password = input_signup_password.getText().toString();
				
				signup(str_signup_username, str_signup_password);
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton btn, boolean checked)
	{}
	
	private void initViews()
	{
		db = new VirtualRVMDatabase(this);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		flatFont = Typeface.createFromAsset(getAssets(), "fonts/quicksand_light.ttf");
		
		tv_signin = findViewById(R.id.logreg_signin_header);
		tv_signup = findViewById(R.id.logreg_signup_header);
		tv_signin.setTypeface(flatFont);
		tv_signup.setTypeface(flatFont);
		
		form_signin = findViewById(R.id.logreg_signin);
		form_signup = findViewById(R.id.logreg_signup);
		
		input_signin_username = findViewById(R.id.logreg_signin_username);
		input_signin_password = findViewById(R.id.logreg_signin_password);
		input_signin_username.setTypeface(flatFont);
		input_signin_password.setTypeface(flatFont);
		
		input_signup_username = findViewById(R.id.logreg_signup_username);
		input_signup_password = findViewById(R.id.logreg_signup_password);
		input_signup_username.setTypeface(flatFont);
		input_signup_password.setTypeface(flatFont);
		
		btn_signin = findViewById(R.id.logreg_signin_btn);
		btn_signup = findViewById(R.id.logreg_signup_btn);
		btn_signin.setTypeface(flatFont);
		btn_signup.setTypeface(flatFont);
		btn_signin.setOnClickListener(this);
		btn_signup.setOnClickListener(this);
		
		tv_bottom_nav = findViewById(R.id.logreg_bottom_nav);
		tv_bottom_info = findViewById(R.id.logreg_bottom_info);
		tv_bottom_nav.setTypeface(flatFont, Typeface.BOLD);
		tv_bottom_info.setTypeface(flatFont);
		tv_bottom_nav.setOnClickListener(this);
		
		cb_signin_remember = findViewById(R.id.logreg_signin_remember);
		cb_signin_remember.setTypeface(flatFont);
		cb_signin_remember.setOnCheckedChangeListener(this);
	}
	
	private void showSignUpForm()
	{
		form_signin.setVisibility(View.GONE);
		form_signup.setVisibility(View.VISIBLE);
		tv_bottom_nav.setText(R.string.nav_signin);
		tv_bottom_info.setText(R.string.nav_already_have_account);
	}
	
	private void showSignInForm()
	{
		form_signin.setVisibility(View.VISIBLE);
		form_signup.setVisibility(View.GONE);
		tv_bottom_nav.setText(R.string.nav_signup);
		tv_bottom_info.setText(R.string.nav_dont_have_account);
	}
	
	private void signin(String username, String password, boolean rememberLogin)
	{
		if(username.isEmpty() && !password.isEmpty())
		{
			input_signin_username.setError(getString(R.string.empty_field));
			return;
		}
		
		if(!username.isEmpty() && password.isEmpty())
		{
			input_signin_password.setError(getString(R.string.empty_field));
			return;
		}
		
		if(!username.isEmpty() && password.length() < 8)
		{
			input_signin_password.setError(getString(R.string.password_too_short));
			return;
		}
		
		if(username.isEmpty() && password.isEmpty())
		{
			input_signin_username.setError(getString(R.string.empty_field));
			input_signin_password.setError(getString(R.string.empty_field));
			return;
		}
		
		if(db.getUserData(username) == null)
		{
			input_signin_username.setError(getString(R.string.not_valid_account));
			return;
		}
		
		if(!(new String(Base64.decode(db.getUserData(username)[1], Base64.DEFAULT)).equals(password)))
		{
			input_signin_password.setError(getString(R.string.incorrect_password));
			return;
		}
		
		String[] userData = db.getUserData(username);
		SharedPreferences.Editor data = sp.edit();
		data.putString(Constants.KEY_USERNAME, userData[0]).commit();
		data.putString(Constants.KEY_CENTS, userData[2]).commit();
		data.putInt(Constants.KEY_RANK, Integer.parseInt(userData[3])).commit();
		data.putBoolean(Constants.KEY_REMEMBER, rememberLogin).commit();
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
	
	private void signup(String username, String password)
	{
		if(username.isEmpty() && !password.isEmpty())
		{
			input_signup_username.setError(getString(R.string.empty_field));
			return;
		}

		if(!username.isEmpty() && password.isEmpty())
		{
			input_signup_password.setError(getString(R.string.empty_field));
			return;
		}

		if(!username.isEmpty() && password.length() < 8)
		{
			input_signup_password.setError(getString(R.string.password_too_short));
			return;
		}

		if(username.isEmpty() && password.isEmpty())
		{
			input_signup_username.setError(getString(R.string.empty_field));
			input_signup_password.setError(getString(R.string.empty_field));
			return;
		}

		if(db.getUserData(username) != null)
		{
			input_signup_username.setError(getString(R.string.user_exists));
			Toast.makeText(this, getString(R.string.user_exists), Toast.LENGTH_LONG).show();
			return;
		}
		
		db.insertUserData(username, Base64.encodeToString(password.getBytes(), Base64.DEFAULT), 0, "0.0¢");
		Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_LONG).show();
	}
	
	private void initData()
	{
		db.insertItemData("Aquafina", "500mL", "4803925250019", "bottle", "0.50¢");
		db.insertItemData("Nature's Spring", "350mL", "4800049720107", "bottle", "0.25¢");
		db.insertItemData("Nature's Spring", "500mL", "4800049720114", "bottle", "0.50¢");
		
		db.insertItemData("Sprite", "330mL", "4801981110209", "can", "0.25¢");
		db.insertItemData("Royal", "330mL", "4801981110100", "can", "0.25¢");
		db.insertItemData("Coca Cola", "330mL", "4801981110001", "can", "0.25¢");
		
		sp.edit().putBoolean(Constants.KEY_FIRST_LAUNCH, false).commit();
	}
}
