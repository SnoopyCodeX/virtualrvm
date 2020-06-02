package com.cdph.virtualrvm.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v7.app.AlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.AdminActivity;
import com.cdph.virtualrvm.BaseApplication;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;
import com.cdph.virtualrvm.R;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class AdminAddItemDialog implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ZBarScannerView.ResultHandler
{
    private AdminActivity activity;
	private ZBarScannerView scanner;
	private Switch swUseFlash;
	private Typeface flatFont;
	private View parent;
	private Button btnAdd, btnCancel;
	private EditText etId, etName, etWeight, etType, etWorth;
	private TextView header1, header2;
	private AlertDialog dlg;
	private Context ctx;
	
	private AdminAddItemDialog()
	{}
	
	private AdminAddItemDialog(Context ctx)
	{
		this.ctx = ctx;
	}
	
	public static synchronized AdminAddItemDialog init(Context ctx)
	{
		return (new AdminAddItemDialog(ctx));
	}
	
	public AdminAddItemDialog setActivity(AdminActivity activity)
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
		
		parent = LayoutInflater.from(ctx).inflate(R.layout.admin_add_item, null);
		flatFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/quicksand_light.ttf");
		scanner = parent.findViewById(R.id.scanner_view);
		swUseFlash = parent.findViewById(R.id.scanner_useflash);
		header1 = parent.findViewById(R.id.et_header_1);
		header2 = parent.findViewById(R.id.et_header_2);
		btnAdd = parent.findViewById(R.id.bt_add);
		btnCancel = parent.findViewById(R.id.bt_cancel);
		etId = parent.findViewById(R.id.et_itemid);
		etName = parent.findViewById(R.id.et_itemname);
		etWeight = parent.findViewById(R.id.et_itemweight);
		etType = parent.findViewById(R.id.et_itemtype);
		etWorth = parent.findViewById(R.id.et_itemworth);
		
		swUseFlash.setTypeface(flatFont);
		header1.setTypeface(flatFont, Typeface.BOLD);
		header2.setTypeface(flatFont, Typeface.BOLD);
		btnAdd.setTypeface(flatFont, Typeface.BOLD);
		btnCancel.setTypeface(flatFont, Typeface.BOLD);
		etId.setTypeface(flatFont);
		etName.setTypeface(flatFont);
		etWeight.setTypeface(flatFont);
		etType.setTypeface(flatFont);
		etWorth.setTypeface(flatFont);
		
		btnAdd.setEnabled(false);
		btnAdd.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		swUseFlash.setOnCheckedChangeListener(this);
		scanner.setResultHandler(this);
		
		scanner.setAspectTolerance(0.2f);
		scanner.startCamera();
		
		etId.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence cs, int p1, int p2, int p3)
			{}
			
			@Override
			public void afterTextChanged(Editable et)
			{}
			
			@Override
			public void onTextChanged(CharSequence cs, int p1, int p2, int p3)
			{
				if(TextUtils.isEmpty(cs))
				{
					header2.setVisibility(View.VISIBLE);
					scanner.setVisibility(View.VISIBLE);
					swUseFlash.setVisibility(View.VISIBLE);
				}
				else
				{
					header2.setVisibility(View.GONE);
					scanner.setVisibility(View.GONE);
					swUseFlash.setVisibility(View.GONE);
				}
				
				etName.setEnabled(!TextUtils.isEmpty(cs) && TextUtils.isDigitsOnly(cs));
				etWeight.setEnabled(!TextUtils.isEmpty(cs) && TextUtils.isDigitsOnly(cs));
				etType.setEnabled(!TextUtils.isEmpty(cs) && TextUtils.isDigitsOnly(cs));
				etWorth.setEnabled(!TextUtils.isEmpty(cs) && TextUtils.isDigitsOnly(cs));
				btnAdd.setEnabled(!TextUtils.isEmpty(cs) && TextUtils.isDigitsOnly(cs));
			}
		});
		etId.clearComposingText();
		
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
					String id = etId.getText().toString();
					String name = etName.getText().toString();
					String weight = etWeight.getText().toString();
					String type = etType.getText().toString();
					String worth = etWorth.getText().toString();
					
					if(TextUtils.isEmpty(name))
					{
						etName.setError("Field can't be empty!");
						return;
					}
					
					if(TextUtils.isEmpty(weight))
					{
						etWeight.setError("Field can't be empty!");
						return;
					}
					
					if(TextUtils.isEmpty(type))
					{
						etType.setError("Field can't be empty!");
						return;
					}
					
					if(TextUtils.isEmpty(worth))
					{
						etWorth.setError("Field can't be empty!");
						return;
					}
					
					if(TextUtils.isDigitsOnly(weight))
					{
						new SweetAlertDialog(ctx, SweetAlertDialog.WARNING_TYPE)
							.setTitleText("Warning")
							.setContentText("Please specify it's weight with these conversions, ie: ml, mg, kg, etc.")
							.setConfirmText("Okay")
							.show();
							
						return;
					}
					
					if(!TextUtils.isDigitsOnly(worth.replaceAll("[.]", "")))
					{
						etWorth.setError("Only numbers are allowed on this field!");
						return;
					}
						
					Double i = Double.parseDouble(worth);
					if(i >= 1)
						worth = "₱" + worth;
					else
						worth += "¢";
						
					HashMap<String, Object> data = new HashMap<>();
					data.put("action_addNewItem", "");
					data.put("item_id", id);
					data.put("item_name", name);
					data.put("item_weight", weight);
					data.put("item_type", type);
					data.put("item_worth", worth);
					
					final SweetAlertDialog swal = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
					swal.setCancelable(false);
					swal.setCanceledOnTouchOutside(false);
					swal.setTitleText("Adding new item...");
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
									
									activity.loadAllItemData();
								} catch(Exception e) {
									e.printStackTrace();
								}
							}
						})
						.setEndPoint("item/addNewItem.php")
						.sendRequest(data);
				}
			break;
			
			case R.id.bt_cancel:
				scanner.stopCamera();
				dlg.dismiss();
				dlg = null;
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton cb, boolean checked)
	{
		swUseFlash.setText((checked) ? R.string.useflash_on : R.string.useflash_off);
		scanner.setFlash(checked);
	}
	
	@Override
	public void handleResult(Result result)
	{
		etId.setText(result.getContents());
		
		Handler resetHandler = new Handler();
		resetHandler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				scanner.resumeCameraPreview(AdminAddItemDialog.this);
			}
		}, 5000);
	}
}
