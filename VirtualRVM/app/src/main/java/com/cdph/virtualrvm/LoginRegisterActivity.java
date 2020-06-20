package com.cdph.virtualrvm;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v7.app.AppCompatActivity;
import com.cdph.virtualrvm.auth.AccountManager;
import com.cdph.virtualrvm.net.InternetConnection.OnInternetConnectionChangedListener;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.model.UserModel;

public class LoginRegisterActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, OnInternetConnectionChangedListener
{
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
		
		try {
			initViews();
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.e(LoginRegisterActivity.class.toGenericString(), e.getMessage());
		}
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		try {
			if(sp.getBoolean(Constants.KEY_FIRST_LAUNCH, true))
			{
				sp.edit().putBoolean(Constants.KEY_FIRST_LAUNCH, false).commit();
				requestPermissions();
			}
		
			if(sp.getBoolean(Constants.KEY_REMEMBER, false))
				if(sp.getInt(Constants.KEY_RANK, 0) == 0)
					startActivity(new Intent(this, MainActivity.class));
				else
					startActivity(new Intent(this, AdminActivity.class));
			
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.e(LoginRegisterActivity.class.toString(), e.getMessage());
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
	
	private void requestPermissions()
	{
		try {
			ArrayList<String> permissions = new ArrayList<>();

			if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.CAMERA);

			if(checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.INTERNET);
				
			if(checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);

			if(permissions.size() > 0)
				requestPermissions(permissions.toArray(new String[permissions.size()]), 21);
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.e(LoginRegisterActivity.class.toString(), e.getMessage());
		}
	}
	
