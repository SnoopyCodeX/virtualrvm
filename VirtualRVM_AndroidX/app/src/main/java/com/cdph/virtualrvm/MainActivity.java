package com.cdph.virtualrvm;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.cdph.virtualrvm.db.VirtualRVMDatabase;
import com.cdph.virtualrvm.util.Constants;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, SurfaceHolder.Callback, Detector.Processor<Barcode>
{
	private VirtualRVMDatabase db;
	private SharedPreferences preference;
	private BarcodeDetector barcodeDetector;
	private CameraSource cameraSource;
	private SurfaceView scannerView;
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
	}
	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		finish();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		requestPermissions();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holded)
	{
		cameraSource.stop();
	}
	
	@Override
	public void release()
	{}
	
	@Override
	public void receiveDetections(Detector.Detections<Barcode> result)
	{
		SparseArray<Barcode> data = result.getDetectedItems();
		
		if(data.size() <= 0)
			return;
		
		String itemId = data.valueAt(0).displayValue;
		String[] itemData = db.getItemData(itemId);

		if(itemData != null)
		{
			String id = String.format("<font color=\"#00d170\">%s</font><br/>", itemData[0]);
			String name = String.format("<font color=\"#00d170\">%s</font><br/>", itemData[1]);
			String weight = String.format("<font color=\"#00d170\">%s</font><br/>", itemData[2]);
			String type = String.format("<font color=\"#00d170\">%s</font><br/>", itemData[3]);
			String amnt = String.format("<font color=\"#00d170\">%s</font><br/>", itemData[4]);
			scannerInfo.setText(Html.fromHtml(String.format(getString(R.string.scannerInfo_content), id, name, weight, type, amnt)));

			String[] userData = db.getUserData(preference.getString(Constants.KEY_USERNAME, ""));
			double user_cent = Double.parseDouble(userData[2].replace("¢", "").replace("₱", ""));
			double item_cent = Double.parseDouble(itemData[4].replace("¢", ""));
			db.updateUserData("user_cent", ((user_cent + item_cent) >= 1 ? "₱" : "") + String.valueOf(user_cent + item_cent) + ((user_cent + item_cent) < 1 ? "¢" : ""), userData[0]);
			updatePersonalDetail(((user_cent + item_cent) >= 1 ? "₱" : "") + String.valueOf(user_cent + item_cent) + ((user_cent + item_cent) < 1 ? "¢" : ""));

			Toast.makeText(this, Html.fromHtml(String.format(getString(R.string.item_valid), amnt)), Toast.LENGTH_LONG).show();
		}
		else
			Toast.makeText(this, getString(R.string.item_invalid), Toast.LENGTH_LONG).show();
	}
	
	private void initViews()
	{
		db = new VirtualRVMDatabase(this);
		flatFont = Typeface.createFromAsset(getAssets(), "fonts/quicksand_light.ttf");
		preference = PreferenceManager.getDefaultSharedPreferences(this);
		scannerView = findViewById(R.id.scanner_view);
		scannerInfo = findViewById(R.id.scanner_info);
		scannerInfoHeader = findViewById(R.id.scanner_info_header);
		useFlash = findViewById(R.id.scanner_useflash);
		signout = findViewById(R.id.main_signout_btn);
		personalDetail = findViewById(R.id.personal_details);
		personalDetailHeader = findViewById(R.id.personal_details_header);
		
		scannerView.getHolder().addCallback(this);
		barcodeDetector = new BarcodeDetector.Builder(this)
			.setBarcodeFormats(Barcode.ALL_FORMATS)
			.build();
			
		cameraSource = new CameraSource.Builder(this, barcodeDetector)
			.setRequestedPreviewSize(scannerView.getDisplay().getWidth(), scannerView.getDisplay().getHeight())
			.setAutoFocusEnabled(true)
			.build();
		
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
		try {
			cameraSource.start(scannerView.getHolder());
		} catch(Exception e) {
			e.printStackTrace();
			showAsPopup(e.getMessage());
		}
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
	
	private void showAsPopup(String message)
	{
		AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.setTitle("Crash Report");

		String _message = "Model: %s\nBrand: %s\nManufacturer: %s\nSdk Version: %s\n=====[Error]=====\n%s";
		dlg.setMessage(String.format(_message, Build.MODEL, Build.BRAND, Build.MANUFACTURER, Build.VERSION.SDK, message));
		dlg.setCanceledOnTouchOutside(false);
		dlg.setCancelable(true);
		dlg.show();
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
		{
			Toast.makeText(this, "Permissions needs to be granted!", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton btn, boolean checked)
	{
		if(checked)
			useFlash.setText(R.string.useflash_on);
		else
			useFlash.setText(R.string.useflash_off);
	}
	
	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.main_signout_btn:
				final AlertDialog dlg = new AlertDialog.Builder(this).create();
				dlg.setIcon(android.R.drawable.ic_dialog_alert);
				dlg.setMessage("Do you really want to sign out?");
				dlg.setTitle("Confirm");
				dlg.setCancelable(false);
				dlg.setCanceledOnTouchOutside(false);
				dlg.setButton(AlertDialog.BUTTON1, "Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface di, int which)
					{
						preference.edit().putString(Constants.KEY_USERNAME, "").commit();
						preference.edit().putBoolean(Constants.KEY_REMEMBER, false).commit();
						preference.edit().putString(Constants.KEY_CENTS, "").commit();
						startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class));
						finish();
					}
				});
				dlg.setButton(AlertDialog.BUTTON2, "No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface di, int which)
					{
						dlg.dismiss();
					}
				});
				dlg.show();
			break;
		}
	}
}
