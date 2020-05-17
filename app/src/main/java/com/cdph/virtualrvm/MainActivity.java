package com.cdph.virtualrvm;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler
{
	private SharedPreferences preference;
	private ZBarScannerView scannerView;
	private TextView scannerInfo;
	
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
	
	private void initViews()
	{
		preference = PreferenceManager.getDefaultSharedPreferences(this);
		scannerView = findViewById(R.id.scanner_view);
		scannerInfo = findViewById(R.id.scanner_info);
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
		{
			Toast.makeText(this, "Permissions needs to be granted!", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	@Override
	public void handleResult(Result result)
	{
		String info = "Type: %s\nId: %s";
		scannerInfo.setText(String.format(info, result.getBarcodeFormat().getName(), result.getContents()));
		
		Handler resetHandler = new Handler();
		resetHandler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				scannerView.resumeCameraPreview(MainActivity.this);
			}
		}, 100);
	}
}
