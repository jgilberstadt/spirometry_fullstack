package com.stollmann.terminalIO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.stollmann.shared.AndroidBLEScanRecord;
import com.stollmann.shared.STTrace;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

/**
 *  The TIOManager is the fundamental class to provide TerminalIO functionality towards the application. It exists as one instance only per application (singleton),
 *  encapsulating the TerminalIO Bluetooth Low Energy functionality and supplying its listener {@link TIOManagerCallback} with TerminalIO relevant events.
 */ 
public class TIOManager {

	private static final String KNOWN_PERIPHERAL_ADDRESSES_FILE_NAME = "TIOKnownPeripheralAddresses"; 
	
	private static TIOManager _instance = null;
	
	private Context _applicationContext;
	private BluetoothAdapter _bluetoothAdapter;
	private ScanListener _scanListener;
	private TIOManagerCallback _listener;
	private boolean _listenerIsActivity;
	private ArrayList<TIOPeripheral> _peripherals = new ArrayList<TIOPeripheral>();

	
	//******************************************************************************
	// Initialization 
	//******************************************************************************

	/**
	 *  Create the singleton TIOManager instance - including the tying up to the Android BluetoothAdapter - and loads the list of know peripherals.
	 *  It is therefore recommended to call this message at an early state of application startup, e.g. within the appliction's onCreate() override.
	 *  
	 *  No TIOManager operations shall be performed before this method has been called.
	 *  
	 *  @return The singleton TIOManager instance.
	 */ 
	public static void initialize(Context applicationContext) {
		STTrace.method("initialize");
		
		if (TIOManager._instance != null) {
			STTrace.error("already initialized");
			return;
		}
		
		TIOManager._instance = new TIOManager();	
		BluetoothManager bluetoothManager = (BluetoothManager) applicationContext.getSystemService(Context.BLUETOOTH_SERVICE);
		TIOManager._instance._bluetoothAdapter = bluetoothManager.getAdapter();
		TIOManager._instance._applicationContext = applicationContext;
		
		TIOManager._instance.loadPeripherals();
	}
	
	private TIOManager() {
		this._scanListener = new ScanListener();
	}	

	
	//******************************************************************************
	// Properties
	//******************************************************************************
	
	/**
	 *  Sets the listener for TerminalIO scan events.
	 *
	 *  If <code>listener</code> extends Activity, TIOManager invokes all TIOManagerCallback methods on the UI thread.
	 *  @param listener	The listener to receive TIOManagerCallback events.
	 */ 	
	public void setListener(TIOManagerCallback listener) {
		this._listener = listener;
		this._listenerIsActivity = this._listener instanceof Activity;
	}
	
	/**
	 * Gets the current Bluetooth enabled state.
	 *  @return <code>true</code> if Bluetooth is enabled on the Smart Phone, <code>false</code> otherwise.
	 */ 
	public boolean isBluetoothEnabled() {
		return this._bluetoothAdapter.isEnabled();
	}
	
	/**
	 * Gets the application context passed to TIOManager in the call to {@link initialize(Context) initialize()}.
	 *  @return The application context passed to TIOManager in the call to {@link initialize(Context) initialize()}.
	 */ 
	public Context getApplicationContext() {
		return this._applicationContext;
	}
	
	/**
	 * Gets all TIOPeripheral instances representing the currently known TerminalIO peripherals.
	 *  @return An array of TIOPeripheral instances representing all currently known TerminalIO peripherals.
	 */ 
	public TIOPeripheral[] getPeripherals() {
		return this._peripherals.toArray(new TIOPeripheral[this._peripherals.size()]);
	}

	
	//******************************************************************************
	// Public methods
	//******************************************************************************

	/**
	 *  Returns the singleton TIOManager instance.
	 *  @return The singleton TIOManager instance.
	 */ 
	public static final TIOManager sharedInstance() {
		return TIOManager._instance;
	}
	
