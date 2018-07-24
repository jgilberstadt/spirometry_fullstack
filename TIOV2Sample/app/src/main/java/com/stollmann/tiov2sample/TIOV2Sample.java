package com.stollmann.tiov2sample;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.stollmann.shared.STTrace;
import com.stollmann.terminalIO.TIOManager;

import android.app.Application;
import android.text.TextUtils;

public class TIOV2Sample extends Application {
	public static final String TAG = TIOV2Sample.class.getSimpleName();

	public static final String PERIPHERAL_ID_NAME = "com.stollmann.tiov2sample.peripheralId";

	private RequestQueue mRequestQueue;

	private static TIOV2Sample mInstance;
	
	@Override
	public void onCreate() {
	
		STTrace.setTag("TIOV2Sample");
		STTrace.method("onCreate");
		
		TIOManager.initialize(this.getApplicationContext());
		mInstance = this;
	}

	public static synchronized TIOV2Sample getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

}
