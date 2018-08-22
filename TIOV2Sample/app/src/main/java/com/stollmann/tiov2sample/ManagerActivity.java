package com.stollmann.tiov2sample;

import com.stollmann.shared.STSwipeTapDetector;
import com.stollmann.shared.STTrace;
import com.stollmann.terminalIO.TIOManager;
import com.stollmann.terminalIO.TIOManagerCallback;
import com.stollmann.terminalIO.TIOPeripheral;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import static com.stollmann.tiov2sample.TIOV2Sample.TAG;

public class ManagerActivity extends Activity implements TIOManagerCallback {

	private static final int ENABLE_BT_REQUEST_ID = 1;
	private static final int SCAN_INTERVAL = 8000;

	private Button _scanButton;
	private Button _clearAllButton;
	private ProgressBar _scanIndicator;
	private ListView _peripheralsListView;
	private ArrayAdapter<TIOPeripheral> _peripheralsAdapter;
	private Handler _scanHandler = new Handler();
	
	
	//******************************************************************************
	// Activity overrides 
	//******************************************************************************
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		STTrace.method("onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manager);

		this.connectViews();
		
		// register activity as TIOManagerCallback in order to receive scan events
		TIOManager.sharedInstance().setListener(this);
		
		// displays a dialog requesting user permission to enable Bluetooth.
		if (!TIOManager.sharedInstance().isBluetoothEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    this.startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
		}

		// initialize peripherals list view
		this.initializePeripheralsListView();
		this.updatePeripheralsListView();

		// initialize clearAllButton
		this.updateClearAllButton();
		
        // display version number
		this.displayVersionNumber();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manager, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		STTrace.method("onActivityResult", Integer.toString(resultCode));
	
		if (requestCode == ENABLE_BT_REQUEST_ID) {
			if(resultCode == Activity.RESULT_CANCELED) {
				this._scanButton.setEnabled(false);
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	//******************************************************************************
	// UI event handlers 
	//******************************************************************************
	
	public void onScanButtonPressed(View sender) {
		STTrace.method("onScanButtonPressed");

		this.startTimedScan();
	}

	public void onClearAllButtonPressed(View sender) {
		STTrace.method("onClearAllButtonPressed");

		TIOManager.sharedInstance().removeAllPeripherals();
		this.updatePeripheralsListView();
		this.updateClearAllButton();
	}
	
	private void onRemoveButtonPressed(TIOPeripheral peripheral) {
		STTrace.method("onRemoveButtonPressed", peripheral.toString());

		TIOManager.sharedInstance().removePeripheral(peripheral);
		this.updatePeripheralsListView();		
		this.updateClearAllButton();
	}
	
	private void onPeripheralCellPressed(TIOPeripheral peripheral) {
		STTrace.method("onPeripheralCellPressed", peripheral.toString());

    	Intent intent = new Intent(ManagerActivity.this, PeripheralActivity.class);
		intent.putExtra(TIOV2Sample.PERIPHERAL_ID_NAME, peripheral.getAddress());
		ManagerActivity.this.startActivity(intent);
	}

	
	//******************************************************************************
	// TIOManagerCallback implementation 
	//******************************************************************************
	
	@Override
	public void tioManagerDidDiscoverPeripheral(TIOPeripheral peripheral) {
		STTrace.method("tioManagerDidDiscoverPeripheral", peripheral.toString());
		Log.d(TAG, "yo a");

		// overrule default behaviour: peripheral shall be saved only after having been connected
		peripheral.setShallBeSaved(false);
		TIOManager.sharedInstance().savePeripherals();
		
		this.updatePeripheralsListView();
	}

	@Override
	public void tioManagerDidUpdatePeripheral(TIOPeripheral peripheral) {
		STTrace.method("tioManagerDidUpdatePeripheral", peripheral.toString());
		Log.d(TAG, "yo b");

		this.updatePeripheralsListView();
	}

	
	//******************************************************************************
	// Internal methods 
	//******************************************************************************
	
	private void connectViews() {
		STTrace.method("connectViews");
		
		this._scanButton = (Button) this.findViewById(R.id.scanButton);
		this._clearAllButton = (Button) this.findViewById(R.id.clearAllButton);
		this._scanIndicator = (ProgressBar) this.findViewById(R.id.scanIndicator);
		this._peripheralsListView = (ListView) this.findViewById(R.id.peripheralsListView);
	}
	
	private void initializePeripheralsListView() {
		STTrace.method("initializePeripheralsListView");
		
		// create data adapter for peripherals list view
		this._peripheralsAdapter = new ArrayAdapter<TIOPeripheral>(this, 0) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return ManagerActivity.this.createPeripheralCell(position);
			}
			@Override
			public int getCount() {
				return TIOManager.sharedInstance().getPeripherals().length;
			}
		};
		this._peripheralsListView.setAdapter(this._peripheralsAdapter);
	}

	private View createPeripheralCell(int position) {
		
	    final TIOPeripheral peripheral = TIOManager.sharedInstance().getPeripherals()[position];
		
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View peripheralCell = inflater.inflate(R.layout.peripheral_cell, this._peripheralsListView, false);
	    
	    TextView mainTitle = (TextView) peripheralCell.findViewById(R.id.mainTitle);
		mainTitle.setText(peripheral.getName() + "  " + peripheral.getAddress());
	    
		TextView subTitle = (TextView) peripheralCell.findViewById(R.id.subTitle);
		subTitle.setText(peripheral.getAdvertisementDisplayString());

		Button removeButton = (Button) peripheralCell.findViewById(R.id.removeButton);
		removeButton.setVisibility(View.INVISIBLE);
		removeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ManagerActivity.this.onRemoveButtonPressed(peripheral);		
			}
		});
		
		peripheralCell.setOnTouchListener(this.createPeripheralCellGestureDetector(peripheral, removeButton));

		return peripheralCell;
	}
	
	private STSwipeTapDetector createPeripheralCellGestureDetector(final TIOPeripheral peripheral, final Button removeButton) {
		STSwipeTapDetector detector = new STSwipeTapDetector(this) {
			
			private boolean _removeMode;
			@Override
			public boolean onLeftSwipe() {
				if (this._removeMode)
	        		return true; // handled

				this._removeMode = true;
				TranslateAnimation animation = new TranslateAnimation(removeButton.getWidth(), 0, 0, 0);
				animation.setDuration(200);
				animation.setAnimationListener(new Animation.AnimationListener() {
	                @Override
	                public void onAnimationStart(Animation animation) {
						removeButton.setVisibility(View.VISIBLE);
						removeButton.setEnabled(false);
	                }
	                @Override
	                public void onAnimationEnd(Animation animation) {
						removeButton.setEnabled(true);
	                }
					@Override
					public void onAnimationRepeat(Animation animation) { }
	            });						
				removeButton.startAnimation(animation);
				
				return true; // handled
			}
			@Override
			public boolean onRightSwipe() {
				if (!this._removeMode)
	        		return true; // handled

				TranslateAnimation animation = new TranslateAnimation(0, removeButton.getWidth(), 0, 0);
				animation.setDuration(200);
				animation.setAnimationListener(new Animation.AnimationListener() {
	                @Override
	                public void onAnimationStart(Animation animation) {
						removeButton.setEnabled(false);
	                }
	                @Override
	                public void onAnimationEnd(Animation animation) {
						removeButton.setVisibility(View.INVISIBLE);
	                }
					@Override
					public void onAnimationRepeat(Animation animation) { }
	            });						
				removeButton.startAnimation(animation);
				this._removeMode = false;
				
				return true;
			}
	        @Override
	        public boolean onTap() {
	        	if (this._removeMode)
	        		return true; // handled

        		ManagerActivity.this.onPeripheralCellPressed(peripheral);
				
				return true; // handled
	        }
		};
		
		return detector;
	}

	private void startTimedScan() {
		STTrace.method("startTimedScan");
		
		this._scanButton.setEnabled(false);
		this._clearAllButton.setEnabled(false);
		this._scanIndicator.setVisibility(View.VISIBLE);
		
		this._scanHandler.postDelayed(new Runnable() {
            		@Override
					public void run() {
            	    	TIOManager.sharedInstance().stopScan();
            			
            			ManagerActivity.this._scanIndicator.setVisibility(View.INVISIBLE);
            			ManagerActivity.this._scanButton.setEnabled(true);
            			ManagerActivity.this.updateClearAllButton();
            		}
            	}, ManagerActivity.SCAN_INTERVAL);

    	TIOManager.sharedInstance().startScan();
	}

	private void updatePeripheralsListView() {
		// update adapter with currently known peripherals
		this._peripheralsAdapter.notifyDataSetChanged();
	}
	
	private void updateClearAllButton() {
		this._clearAllButton.setEnabled(TIOManager.sharedInstance().getPeripherals().length > 0);
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
}
