package com.stollmann.tiov2sample;

import com.stollmann.shared.STTrace;
import com.stollmann.shared.STUtil;
import com.stollmann.terminalIO.TIOManager;
import com.stollmann.terminalIO.TIOPeripheral;
import com.stollmann.terminalIO.TIOPeripheralCallback;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class PeripheralActivity extends Activity implements TIOPeripheralCallback {

	private static final int RSSI_INTERVAL = 1670;
	private static final int MAX_RECEIVED_TEXT_LENGTH = 512;

	private TIOPeripheral _peripheral;
	private Handler _rssiHandler = new Handler();
	private Runnable _rssiRunnable;

	private TextView _mainTitleTextView;
	private TextView _subTitleTextView;
	private Button _connectButton;
	private Button _disconnectButton;
	private Button _sendButton;
	private Button _clearButton;
	private TextView _rssiTextView;
	private TextView _remoteCreditsTextView;
	private EditText _dataToSendEditText;
	private TextView _localCreditsTextView;
	private TextView _receivedDataTextView;
	private ScrollView _receivedDataScrollView;

	private Button _uploadButton;
	private String test_result;

	private static final String TAG = PeripheralActivity.class.getSimpleName();

	//******************************************************************************
	// Activity overrides 
	//******************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		STTrace.method("onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peripheral);
		// Show the Up button in the action bar.
		setupActionBar();

		this.connectViews();
		this.connectPeripheral();
		this.updateUIState();
		this.displayVersionNumber();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.peripheral, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		STTrace.method("onResume");

		if (this._peripheral.isConnected()) {
			this.startRSSITimer();
			this._localCreditsTextView.setText(Integer.toString(this._peripheral.getLocalUARTCreditsCount()));
			this._remoteCreditsTextView.setText(Integer.toString(this._peripheral.getRemoteUARTCreditsCount()));
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		STTrace.method("onPause");

		this.stopRSSITimer();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		STTrace.method("onDestroy");

		this._peripheral.setListener(null);

		super.onDestroy();
	}


	//******************************************************************************
	// UI event handlers 
	//******************************************************************************

	public void onConnectButtonPressed(View sender) {
		STTrace.method("onConnectButtonPressed");

		this._peripheral.connect();
		this.updateUIState();
	}

	public void onDisconnectButtonPressed(View sender) {
		STTrace.method("onDisconnectButtonPressed");

		this.stopRSSITimer();
		this._peripheral.disconnect();
	}

	public void onSendButtonPressed(View sender) {
		STTrace.method("onSendButtonPressed");

		try {
			String text = this._dataToSendEditText.getText().toString();
			byte[] data = text.getBytes("CP-1252");
			this._peripheral.writeUARTData(data);
		} catch (Exception ex) {
			STTrace.exception(ex);
		}
	}

	public void onClearButtonPressed(View sender) {
		STTrace.method("onClearButtonPressed");

		if (this._dataToSendEditText.length() > 0) {
			this._dataToSendEditText.setText("");
		} else {
			this._receivedDataTextView.setText("");
		}
	}

	//******************************************************************************
	// TIOPeripheralCallback implementation 
	//******************************************************************************

	@Override
	public void tioPeripheralDidConnect(TIOPeripheral peripheral) {
		STTrace.method("tioPeripheralDidConnect");

		this.updateUIState();
		this.startRSSITimer();

		if (!this._peripheral.shallBeSaved()) {
			// save if connected for the first time
			this._peripheral.setShallBeSaved(true);
			TIOManager.sharedInstance().savePeripherals();
		}
	}

	@Override
	public void tioPeripheralDidFailToConnect(TIOPeripheral peripheral, String errorMessage) {
		STTrace.method("tioPeripheralDidFailToConnect", errorMessage);

		this.updateUIState();

		if (errorMessage.length() > 0) {
			STUtil.showErrorAlert("Failed to connect with error message: " + errorMessage, this);
		}
	}

	@Override
	public void tioPeripheralDidDisconnect(TIOPeripheral peripheral, String errorMessage) {
		STTrace.method("tioPeripheralDidDisconnect", errorMessage);

		this.stopRSSITimer();
		this.updateUIState();

		if (errorMessage.length() > 0) {
			STUtil.showErrorAlert("Disconnected with error message: " + errorMessage, this);
		}
	}

	@Override
	public void tioPeripheralDidReceiveUARTData(TIOPeripheral peripheral, byte[] data) {
		STTrace.method("tioPeripheralDidReceiveUARTData", STTrace.byteArrayToString(data));

		try {
			// transform bytes to string
			String text = new String(data, "CP-1252");

			// append text to view
			this._receivedDataTextView.append(text);

			Log.d("blow results", text);

			// upload text to server
			//upload_test(test_result);

			test_result = text;

			// limit view's text length to MAX_RECEIVED_TEXT_LENGTH
			if (this._receivedDataTextView.length() > PeripheralActivity.MAX_RECEIVED_TEXT_LENGTH + 3) {
				text = this._receivedDataTextView.getText().toString();
				text = "..." + text.substring(text.length() - (PeripheralActivity.MAX_RECEIVED_TEXT_LENGTH + 3));
				this._receivedDataTextView.setText(text);
			}


			// scroll text view to bottom
			this._receivedDataScrollView.post(new Runnable() {
				@Override
				public void run() {
					PeripheralActivity.this._receivedDataScrollView.scrollTo(0, PeripheralActivity.this._receivedDataTextView.getBottom());
				}
			});
		} catch (Exception ex) {
			STTrace.exception(ex);
		}
	}

	@Override
	public void tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral peripheral, int bytesWritten) {
		STTrace.method("tioPeripheralDidWriteNumberOfUARTBytes", Integer.toString(bytesWritten));

	}

	@Override
	public void tioPeripheralUARTWriteBufferEmpty(TIOPeripheral peripheral) {
		STTrace.method("tioPeripheralUARTWriteBufferEmpty");

	}

	@Override
	public void tioPeripheralDidUpdateAdvertisement(TIOPeripheral peripheral) {
		STTrace.method("tioPeripheralDidUpdateAdvertisement");

		// display peripheral properties
		Log.d(TAG, "wazzzzuppp");
		this._mainTitleTextView.setText(this._peripheral.getName() + "  " + this._peripheral.getAddress());
		this._subTitleTextView.setText(this._peripheral.getAdvertisementDisplayString());
	}

	@Override
	public void tioPeripheralDidUpdateRSSI(TIOPeripheral peripheral, int rssi) {
		STTrace.method("tioPeripheralDidUpdateRSSI", Integer.toString(rssi));

		this._rssiTextView.setText(Integer.toString(rssi));
	}

	@Override
	public void tioPeripheralDidUpdateLocalUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
		STTrace.method("tioPeripheralDidUpdateLocalUARTCreditsCount", Integer.toString(creditsCount));

		this._localCreditsTextView.setText(Integer.toString(creditsCount));
	}

	@Override
	public void tioPeripheralDidUpdateRemoteUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
		STTrace.method("tioPeripheralDidUpdateRemoteUARTCreditsCount", Integer.toString(creditsCount));

		this._remoteCreditsTextView.setText(Integer.toString(creditsCount));
	}


	//******************************************************************************
	// Internal methods 
	//******************************************************************************

	private void connectViews() {
		STTrace.method("connectViews");

		this._mainTitleTextView = (TextView) this.findViewById(R.id.mainTitle);
		this._subTitleTextView = (TextView) this.findViewById(R.id.subTitle);
		this._connectButton = (Button) this.findViewById(R.id.connectButton);
		this._disconnectButton = (Button) this.findViewById(R.id.disconnectButton);
		this._sendButton = (Button) this.findViewById(R.id.sendButton);
		this._clearButton = (Button) this.findViewById(R.id.clearButton);
		this._rssiTextView = (TextView) this.findViewById(R.id.rssiTextView);
		this._remoteCreditsTextView = (TextView) this.findViewById(R.id.remoteCreditsTextView);
		this._dataToSendEditText = (EditText) this.findViewById(R.id.dataToSendEditText);
		this._localCreditsTextView = (TextView) this.findViewById(R.id.localCreditsTextView);
		this._receivedDataTextView = (TextView) this.findViewById(R.id.receivedDataTextView);
		this._receivedDataScrollView = (ScrollView) this.findViewById(R.id.receivedDataScrollView);

		this._uploadButton = (Button) this.findViewById(R.id.upload_button);

		this._rssiTextView.setText("0");
		this._remoteCreditsTextView.setText("0");
		this._localCreditsTextView.setText("0");

		this._dataToSendEditText.setText("");
		this._sendButton.setEnabled(false);
		this._clearButton.setEnabled(false);
		this._dataToSendEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				PeripheralActivity.this._sendButton.setEnabled(PeripheralActivity.this._dataToSendEditText.length() > 0);
				PeripheralActivity.this._clearButton.setEnabled(PeripheralActivity.this._dataToSendEditText.length() > 0 || PeripheralActivity.this._receivedDataTextView.length() > 0);
			}
		});

		this._receivedDataTextView.setText("");
		this._receivedDataTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				PeripheralActivity.this._clearButton.setEnabled(PeripheralActivity.this._dataToSendEditText.length() > 0 && PeripheralActivity.this._receivedDataTextView.length() > 0);
			}
		});
	}

	private void connectPeripheral() {
		STTrace.method("connectPeripheral");

		// extract peripheral id (address) from intent
		Intent intent = this.getIntent();
		String address = intent.getStringExtra(TIOV2Sample.PERIPHERAL_ID_NAME);

		// retrieve peripheral instance from TIOManager
		this._peripheral = TIOManager.sharedInstance().findPeripheralByAddress(address);

		// register callback
		this._peripheral.setListener(this);

		// display peripheral properties
		this._mainTitleTextView.setText(this._peripheral.getName() + "  " + this._peripheral.getAddress());
		this._subTitleTextView.setText(this._peripheral.getAdvertisementDisplayString());
	}

	private void startRSSITimer() {
		STTrace.method("startRSSITimer");

		if (this._rssiRunnable == null) {
			this._rssiRunnable = new Runnable() {
				@Override
				public void run() {
					PeripheralActivity.this._peripheral.readRSSI();
					PeripheralActivity.this._rssiHandler.postDelayed(PeripheralActivity.this._rssiRunnable, PeripheralActivity.RSSI_INTERVAL);
				}
			};
		}
		this._peripheral.readRSSI();
		this._rssiHandler.postDelayed(this._rssiRunnable, PeripheralActivity.RSSI_INTERVAL);
	}

	private void stopRSSITimer() {
		STTrace.method("stopRSSITimer");

		this._rssiHandler.removeCallbacks(this._rssiRunnable);
	}

	private void updateUIState() {
		STTrace.method("updateUIState");

		boolean isConnected = this._peripheral.isConnected();
		boolean isConnecting = this._peripheral.isConnecting();
		int visibility = isConnected ? View.VISIBLE : View.INVISIBLE;

		this._connectButton.setEnabled(!isConnected && !isConnecting);
		this._disconnectButton.setEnabled(isConnected);
		this._dataToSendEditText.setVisibility(visibility);
		this._receivedDataTextView.setVisibility(visibility);

		this._sendButton.setVisibility(visibility);
		this._clearButton.setVisibility(visibility);
	}

	private void displayVersionNumber() {
		PackageInfo packageInfo;
		String version = "";
		try {
			packageInfo = this.getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (Exception ex) {
			STTrace.exception(ex);
		}
		this.setTitle(this.getTitle() + " " + version);
	}

	public void upload_test(View view) {
		// Tag used to cancel the request
		String tag_string_req = "req_response";
		StringRequest strReq = new StringRequest(Request.Method.POST, UrlConfig.URL_VITOL_UPLOAD, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Instance Response: " + response.toString());

				try {
					JSONObject jObj = new JSONObject(response);


				} catch (JSONException e) {
					// JSON error
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Upload Error: " + error.getMessage());
				Toast.makeText(getApplicationContext(),
						error.getMessage(), Toast.LENGTH_LONG).show();
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to response url
				Map<String, String> params = new HashMap<String, String>();
				params.put("patient_id", "100101");
				params.put("test_result", test_result);


				return params;
			}

		};

		// Adding request to request queue
		TIOV2Sample.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


}
