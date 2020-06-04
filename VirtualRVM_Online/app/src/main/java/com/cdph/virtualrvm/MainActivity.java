package com.cdph.virtualrvm;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.auth.AccountManager;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.net.InternetConnection;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler, CompoundButton.OnCheckedChangeListener, View.OnClickListener, InternetConnection.OnInternetConnectionChangedListener
{
	private SharedPreferences preference;
	private ZBarScannerView scannerView;
	private Button signout;
	private Switch useFlash;
	private TextView scannerInfo, scannerInfoHeader, personalDetail, personalDetailHeader;
	private Typeface flatFont;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		initViews();
    }

	@Override
	protected void onResume()
	{
		super.onResume();
		
		boolean isAllPermsGranted = preference.getBoolean("isAllPermsGranted", false);
		if(!isAllPermsGranted)
			requestPermissions();
		else
			startScanner();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		
		scannerView.stopCamera();
	}
	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		finish();
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
	
	private void initViews()
	{
		BaseApplication.conn.addOnInternetConnectionChangedListener(this);
		flatFont = Typeface.createFromAsset(getAssets(), "fonts/quicksand_light.ttf");
		preference = PreferenceManager.getDefaultSharedPreferences(this);
		scannerView = findViewById(R.id.scanner_view);
		scannerInfo = findViewById(R.id.scanner_info);
		scannerInfoHeader = findViewById(R.id.scanner_info_header);
		useFlash = findViewById(R.id.scanner_useflash);
		signout = findViewById(R.id.main_signout_btn);
		personalDetail = findViewById(R.id.personal_details);
		personalDetailHeader = findViewById(R.id.personal_details_header);
		
		signout.setOnClickListener(this);
		useFlash.setOnCheckedChangeListener(this);
		useFlash.setTypeface(flatFont);
		
		scannerInfo.setTypeface(flatFont);
		scannerInfo.setText(String.format(getString(R.string.scannerInfo_content), "", "", "", "", ""));
		scannerInfoHeader.setTypeface(flatFont, Typeface.BOLD);
		
		String username = preference.getString(Constants.KEY_USERNAME, "");
		String cents = preference.getString(Constants.KEY_CENTS, "");
		Double cent = Double.parseDouble(cents.replace("¢", "").replace("₱", ""));
		
		if(cent >= 1)
			cents = "₱" + String.valueOf(cent);
		
		personalDetail.setTypeface(flatFont);
		personalDetail.setText(String.format(getString(R.string.personalDetail_content), username, cents));
		personalDetailHeader.setTypeface(flatFont, Typeface.BOLD);
	}
	
	private void updatePersonalDetail(String cent)
	{
		preference.edit().putString(Constants.KEY_CENTS, cent).commit();
		String username = preference.getString(Constants.KEY_USERNAME, "");
		String cents = preference.getString(Constants.KEY_CENTS, "");
		Double _cent_ = Double.parseDouble(cents.replace("¢", "").replace("₱", ""));

		if(_cent_ >= 1)
			cents = "₱" + String.valueOf(_cent_);
			
		personalDetail.setText(String.format(getString(R.string.personalDetail_content), username, cents));
	}
	
	private void startScanner()
	{
		scannerView.setResultHandler(this);
		scannerView.setAspectTolerance(0.2f);
		scannerView.startCamera();
	}
	
	private void requestPermissions()
	{
		ArrayList<String> permissions = new ArrayList<>();
		
		if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
			permissions.add(Manifest.permission.CAMERA);
			
		if(permissions.size() > 0)
			requestPermissions(permissions.toArray(new String[permissions.size()]), 21);
		else
			startScanner();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		boolean allGranted = false;
		if(requestCode == 21)
			for(int i = 0; i < grantResults.length; i++)
			{
				String permission = permissions[i];
				int granted = grantResults[i];
				
				if(permission.equals(Manifest.permission.CAMERA))
					allGranted = (granted == PackageManager.PERMISSION_GRANTED);
			}
		
		preference.edit().putBoolean("isAllPermsGranted", allGranted).commit();
		if(allGranted)
			startScanner();
		else
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
	}
	
	@Override
	public void handleResult(Result result)
	{
		if(!BaseApplication.conn.isConnected(this))
		{
			onInternetConnectionChanged(false);
			
			Handler resetHandler = new Handler();
			resetHandler.postDelayed(new Runnable() {
				@Override
				public void run()
				{
					scannerView.resumeCameraPreview(MainActivity.this);
				}
			}, 100);
			
			return;
		}
		
		String itemId = result.getContents();
		
		final SweetAlertDialog swal = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
		swal.setTitleText("Verifying item...");
		swal.setCancelable(false);
		swal.setCanceledOnTouchOutside(false);
		swal.show();
		
		HashMap<String, Object> data = new HashMap<>();
		data.put("action_getItemData", "");
		data.put("item_id", itemId);
		
		VolleyRequest.newRequest(this, Constants.BASE_URL)
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
							ItemModel model = ItemModel.newItem(
								jobj.getString("item_id"),
								jobj.getString("item_name"),
								jobj.getString("item_weight"),
								jobj.getString("item_type"),
								jobj.getString("item_worth")
							);

							final Double itemWorth = Double.parseDouble(model.itemWorth.replaceAll("\b(¢|₱)\b", ""));
							Double userCents = Double.parseDouble(preference.getString(Constants.KEY_CENTS, "").replaceAll("\b(¢|₱)\b", ""));
							Double _cent_ = itemWorth + userCents;
							String _cents_ = String.valueOf((_cent_ >= 1) ? "₱" + _cent_ : _cent_ + "¢");
							preference.edit().putString(Constants.KEY_CENTS, _cents_).commit();
							updatePersonalDetail(_cents_);
							
							final SweetAlertDialog swal = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
							swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
							swal.setTitleText("Updating your coins...");
							swal.setCancelable(false);
							swal.setCanceledOnTouchOutside(false);
							swal.show();
							
							HashMap<String, Object> data = new HashMap<>();
							data.put("action_updateUserData", "");
							data.put("old_username", preference.getString(Constants.KEY_USERNAME, ""));
							data.put("user_name", preference.getString(Constants.KEY_USERNAME, ""));
							data.put("user_pass", preference.getString(Constants.KEY_PASSWORD, ""));
							data.put("user_cent", _cents_);
							data.put("user_rank", 0);
							
							VolleyRequest.newRequest(MainActivity.this, Constants.BASE_URL)
								.addOnVolleyResponseReceivedListener(new VolleyRequest.OnVolleyResponseReceivedListener() {
									@Override
									public void onVolleyResponseReceived(String response)
									{
										swal.dismissWithAnimation();

										try {
											JSONArray jar = new JSONArray(response);
											JSONObject job = jar.getJSONObject(0);
											String message = job.getString("message");
											boolean hasError = job.getBoolean("hasError");

											final SweetAlertDialog swp = new SweetAlertDialog(MainActivity.this, ((hasError) ? SweetAlertDialog.ERROR_TYPE : SweetAlertDialog.SUCCESS_TYPE));
											swp.setCancelable(false);
											swp.setCanceledOnTouchOutside(false);
											swp.setTitleText((hasError) ? "Verify Failed" : "Verify Success");
											swp.setContentText((hasError) ? message : "Item is valid! You have gained " + itemWorth + "!");
											swp.setConfirmText("Okay");
											swp.show();
										} catch(Exception e) {
											e.printStackTrace();
										}
									}
								})
								.setEndPoint("user/updateUserData.php")
								.sendRequest(data);
						}
						
						final SweetAlertDialog swp = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
						swp.setCancelable(false);
						swp.setCanceledOnTouchOutside(false);
						swp.setTitleText("Verifying Failed");
						swp.setContentText(message);
						swp.setConfirmText("Okay");
						swp.show();
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					Handler resetHandler = new Handler();
					resetHandler.postDelayed(new Runnable() {
						@Override
						public void run()
						{
							scannerView.resumeCameraPreview(MainActivity.this);
						}
					}, 100);
				}
			})
			.setEndPoint("item/getItemData.php")
			.sendRequest(data);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton btn, boolean checked)
	{
		if(checked)
			useFlash.setText(R.string.useflash_on);
		else
			useFlash.setText(R.string.useflash_off);
		
		scannerView.setFlash(checked);
		scannerView.stopCamera();
		scannerView.startCamera();
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.main_signout_btn:
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
							
							AccountManager.getInstance(MainActivity.this)
								.removeUserData()
								.commit();
								
							MainActivity.this.finish();
							startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class));
						}
					}).show();
			break;
		}
	}
}
