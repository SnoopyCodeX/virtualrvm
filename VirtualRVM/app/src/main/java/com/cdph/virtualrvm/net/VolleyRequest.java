package com.cdph.virtualrvm.net;

import android.content.Context;
import com.cdph.virtualrvm.util.Constants;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class VolleyRequest 
{
	private OnVolleyResponseReceivedListener listener;
	private Context context;
    private String endPoint, baseUrl, response;
	
	private VolleyRequest()
	{}
	
	private VolleyRequest(Context context, String baseUrl)
	{
		this.baseUrl = baseUrl;
		this.context = context;
	}
	
	public static synchronized VolleyRequest newRequest(Context context, String baseUrl)
	{
		return (new VolleyRequest(context, baseUrl));
	}
	
	public VolleyRequest setEndPoint(String endPoint)
	{
		this.endPoint = endPoint;
		return this;
	}
	
	public VolleyRequest addOnVolleyResponseReceivedListener(OnVolleyResponseReceivedListener listener)
	{
		this.listener = listener;
		return this;
	}
	
	public void sendRequest(final HashMap<String, Object> data)
	{
		Response.Listener<String> _response_ = new Response.Listener<String>() {
			@Override
			public void onResponse(String response)
			{
				if(listener != null)
					listener.onVolleyResponseReceived(response);
			}
		};
		
		Response.ErrorListener _error_ = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if(listener != null)
					listener.onVolleyResponseReceived(String.format("[{\"message\":\"%s\", \"hasError\":true, \"data\":[]}]", error.getMessage()));
			}
		};
		
		StringRequest request = new StringRequest(
			Request.Method.POST, 
			baseUrl + endPoint, 
			_response_, 
			_error_
		) {
			@Override
			protected Map<String, String> getParams()
			{
				Map<String, String> params = new HashMap<>();
				
				for(Map.Entry entry : data.entrySet())
					params.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
				
				return params;
			}
		};
		
		RequestQueue queue = Volley.newRequestQueue(context);
		queue.add(request);
	}
	
	public interface OnVolleyResponseReceivedListener
	{
		public void onVolleyResponseReceived(String response);
	}
}
