package com.cdph.virtualrvm.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v7.app.AlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.cdph.virtualrvm.AdminActivity;
import com.cdph.virtualrvm.BaseApplication;
import com.cdph.virtualrvm.model.ItemModel;
import com.cdph.virtualrvm.model.UserModel;
import com.cdph.virtualrvm.net.VolleyRequest;
import com.cdph.virtualrvm.util.Constants;
import com.cdph.virtualrvm.R;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class AdminVerifyCoinDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ZBarScannerView.ResultHandler
{
	private AdminActivity activity;
	private AlertDialog dlg;
	private UserModel userData;
	private Button btnCancel;
	private Switch scannerUseFlash;
	private TextView header1, header2, userCoin, userCoinLabel, adminCoin, adminCoinLabel;
	private ZBarScannerView scanner;
	private Typeface flatFont;
	private View parent;
	private Context ctx;
	
    private AdminVerifyCoinDialog()
	{}
	
	private AdminVerifyCoinDialog(Context ctx, UserModel userData)
	{
		this.ctx = ctx;
		this.userData = userData;
	}
	
	public static synchronized AdminVerifyCoinDialog init(Context ctx, UserModel userData)
	{
		return (new AdminVerifyCoinDialog(ctx, userData));
	}
	
	public AdminVerifyCoinDialog setActivity(AdminActivity activity)
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
		
		parent = LayoutInflater.from(ctx).inflate(R.layout.admin_verify_coin, null);
		flatFont = Typeface.createFromAsset(ctx.getAssets(), "fonts/quicksand_light.ttf");
		btnCancel = parent.findViewById(R.id.bt_cancel);
		scannerUseFlash = parent.findViewById(R.id.av_scanner_useflash);
		scanner = parent.findViewById(R.id.av_scanner_view);
		header1 = parent.findViewById(R.id.et_header_1);
		header2 = parent.findViewById(R.id.et_header_2);
		userCoin = parent.findViewById(R.id.et_user_coin);
		userCoinLabel = parent.findViewById(R.id.et_user_coin_label);
		adminCoin = parent.findViewById(R.id.et_admin_coin);
		adminCoinLabel = parent.findViewById(R.id.et_admin_coin_label);
		
		btnCancel.setTypeface(flatFont, Typeface.BOLD);
		scannerUseFlash.setTypeface(flatFont);
		header1.setTypeface(flatFont, Typeface.BOLD);
		header2.setTypeface(flatFont, Typeface.BOLD);
		userCoin.setTypeface(flatFont, Typeface.BOLD);
		userCoinLabel.setTypeface(flatFont);
		adminCoin.setTypeface(flatFont, Typeface.BOLD);
		adminCoinLabel.setTypeface(flatFont);
		
		btnCancel.setOnClickListener(this);
		scannerUseFlash.setOnCheckedChangeListener(this);
		
		scanner.setResultHandler(this);
		scanner.setAspectTolerance(0.2f);
		scanner.startCamera();
		
		if(Double.parseDouble(userData.userCent.replaceAll("[¢|₱]", "")) <= 0)
		{
			header2.setVisibility(View.GONE);
			scanner.setVisibility(View.GONE);
		}
		
		userCoin.setText(userData.userCent);
		adminCoin.setText("0.0¢");
		
		dlg.setView(parent);
	}
	
	@Override
	public void onClick(View v)
	{
		scanner.stopCamera();
		dlg.dismiss();
		dlg = null;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton cb, boolean checked)
	{
		scannerUseFlash.setText((checked) ? R.string.useflash_on : R.string.useflash_off);
		scanner.setFlash(checked);
	}
	
	@Override
	public void handleResult(Result result)
	{
		String itemId = result.getContents();
		
		final SweetAlertDialog swal = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
		swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
		swal.setTitleText("Verifying item...");
		swal.setCancelable(false);
		swal.setCanceledOnTouchOutside(false);
		swal.show();
		
		HashMap<String, Object> data = new HashMap<>();
		data.put("action_getItemData", "");
		data.put("item_id", itemId);
		
		VolleyRequest.newRequest(ctx, Constants.BASE_URL)
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
							
							Double itemWorth = Double.parseDouble(model.itemWorth.replaceAll("[¢|₱]", ""));
							Double admnCoins = Double.parseDouble(adminCoin.getText().toString().replaceAll("[¢|₱]", ""));
							Double userCoins = Double.parseDouble(userCoin.getText().toString().replaceAll("[¢|₱]", ""));
							adminCoin.setText(((admnCoins += itemWorth) >= 1) ? "₱" + admnCoins : admnCoins + "¢");
							
							if(admnCoins.doubleValue() == userCoins.doubleValue())
							{
								final SweetAlertDialog swp = new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE);
								swp.setCancelable(false);
								swp.setCanceledOnTouchOutside(false);
								swp.setTitleText("Verify Success");
								swp.setContentText(String.format("%s\'s coins is legit and has not been altered!\nWould you like to reset %s\'s coins now?", userData.userName, userData.userName));
								swp.setConfirmText("Yes, reset");
								swp.setCancelText("No, not yet");
								swp.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
									@Override
									public void onClick(SweetAlertDialog swp)
									{
										swp.dismissWithAnimation();
										dlg.dismiss();
										
										final SweetAlertDialog swal = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
										swal.getProgressHelper().setBarColor(android.graphics.Color.parseColor("#00d170"));
										swal.setTitleText("Commencing reset...");
										swal.setCancelable(false);
										swal.setCanceledOnTouchOutside(false);
										swal.show();

										HashMap<String, Object> data = new HashMap<>();
										data.put("action_updateUserData", "");
										data.put("old_username", userData.userName);
										data.put("user_name", userData.userName);
										data.put("user_pass", userData.userPass);
										data.put("user_cent", "0.0¢");
										data.put("user_rank", userData.userRank);
										
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
														
														final SweetAlertDialog swp = new SweetAlertDialog(ctx, ((hasError) ? SweetAlertDialog.ERROR_TYPE : SweetAlertDialog.SUCCESS_TYPE));
														swp.setCancelable(false);
														swp.setCanceledOnTouchOutside(false);
														swp.setTitleText((hasError) ? "Reset Failed" : "Reset Success");
														swp.setContentText((hasError) ? String.format("Failed to reset %s\'s coins!", userData.userName) : String.format("Successfully resetted %s\'s coins!", userData.userName));
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
								});
								swp.show();
							}
							
							return;
						}
						
						final SweetAlertDialog swp = new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE);
						swp.setCancelable(false);
						swp.setCanceledOnTouchOutside(false);
						swp.setTitleText("Verifying Failed");
						swp.setContentText(message);
						swp.setConfirmText("Okay");
						swp.show();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			})
			.setEndPoint("item/getItemData.php")
			.sendRequest(data);
			
		Handler reset = new Handler();
		reset.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				scanner.resumeCameraPreview(AdminVerifyCoinDialog.this);
			}
		}, Constants.SCANNER_RELOAD_DELAY);
	}
}