	/**
	 *  Starts a Bluetooth Low Energy scan procedure.
	 *  
	 *  Discovered TerminalIO peripherals will be reported via the {@link TIOManagerCallback#tioManagerDidDiscoverPeripheral(TIOPeripheral) TIOManager.Callback#tioManagerDidDiscoverPeripheral())} method.
	 *
	 *  Call stopScan to stop the scan procedure and save battery power.
	 */ 
	public void startScan() {
		STTrace.method("startScan");
		
		// We would like to filter scan results for the TerminalIO service UUID, but Android 4.4 does not support the filtering of scan records containing 128-bit UUIDs;
		// so we have to consume all scan results and implement our own filtering mechanism.
//		this._bluetoothAdapter.startLeScan(new UUID[] { TIO.SERVICE_UUID }, this._scanListener);
		this._bluetoothAdapter.startLeScan(this._scanListener);
	}
	
	/**
	 *  Stops a currently running scan procedure.
	 */ 	
	public void stopScan() {
		STTrace.method("stopScan");
		this._bluetoothAdapter.stopLeScan(this._scanListener);
	}
	
	/**
	 *  Loads TIOPeripherals previously saved with {@link #savePeripherals() savePeripherals()}.
	 *
	 *  Populates the TIOManager's peripherals list with TIOPeripheral instances created from a serialized list of Bluetooth addresses.
	 *  This method is called implicitly during initialization via {@link #initialize(Context) initialize()}.
	 *
	 *  The reconstructed TIOPeripheral instances do not contain any advertisement information.
	 *  If Bluetooth has been switched off, also the name information will be lost.
	 *  If any advertisement information (e.g. local name, TIOPeripheralOperationMode) or name information is required,
	 *  call {@link #startScan() startScan()} in order to refresh the advertisement information of peripherals within radio range.
	 */ 
	public void loadPeripherals()	{
		STTrace.method("loadPeripherals");

		String[] addresses = null;
		
		try {
			FileInputStream fileStream = this._applicationContext.openFileInput(TIOManager.KNOWN_PERIPHERAL_ADDRESSES_FILE_NAME);
			ObjectInputStream objectStream = new ObjectInputStream(fileStream);
			addresses = (String[]) objectStream.readObject();
			objectStream.close();
			fileStream.close();
		} catch (FileNotFoundException ex) {
			STTrace.error("Saved peripheral addresses file not found; application is probably installed for the first time...");
			return;
		} catch (Exception ex) {
			STTrace.exception(ex);
			return;
		}

		for (String address: addresses) {
			// check for duplicates
			if (this.findPeripheralByAddress(address) != null)
				continue;

			// create TIOPeripheral instance from Bluetooth address
			BluetoothDevice device = this._bluetoothAdapter.getRemoteDevice(address);
			TIOPeripheral peripheral = TIOPeripheral.createFromBluetoothDevice(device);
			STTrace.line("loaded peripheral " + peripheral.toString() + " with name " + device.getName());
			
			// add peripheral to list
			this._peripherals.add(peripheral);
		}
	} 
	
	/**
	 *  Serializes a list of known peripheral Bluetooth addresses to a persistent file in the application directory.
	 */ 
	public void savePeripherals()
	{
		STTrace.method("savePeripherals");

		// collect list of known Bluetooth addresses
		ArrayList<String> addressList = new ArrayList<String>();
		for (TIOPeripheral peripheral: this._peripherals)
		{
			if (peripheral.shallBeSaved())
			{
				addressList.add(peripheral.getAddress());
			}
		}
		
		// write address string array to file
		String[] addresses = addressList.toArray(new String[addressList.size()]);
		try {
			FileOutputStream fileStream = this._applicationContext.openFileOutput(TIOManager.KNOWN_PERIPHERAL_ADDRESSES_FILE_NAME, Context.MODE_PRIVATE);
			ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(addresses);
			objectStream.close();
			fileStream.close();
		} catch (Exception ex) {
			STTrace.exception(ex);
		}
	}