	@Override
	public void onClick(View view)
	{
		try {
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
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.e(LoginRegisterActivity.class.toString(), e.getMessage());
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		try {
			boolean allGranted = false;
			if(requestCode == 21)
				for(int i = 0; i < grantResults.length; i++)
				{
					String permission = permissions[i];
					int granted = grantResults[i];

					if(permission.equals(Manifest.permission.CAMERA))
						allGranted = (granted == PackageManager.PERMISSION_GRANTED);
						
					if(permission.equals(Manifest.permission.INTERNET))
						allGranted = (granted == PackageManager.PERMISSION_GRANTED);
						
					if(permission.equals(Manifest.permission.ACCESS_NETWORK_STATE))
						allGranted = (granted == PackageManager.PERMISSION_GRANTED);
				}

			sp.edit().putBoolean("isAllPermsGranted", allGranted).commit();
			if(!allGranted)
				new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
					.setTitleText("Warning")
					.setContentText("App needs all the permissions to be granted!")
					.setConfirmText("Grant Permissions")
					.setCancelText("Nope")
					.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
						@Override
						public void onClick(SweetAlertDialog dlg)
						{
							dlg.dismissWithAnimation();
							requestPermissions();
						}
					})
					.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
						@Override
						public void onClick(SweetAlertDialog dlg)
						{
							dlg.dismissWithAnimation();
							finish();
						}
					})
					.show();
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.e(LoginRegisterActivity.class.toString(), e.getMessage());
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton btn, boolean checked)
	{}
	
	private void initViews()
	{
		BaseApplication.conn.addOnInternetConnectionChangedListener(this);
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
	
	private void signin(String username, String password, final boolean rememberLogin)
	{
		try {
			if(username.isEmpty() && !password.isEmpty())
			{
				input_signin_username.setError(getString(R.string.empty_field));
				return;
			}

			else if(!username.isEmpty() && password.isEmpty())
			{
				input_signin_password.setError(getString(R.string.empty_field));
				return;
			}

			else if(!username.isEmpty() && password.length() < 8)
			{
				input_signin_password.setError(getString(R.string.password_too_short));
				return;
			}

			else if(username.isEmpty() && password.isEmpty())
			{
				input_signin_username.setError(getString(R.string.empty_field));
				input_signin_password.setError(getString(R.string.empty_field));
				return;
			}
			
			else if(!BaseApplication.conn.isConnected(this)) 
			{
				SweetAlertDialog swal = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
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
			
			HashMap<String, Object> data = new HashMap<>();
			data.put("action_getUserData_login", "");
			data.put("user_name", username);
			data.put("user_pass", password);
			
			final SweetAlertDialog pd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
			pd.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
			pd.setTitleText("Signing in...");
			pd.setCancelable(false);
			pd.show();
			
			VolleyRequest.newRequest(this, Constants.BASE_URL)
				.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
					@Override
					public void onVolleyResponseReceived(String response)
					{
						try {
							pd.dismissWithAnimation();
							JSONArray jar = new JSONArray(response);
							JSONObject job = jar.getJSONObject(0);

							JSONArray jdat = job.getJSONArray("data");
							String message = job.getString("message");
							boolean hasError = job.getBoolean("hasError");
							
							if(!hasError)
							{
								for(int i = 0; i < jdat.length(); i++)
								{
									JSONObject obj = jdat.getJSONObject(i);
									
									AccountManager.getInstance(LoginRegisterActivity.this)
										.saveUserData(
											obj.getString("user_name"),
											obj.getString("user_pass"),
											obj.getString("user_cent"),
											obj.getInt("user_rank"),
											rememberLogin
										)
										.commit();
								}
								
								startActivity(new Intent(LoginRegisterActivity.this, (sp.getInt(Constants.KEY_RANK, 0) == 0) ? MainActivity.class : AdminActivity.class));
								finish();
								return;
							}
							
							new SweetAlertDialog(LoginRegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
								.setTitleText("Sign In Failed")
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
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.e(LoginRegisterActivity.class.toString(), e.getMessage());
		}
	}
	
	private void signup(final String username, final String password)
	{
		try {
			if(username.isEmpty() && !password.isEmpty())
			{
				input_signup_username.setError(getString(R.string.empty_field));
				return;
			}

			else if(!username.isEmpty() && password.isEmpty())
			{
				input_signup_password.setError(getString(R.string.empty_field));
				return;
			}

			else if(!username.isEmpty() && password.length() < 8)
			{
				input_signup_password.setError(getString(R.string.password_too_short));
				return;
			}

			else if(username.isEmpty() && password.isEmpty())
			{
				input_signup_username.setError(getString(R.string.empty_field));
				input_signup_password.setError(getString(R.string.empty_field));
				return;
			}
			
			else if(!BaseApplication.conn.isConnected(this)) 
			{
				SweetAlertDialog swal = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
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
			
			HashMap<String, Object> data = new HashMap<>();
			data.put("action_addNewUser", "");
			data.put("user_name", username);
			data.put("user_pass", Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
			data.put("user_email", "");
			data.put("user_number", "");
			data.put("user_cent", "0.0¢");
			data.put("user_rank", 0);
			
			((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("", Base64.encodeToString(password.getBytes(), Base64.DEFAULT)));
			
			final SweetAlertDialog pd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
			pd.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
			pd.setTitleText("Signing up...");
			pd.setCancelable(false);
			pd.show();
			
			VolleyRequest.newRequest(this, Constants.BASE_URL)
				.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
					@Override
					public void onVolleyResponseReceived(String response)
					{
						try {
							pd.dismissWithAnimation();
							JSONArray jar = new JSONArray(response);
							JSONObject job = jar.getJSONObject(0);
							
							String message = job.getString("message");
							boolean hasError = job.getBoolean("hasError");
							
							new SweetAlertDialog(LoginRegisterActivity.this, ((hasError) ? SweetAlertDialog.ERROR_TYPE : SweetAlertDialog.SUCCESS_TYPE))
								.setTitleText((hasError) ? "Sign Up Failed" : "Sign Up Success")
								.setContentText(message)
								.setConfirmText("Okay")
								.show();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				})
				.setEndPoint("user/addNewUser.php")
				.sendRequest(data);
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.e(LoginRegisterActivity.class.toString(), e.getMessage());
		}
	}
}
