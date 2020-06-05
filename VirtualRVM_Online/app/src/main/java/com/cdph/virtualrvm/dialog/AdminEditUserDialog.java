package com.cdph.virtualrvm.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v7.app.AlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.AdminActivity;
import com.cdph.virtualrvm.BaseApplication;
import com.cdph.virtualrvm.model.UserModel;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;
import com.cdph.virtualrvm.R;

public class AdminEditUserDialog implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
	private static final String[] RANKS = {"Member", "Admin"};
	
	private SharedPreferences prefs;
	private AdminActivity activity;
    private UserModel userData;
	private Typeface flatFont;
	private View parent;
	private Button btnSave, btnCancel;
	private EditText etUser, etPass, etCent;
	private Spinner spRank;
	private TextView header;
	private AlertDialog dlg;
	private Context ctx;
	
	private AdminEditUserDialog()
	{}
	
	private AdminEditUserDialog(Context ctx, UserModel userData)
	{
		this.ctx = ctx;
		this.userData = userData;
	}
	
	public static synchronized AdminEditUserDialog init(Context ctx, UserModel userData)
	{
		return (new AdminEditUserDialog(ctx, userData));
	}
	
	public AdminEditUserDialog setActivity(AdminActivity activity)
	{
		this.activity = activity;
		return this;
	}
	
	public void show()
	{
		try {
			init();
			if(dlg != null || !dlg.isShowing())
				dlg.show();
		} catch(Exception e) {
			e.printStackTrace();
			android.util.Log.d(AdminEditUserDialog.class.toString(), e.getMessage());
		}
	}
	
	private void init() throws Exception
	{
		dlg = new AlertDialog.Builder(ctx).create();
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		flatFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/quicksand_light.ttf");
		parent = LayoutInflater.from(ctx).inflate(R.layout.admin_edit_user, null);
		header = parent.findViewById(R.id.et_header);
		etUser = parent.findViewById(R.id.et_username);
		etPass = parent.findViewById(R.id.et_password);
		etCent = parent.findViewById(R.id.et_cent);
		spRank = parent.findViewById(R.id.et_rank);
		btnSave = parent.findViewById(R.id.bt_save);
		btnCancel = parent.findViewById(R.id.bt_cancel);
		
		String encodedPass = userData.userPass;
		String decodedPass = new String(Base64.decode(encodedPass.getBytes(), Base64.DEFAULT));
		etUser.setText(userData.userName);
		etPass.setText(decodedPass);
		etCent.setText(userData.userCent);
		
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		
		etUser.setTypeface(flatFont);
		etPass.setTypeface(flatFont);
		etCent.setTypeface(flatFont);
		btnSave.setTypeface(flatFont);
		btnCancel.setTypeface(flatFont);
		header.setTypeface(flatFont, Typeface.BOLD);
		
		if(prefs.getString(Constants.KEY_USERNAME, "").equals(userData.userName))
		{
			etPass.setEnabled(true);
			etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			etPass.setSelection(decodedPass.length());
		}
		
		ArrayAdapter adapter = new ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item) {
			@Override
			public View getView(int position, View view, ViewGroup parent)
			{
				View v = super.getView(position, view, parent);
				
				TextView tv = (TextView) v;
				tv.setTextColor(android.graphics.Color.parseColor("#00d170"));
				tv.setTypeface(flatFont, Typeface.BOLD);
				
				return v;
			}
		};
		
		adapter.addAll(RANKS);
		spRank.setAdapter(adapter);
		spRank.setOnItemSelectedListener(this);
		spRank.setSelection((userData.userRank.equals("0")) ? 0 : 1);
		
		dlg.setView(parent);
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.bt_save:
				if(BaseApplication.conn.isConnected(ctx))
				{
					String oldUsername = userData.userName;
					String oldPassword = new String(Base64.decode(userData.userPass, Base64.DEFAULT));
					String oldCents = userData.userCent;
					String oldRank = userData.userRank;
					String newUsername = etUser.getText().toString();
					String newPassword = etPass.getText().toString();
					String newCents = etCent.getText().toString();
					String newRank = String.valueOf(spRank.getSelectedItemPosition());
					dlg.dismiss();
					
					if(oldUsername.equals(newUsername) && oldCents.equals(newCents) && oldRank.equals(newRank))
						return;
						
					if(TextUtils.isEmpty(newUsername))
						newUsername = oldUsername;
						
					if(TextUtils.isEmpty(newPassword))
						newPassword = oldPassword;
						
					if(TextUtils.isEmpty(newCents))
						newCents = oldCents;
						
					if(TextUtils.isEmpty(newRank))
						newRank = oldRank;
						
					if(oldUsername.equals(newUsername) && oldCents.equals(newCents) && oldRank.equals(newRank))
						return;
						
					if(!newCents.contains("¢") || !newCents.contains("₱"))
					{
						double i = Double.parseDouble(newCents.replaceAll("[¢|₱]", ""));

						if(i >= 1)
							newCents = "₱" + i;
						else
							newCents = i + "¢";
					}
						
					HashMap<String, Object> data = new HashMap<>();
					data.put("action_updateUserData", "");
					data.put("old_username", oldUsername);
					data.put("user_name", newUsername);
					data.put("user_pass", Base64.encodeToString(newPassword.getBytes(), Base64.DEFAULT));
					data.put("user_cent", newCents);
					data.put("user_rank", spRank.getSelectedItemPosition());
						
					final SweetAlertDialog swal = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
					swal.setCancelable(false);
					swal.setCanceledOnTouchOutside(false);
					swal.setTitleText("Updating user...");
					swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
					swal.show();
					
					VolleyRequest.newRequest(ctx, Constants.BASE_URL)
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
									
									SweetAlertDialog swal = new SweetAlertDialog(ctx, (hasError) ? SweetAlertDialog.ERROR_TYPE : SweetAlertDialog.SUCCESS_TYPE);
									swal.setCancelable(false);
									swal.setCanceledOnTouchOutside(false);
									swal.setTitleText((hasError) ? "Update Failed" : "Update Success");
									swal.setContentText(message);
									swal.setConfirmText("Okay");
									swal.show();
									
									activity.loadAllUserData();
								} catch(Exception e) {
									e.printStackTrace();
								}
							}
						})
						.setEndPoint("user/updateUserData.php")
						.sendRequest(data);
					
					return;
				}
			break;
			
			case R.id.bt_cancel:
				dlg.dismiss();
				dlg = null;
			break;
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int position, long id)
	{}

	@Override
	public void onNothingSelected(AdapterView<?> p1) 
	{}
}