	/**
	 * Retrieves the TIOPeripheral instance with the specified address.
	 * 
	 * Applications may use this method to retrieve an object instance in cases where the Bluetooth address string has been transmitted within a message, e.g. a broadcast or an intent.
	 * 
	 * @param address A Bluetooth address string to retrieve a TIOPeripheral instance for; format is [00:11:22:AA:BB:CC].
	 * @return The requested TIOPeripheral instance, or null, if no matching instance could be found.
	 */
	public TIOPeripheral findPeripheralByAddress(String address) {
		for (TIOPeripheral peripheral: this._peripherals) {
			if (peripheral.getAddress().equals(address))
				return peripheral;
		}
		return null;
	}
	
	/**
	 *  Removes the specified peripheral from the TIOManager's peripheral list.
	 */
	public void removePeripheral(TIOPeripheral peripheral) {
		STTrace.method("removePeripheral", peripheral.toString());

		peripheral.disconnect();
		this._peripherals.remove(peripheral);
		this.savePeripherals();
	}
	
	/**
	 *  Removes all peripherals from the TIOManager's peripheral list.
	 */ 
	public void removeAllPeripherals() {
		STTrace.method("removeAllPeripherals");
		
		for (TIOPeripheral peripheral: this._peripherals) {
			peripheral.disconnect();
		}
		this._peripherals.clear();
		this.savePeripherals();
	}
	
	
	//******************************************************************************
	// Internal methods
	//******************************************************************************
	
	
	
	//******************************************************************************
	// LeScanCallback implementation 	
	//******************************************************************************
		
	private class ScanListener implements LeScanCallback {
		
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			STTrace.method("onLeScan", device.getAddress());
			
			// parse scan record for matching service UUID; this is the replacement for the filtering function missing in Android 4.4
			AndroidBLEScanRecord record = AndroidBLEScanRecord.createFromRecordData(scanRecord);
			if (!record.containsServiceUuid(TIO.SERVICE_UUID)) {
				STTrace.error("no TIO device");
				return;
			}
			STTrace.line("found TIO preripheral address = " + device.getAddress() + "; name = " + device.getName() + "; rssi = " + rssi);

			// extract TerminalIO advertisement info
			TIOAdvertisement advertisement = TIOAdvertisement.createFromScanRecord(record.getLocalName(), record.getManufacturerSpecificData());
			if (advertisement == null) {
				STTrace.error("invalid advertisement");
				return;
			}
			STTrace.line("read advertisement = " + advertisement.toString());
			
			// check for known peripheral
			TIOPeripheral knownPeripheral = TIOManager.this.findPeripheralByAddress(device.getAddress());
			if (knownPeripheral != null) {
				STTrace.line("device already known");
				
				// check for possible advertisement update
				if (knownPeripheral.updateWithAdvertisement(advertisement)) {
					STTrace.line("advertisement has changed");
					TIOManager.this.raiseDidUpdatePeripheral(knownPeripheral);
				}
				return;
			}
			
			// create new TIOPeripheral instance
			final TIOPeripheral peripheral = TIOPeripheral.createFromScanResult(device, advertisement);
			// add peripheral to list
			TIOManager.this._peripherals.add(peripheral);
			// notify application
			TIOManager.this.raiseDidDiscoverPeripheral(peripheral);
			// save updated peripheral list
			TIOManager.this.savePeripherals();
		}
	}
	
	
	//******************************************************************************
	// Callback events  
	//******************************************************************************
	
	private void raiseDidDiscoverPeripheral(final TIOPeripheral peripheral) {
		//Log.d("hyunrae", peripheral.getName());
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOManager.this._listener.tioManagerDidDiscoverPeripheral(peripheral);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioManagerDidDiscoverPeripheral(peripheral);
		}
	}

	private void raiseDidUpdatePeripheral(final TIOPeripheral peripheral) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOManager.this._listener.tioManagerDidUpdatePeripheral(peripheral);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioManagerDidUpdatePeripheral(peripheral);
		}
	}
}
