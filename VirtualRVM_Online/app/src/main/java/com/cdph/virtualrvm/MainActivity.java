package com.cdph.virtualrvm;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.auth.AccountManager;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.util.Constants;

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler, CompoundButton.OnCheckedChangeListener, View.OnClickListener
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
	
	private void initViews()
	{
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
		//Send request to the server
		
		Handler resetHandler = new Handler();
		resetHandler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				scannerView.resumeCameraPreview(MainActivity.this);
			}
		}, 5000);
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
