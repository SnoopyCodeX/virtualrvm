package com.cdph.virtualrvm.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
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
import android.widget.Adapter;

public class AdminAddUserDialog implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private static final String[] RANKS = {"Member", "Admin"};
	
	private AdminActivity activity;
	private Typeface flatFont;
	private View parent;
	private Button btnAdd, btnCancel;
	private EditText etUser, etPass, etCent;
	private Spinner spRank;
	private TextView header;
	private AlertDialog dlg;
	private Context ctx;
	
	private AdminAddUserDialog()
	{}

	private AdminAddUserDialog(Context ctx)
	{
		this.ctx = ctx;
	}

	public static synchronized AdminAddUserDialog init(Context ctx)
	{
		return (new AdminAddUserDialog(ctx));
	}

	public AdminAddUserDialog setActivity(AdminActivity activity)
	{
		this.activity = activity;
		return this;
	}

	public void show()
	{
		init();
		if(dlg != null || !dlg.isShowing())
			dlg.show();
	}

	private void init()
	{
		dlg = new AlertDialog.Builder(ctx).create();
		dlg.setCancelable(false);
		dlg.setCanceledOnTouchOutside(false);

		flatFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/quicksand_light.ttf");
		parent = LayoutInflater.from(ctx).inflate(R.layout.admin_add_user, null);
		header = parent.findViewById(R.id.et_header);
		etUser = parent.findViewById(R.id.et_username);
		etPass = parent.findViewById(R.id.et_password);
		etCent = parent.findViewById(R.id.et_cent);
		spRank = parent.findViewById(R.id.et_rank);
		btnAdd = parent.findViewById(R.id.bt_add);
		btnCancel = parent.findViewById(R.id.bt_cancel);

		btnAdd.setOnClickListener(this);
		btnCancel.setOnClickListener(this);

		etUser.setTypeface(flatFont);
		etPass.setTypeface(flatFont);
		etCent.setTypeface(flatFont);
		btnAdd.setTypeface(flatFont);
		btnCancel.setTypeface(flatFont);
		header.setTypeface(flatFont, Typeface.BOLD);

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
		spRank.setSelection(0);

		dlg.setView(parent);
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.bt_add:
				if(BaseApplication.conn.isConnected(ctx))
				{
					String newUsername = etUser.getText().toString();
					String newPassword = etPass.getText().toString();
					String newCents = etCent.getText().toString();

					if(TextUtils.isEmpty(newUsername))
					{
						etUser.setError("Field can't be empty!");
						return;
					}
					
					if(TextUtils.isEmpty(newPassword))
					{
						etPass.setError("Field can't be empty!");
						return;
					}
					
					if(TextUtils.isEmpty(newCents))
					{
						etCent.setError("Field can't be empty");
						return;
					}
					
					dlg.dismiss();
					if(!newCents.contains("¢") || !newCents.contains("₱"))
					{
						double i = Double.parseDouble(newCents);
						
						if(i >= 1)
							newCents = "₱" + i;
						else
							newCents = i + "¢";
					}

					HashMap<String, Object> data = new HashMap<>();
					data.put("action_addNewUser", "");
					data.put("user_name", newUsername);
					data.put("user_pass", Base64.encodeToString(newPassword.getBytes(), Base64.DEFAULT));
					data.put("user_cent", newCents);
					data.put("user_rank", spRank.getSelectedItemPosition());

					final SweetAlertDialog swal = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
					swal.setCancelable(false);
					swal.setCanceledOnTouchOutside(false);
					swal.setTitleText("Adding new user...");
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
									swal.setTitleText((hasError) ? "Failed" : "Success");
									swal.setContentText(message);
									swal.setConfirmText("Okay");
									swal.show();

									activity.loadAllUserData();
								} catch(Exception e) {
									e.printStackTrace();
								}
							}
						})
						.setEndPoint("user/addNewUser.php")
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
