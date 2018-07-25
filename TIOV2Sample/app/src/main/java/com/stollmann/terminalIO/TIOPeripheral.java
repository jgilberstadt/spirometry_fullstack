package com.stollmann.terminalIO;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.stollmann.shared.STTrace;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 *  TIOPeripheral instances represent remote devices that have been identified as TerminalIO servers during a TIOManager scan procedure.
 *  The application retrieves TIOPeripheral instances by calling the TIOManager {@link TIOManager#getPeripherals() TIOManager.getPeripherals} property or via invocations of one of the peripheral relevant {@link TIOManagerCallback} methods.
 *  The application shall not create any TIOPeripheral instances of its own.
 */ 
public class TIOPeripheral {

	protected static final int DEFAULT_MIN_UART_CREDITS_COUNT = 32;
	protected static final int WRITE_CALLBACK_TIMEOUT = 1000;
	protected static final int RSSI_DEFERRAL_INTERVAL = 1200;

	protected static final UUID CCC_BITS_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34fB");
	
	protected BluetoothDevice _device;
	protected BluetoothGatt _gatt;
	protected TIOAdvertisement _advertisement;
	protected boolean _shallBeSaved = true;
	
	protected BluetoothGattService _tioService;
	protected BluetoothGattCharacteristic _uartTxCharacteristic;
	protected BluetoothGattCharacteristic _uartTxCreditsCharacteristic;
	protected BluetoothGattCharacteristic _uartRxCharacteristic;
	protected BluetoothGattCharacteristic _uartRxCreditsCharacteristic;
	
	protected TIOPeripheralCallback _listener;
	protected boolean _listenerIsActivity;
	
	protected boolean _isConnecting;
	protected boolean _isConnected;
	protected boolean _didSubscribeUARTTx;
	protected boolean _didSubscribeUARTTxCredits;
	protected boolean _didGrantInitialUARTRxCredits;
	protected BroadcastReceiver _bondingReceiver;
	
	protected boolean _isWriting;
	protected final Object _writeLock = new Object();
	protected byte[] _uartDataToBeWritten = new byte[0];
	protected int _lastNumberOfBytesWritten;
	protected int _localUARTCreditsCount;  // RX credits granted to the peripheral
	protected int _pendingLocalUARTCreditsCount;
	protected int _maxLocalUARTCreditsCount = TIO.MAX_UART_CREDITS_COUNT;
	protected int _minLocalUARTCreditsCount = TIOPeripheral.DEFAULT_MIN_UART_CREDITS_COUNT;
	protected int _remoteUARTCreditsCount;  // TX credits granted by the peripheral
	
	protected int _lastRSSI;
	protected boolean _rssiDeferralPending;
	protected Timer _rssiDeferralTimer = new Timer();

	protected Object _tag;
	
	//******************************************************************************
	// Initialization 
	//******************************************************************************

	private TIOPeripheral(BluetoothDevice device, TIOAdvertisement advertisement) {
		this(device);
		this._advertisement = advertisement;
	}
	
	private TIOPeripheral(BluetoothDevice device) {
		this._device = device;
	}
	
	
	//******************************************************************************
	// Properties
	//******************************************************************************
	
	/**
	 * Gets the preripheral's Bluetooth address.
	 * @return A String instance representing the peripheral's Bluetooth address. 
	 */
	public String getAddress() {
		return this._device.getAddress();
	}
	
	/**
	 * Gets the preripheral's Bluetooth GATT name.
	 * @return A String instance representing the peripheral's Bluetooth GATT name. 
	 */
	public String getName() {
		if (this._device.getName() == null)
			return "";
		return this._device.getName();
	}

	/**
	 *  Gets the TIOAdvertisement instance representing the latest advertisement received from the peripheral within a scan procedure.
	 *
	 *  This property may be null if the TIOPeripheral instance has been reconstructed via a call to {@link TIOManager#loadPeripherals() TIOManager.loadPeripherals()} method
	 *   and no subsequent scan has been performed yet.
	 *   
	 * @return A TIOAdvertisement instance representing the peripheral's TerminalIO advertisement, or null.  
	 */
	public TIOAdvertisement getAdvertisement() {
		return this._advertisement;
	}
	
	/**
	 * Gets a string representation of the current {@link #getAdvertisement() advertisement}, see also {@link TIOAdvertisement#getDisplayString() TIOAdvertisement.getDisplayString()}. 
	 * @return A String representing the current {@link #getAdvertisement() advertisement}, or an empty String if the advertisement is null. 
	 */
	public String getAdvertisementDisplayString() {
		if (this._advertisement == null)
			return "";
		return this._advertisement.getDisplayString();
	}

	/**
	 * Gets the peripheral's TerminalIO connection state.
	 * @return <code>true</code> if the TIOPeripheral instance is TerminalIO connected to the remote device, <code>false</code> otherwise.
	 */ 	
	public boolean isConnected() {
		return this._isConnected;
	}

	/**
	 * Gets the peripheral's connecting state.
	 * @return <code>true</code> if the TIOPeripheral instance is in the process of establishing a TerminalIO connection to the remote device, <code>false</code> otherwise.
	 */ 	
	public boolean isConnecting() {
		return this._isConnecting;
	}
	
	/**
	 * Gets the information whether the peripheral shall be persistently saved to file by the {@link TIOManager#savePeripherals() TIOManager.savePeripherals()} method or not.
	 *  @return <code>true</code> if the peripheral shall be persistently saved to file by the {@link TIOManager#savePeripherals() TIOManager.savePeripherals()} method, <code>false</code> otherwise.
	 */ 
	public boolean shallBeSaved() {
		return this._shallBeSaved;
	}
	
	/**
	 * Determines whether the peripheral shall be persistently saved to file by the {@link TIOManager#savePeripherals() TIOManager.savePeripherals()} method or not.
	 */ 
	public void setShallBeSaved(boolean shallBeSaved) {
		this._shallBeSaved = shallBeSaved;
	}
	
	/**
	 * Gets the current number of UART credits available on the local device.
	 * @return An Integer containing the current number of UART credits available on the local device.
	 */
	public int getLocalUARTCreditsCount() {
		return this._localUARTCreditsCount;
	}
	
	/**
	 * Gets the current number of UART credits available on the remote device.
	 * @return An Integer containing the current number of UART credits available on the remote device.
	 */
	public int getRemoteUARTCreditsCount() {
		return this._remoteUARTCreditsCount;
	}
	
	/**
	 *  Sets the listener object to receive TIOPeripheral events, see {@link TIOPeripheralCallback TIOPeripheralCallback}.
	 *  
	 *  If <code>listener</code> extends Activity, the event methods will be invoked on the UI thread.
	 *
	 *  @param listener TIOPeripheralCallback implementor receiving {@link TIOPeripheralCallback TIOPeripheralCallback} events.
	 */ 	
	public void setListener(TIOPeripheralCallback listener) {
		this._listener = listener;
		this._listenerIsActivity = this._listener instanceof Activity;
	}
	
	/**
	 *  Gets the maximum number of local credits that are granted to the remote device, see {@link #setMaxLocalUARTCreditsCount(int) setMaxLocalUARTCreditsCount()}.
	 *  @return An Integer containing the maximum number of local credits that are granted to the remote device.
	 */ 	
	public int getMaxLocalUARTCreditsCount() {
		return this._maxLocalUARTCreditsCount;
	}

	/**
	 *  Sets the maximum number of local credits that shall be granted to the remote device.
	 *
	 *  A credit is defined as the permission to send one data packet of 1 to 20 bytes.
	 *  Receiving a UART data packet (1 to 20 bytes) from the remote device decrements the local credit counter by 1.
	 *  As soon as the local credit counter reaches minLocalUARTCreditsCount, a number of (maxLocalUARTCreditsCount - minLocalUARTCreditsCount) credits will automatically be granted to the remote device;
	 *  the local credit counter will then have the value of maxLocalUARTCreditsCount.
	 *
	 *  maxLocalUARTCreditsCount shall only be set to values > minLocalUARTCreditsCount and <= 255.
	 *  Default value is 255.
	 *  
	 *  @param maxLocalUARTCreditsCount Maximum number of local credits that shall be granted to the remote device.
	 */ 	
	public void setMaxLocalUARTCreditsCount(int maxLocalUARTCreditsCount) {
		int count = (maxLocalUARTCreditsCount < this._minLocalUARTCreditsCount) ? this._minLocalUARTCreditsCount : ((maxLocalUARTCreditsCount > TIO.MAX_UART_CREDITS_COUNT) ? TIO.MAX_UART_CREDITS_COUNT : maxLocalUARTCreditsCount	);
		this._maxLocalUARTCreditsCount = count;
	}

	/**
	 *  Gets the minimum number of local credits the remote device should have at its disposal, see {@link #setMinLocalUARTCreditsCount(int) setMinLocalUARTCreditsCount()}.
	 *  @return An Integer containing the minimum number of local credits that are granted to the remote device.
	 */ 	
	public int getMinLocalUARTCreditsCount() {
		return this._minLocalUARTCreditsCount;
	}

	/**
	 *  Sets the minimum number of local credits that shall be granted to the remote device.
	 *
	 *  A credit is defined as the permission to send one data packet of 1 to 20 bytes.
	 *  Receiving a UART data packet (1 to 20 bytes) from the remote device decrements the local credit counter by 1.
	 *  As soon as the local credit counter reaches minLocalUARTCreditsCount, a number of (maxLocalUARTCreditsCount - minLocalUARTCreditsCount) credits will automatically be granted to the remote device;
	 *  the local credit counter will then have the value of maxLocalUARTCreditsCount.
	 *
	 *  minLocalUARTCreditsCount shall only be set to values < maxLocalUARTCreditsCount and >= 0.
	 *  Default value is 32.
	 *  
	 *  @param minLocalUARTCreditsCount Minimum number of local credits that shall be granted to the remote device.
	 */ 	
	public void setMinLocalUARTCreditsCount(int minLocalUARTCreditsCount)
	{
		int count = (minLocalUARTCreditsCount > this._maxLocalUARTCreditsCount) ? this._maxLocalUARTCreditsCount : ((minLocalUARTCreditsCount < 0) ? 0 : minLocalUARTCreditsCount	);
		this._minLocalUARTCreditsCount = count;
	}

	/**
	 * Gets a string representation of this instance consisting of Bluetooth name and Bluetooth address.
	 * @return A String instance consisting of this instance's Bluetooth name and Bluetooth address.
	 */
	@Override
	public String toString() {
		return this.getName() + " " + this.getAddress();
	}

	
	/**
	 *  Sets a tag object.
	 *  Allows for 'tagging' any application-defined object to the peripheral instance. This property is not used within the TIOPeripheral implementation.
	 *  @param tag An Object instance to tag to this TIOPeripheral instance.
	 */
	public void setTag(Object tag){
		this._tag = tag;
	}
	
	/**
	 *  Gets the tag object.
	 *  This property is not used within the TIOPeripheral implementation.
	 *  @return tag The Object instance tagged to this TIOPeripheral instance.
	 */
	public Object getTag() {
		return this._tag;
	}

	//******************************************************************************
	// Public methods
	//******************************************************************************
	
	/**
	 *  Initiates the establishing of a TerminalIO connection to the remote device.
	 *
	 * The TerminalIO connection establishment consists of various asynchronous operations over the air.
	 * In case of a successful connection establishment, the TIOPeripheralCallback method {@link TIOPeripheralCallback#tioPeripheralDidConnect(TIOPeripheral) tioPeripheralDidConnect()} method is invoked;
	 * otherwise {@link TIOPeripheralCallback#tioPeripheralDidFailToConnect(TIOPeripheral, String) tioPeripheralDidFailToConnect()} will be invoked.
	 * Data exchange operations cannot be performed unless {@link TIOPeripheralCallback#tioPeripheralDidConnect(TIOPeripheral) TIOPeripheralCallback.tioPeripheralDidConnect()} has been invoked; the application may also check for {@link #isConnected()}.
	 *
	 * To cancel a pending connection request, call {@link #disconnect disconnect()}.
	 * 
	 * If the remote device is configured to require security and has not been paired before with the local device, a silently running Bluetooth pairing process will be initiated.
	 */ 	
	public void connect() {
		STTrace.method("connect");
		
		if (this._isConnected || this._isConnecting)
		{
			STTrace.error("already connected or connecting...");
			return;
		}
		
		this._didSubscribeUARTTx = false;
		this._didSubscribeUARTTxCredits = false;
		this._didGrantInitialUARTRxCredits	 = false;
		
		this._isWriting = false;
		this._isConnecting = true;
		
		this.processTIOConnection(); 
	}

	/**
	 *  Requests the operating system to disconnect from the remote device.
	 *  The connection termination will be reported via the TIOPeripheralCallback method {@link TIOPeripheralCallback#tioPeripheralDidDisconnect(TIOPeripheral, String) tioPeripheralDidDisconnect()}.
	 */ 	
	public void disconnect() {
		STTrace.method("disconnect");

		if (!this._isConnected && !this._isConnecting)
		{
			STTrace.error("neither connected nor connecting...");
			return;
		}
		
		this._gatt.disconnect();
	}
	
	/**
	 *  Writes UART data to the remote device. Requires the peripheral to be TerminalIO connected.
	 *
	 *  The specified data will be appended to a write buffer, so there is generally no limitation for the data's size except for memory conditions within the operating system.
	 *  Nevertheless, data sizes should be considered carefully and transmission rates and power consumption should be taken into account.
	 *
	 *  For each data block written, the TIOPeripheralCallback method {@link TIOPeripheralCallback#tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral, int) tioPeripheralDidWriteNumberOfUARTBytes()} will be invoked reporting the number of bytes written.
	 *  If there are no more data to be written, the TIOPeripheralDelegate method {@link TIOPeripheralCallback#tioPeripheralUARTWriteBufferEmpty(TIOPeripheral) tioPeripheralUARTWriteBufferEmpty()} will be invoked.
	 *
	 *  @param data Data to be written.
	 */ 
	public void writeUARTData(byte[] data) {
		STTrace.method("writeUARTData", "\n" + STTrace.byteArrayToHexDump(data));
		
		if (!this._isConnected)
		{
			STTrace.error("not connected...");
			return;
		}
		
		// append data to write buffer taking care of thread safety as the background thread might be working on the buffer...
		synchronized (this._writeLock)
		{
			byte[] uartDataToBeWritten = new byte[this._uartDataToBeWritten.length + data.length];
			System.arraycopy(this._uartDataToBeWritten, 0, uartDataToBeWritten, 0, this._uartDataToBeWritten.length);
			System.arraycopy(data, 0, uartDataToBeWritten, this._uartDataToBeWritten.length, data.length);
			this._uartDataToBeWritten = uartDataToBeWritten;
		}

		this.launchUARTDataWritingOnBackgroundThread();
	} 

	/**
	 *  Initiates the reading of the current RSSI value. Requires the peripheral to be TerminalIO connected.
	 *
	 *  Reading of the RSSI value is performed asynchronously. The identified RSSI value will be reported via the TIOPeripheralCallback method {@link TIOPeripheralCallback#tioPeripheralDidUpdateRSSI(TIOPeripheral, int) tioPeripheralDidUpdateRSSI()}.
	 */ 	
	public void readRSSI() {
//		STTrace.method("readRSSI");

		if (!this._isConnected)
		{
			STTrace.error("not connected...");
			return;
		}
		
		if (this._rssiDeferralPending) {
			// Android BLE stack is not ready to handle a new readRemoteRSSI request, so we transmit the last known value here... 
			STTrace.error("RSSI deferral pending...");
			this.raiseDidUpdateRSSI(this._lastRSSI);
		} else {
			this._rssiDeferralPending = true;
			this._gatt.readRemoteRssi();
		}
	}

	
	//******************************************************************************
	// Internal methods
	//******************************************************************************
	
	private void processTIOConnection()
	{
		STTrace.method("processTIOConnection");
		
		if (this._gatt == null /*|| this._gatt.getConnectionState(this._device) == BluetoothGatt.STATE_DISCONNECTED*/) {
			// first we need to establish a BLE connection
			this._gatt = this._device.connectGatt(TIOManager.sharedInstance().getApplicationContext(), false, new GattListener());
		}
		else if (this._gatt.getServices().size() == 0 || this._tioService == null || this._uartTxCharacteristic == null || this._uartTxCreditsCharacteristic == null) {
			// BLE connection has been established but the BLE service and/or characteristics instances are not known, so a service discovery is required.
			this.discoverServicesAndCharacteristics();
		}
		else if (!this._didSubscribeUARTTxCredits)
		{
			// UART TX credits notification subscription is required.
			this.subscribeToCharacteristic(this._uartTxCreditsCharacteristic);
		}
		else if (!this._didSubscribeUARTTx)
		{
			// UART TX data notification subscription is required.
			this.subscribeToCharacteristic(this._uartTxCharacteristic);
		}
		else if (!this._didGrantInitialUARTRxCredits)
		{
			this._uartTxCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

			// UART RX credits have to be granted to the peripheral in order to establish the TerminalIO connection
			this.grantLocalUARTCredits();
		}
		else
		{
			// set appropriate write mode for characteristics
			this._uartTxCreditsCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
			this._uartTxCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
			
			// TIO connection has been established
			this._isConnected = true;
			this._isConnecting = false;
			
			this.raiseDidConnect();
		}
	} 	
	
	private void handleTIOConnectionError(boolean disconnectRequired, String message) {
		STTrace.method("handleTIOConnectionError", message);
		
		if (disconnectRequired) {
			this.disconnect();
		} else {
			this._isConnected = false;
			this._localUARTCreditsCount = this._remoteUARTCreditsCount = this._pendingLocalUARTCreditsCount = 0;
			this._gatt.close();
			this._gatt = null;
			this._uartTxCharacteristic = null;
			this._uartTxCreditsCharacteristic = null;
		}
		this._isConnecting = false;
		this.raiseDidFailToConnect(message);
	} 
	
	private void handleDisconnect(String message) {
		STTrace.method("handleDisconnect");
		
		this._isConnected = this._isConnecting = false;
		this._localUARTCreditsCount = this._remoteUARTCreditsCount = this._pendingLocalUARTCreditsCount = 0;
		this._gatt.close();
		this._gatt = null;
		this._uartTxCharacteristic = null;
		this._uartTxCreditsCharacteristic = null;
		
		this._localUARTCreditsCount = 0;
		synchronized (this._writeLock)
		{
			this._remoteUARTCreditsCount = 0;
			this._uartDataToBeWritten = new byte[0];
		}
		
		if (this._bondingReceiver != null) {
			try {
				TIOManager.sharedInstance().getApplicationContext().unregisterReceiver(this._bondingReceiver);
			} catch (Exception ex) {
				// we expect an exception if the bonding receiver has already been unregistered, see onDescriptorWrite() 
//				STTrace.exception(ex);
			}
		}
		
		this.raiseDidUpdateLocalUARTCreditsCount(this._localUARTCreditsCount);
		this.raiseDidUpdateRemoteUARTCreditsCount(this._remoteUARTCreditsCount);

		this.raiseDidDisconnect(message);
 	}
	
	private void discoverServicesAndCharacteristics() {
		STTrace.method("discoverServicesAndCharacteristics");
		
		// erase invalid characteristics instances
		this._uartTxCharacteristic	= null;
		this._uartTxCreditsCharacteristic = null;
		
		this._gatt.discoverServices();
	}

	private void subscribeToCharacteristic(BluetoothGattCharacteristic characteristic) {
		STTrace.method("subscribeToCharacteristic", characteristic.toString());

		this._gatt.setCharacteristicNotification(characteristic, true);

		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

	    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(TIOPeripheral.CCC_BITS_UUID );
		byte[] descValue = (characteristic == this._uartTxCreditsCharacteristic) ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
	    descriptor.setValue(descValue);
	    this._gatt.writeDescriptor(descriptor);
	}
	
	private void grantLocalUARTCredits() {
		STTrace.method("grantLocalUARTCredits");
		
		if (this._pendingLocalUARTCreditsCount != 0)
			return;

		this._pendingLocalUARTCreditsCount = this._maxLocalUARTCreditsCount - this._localUARTCreditsCount;
		byte[] value = new byte[] { (byte) (this._pendingLocalUARTCreditsCount & 0xff) };
		this._uartRxCreditsCharacteristic.setValue(value);
		this._gatt.writeCharacteristic(this._uartRxCreditsCharacteristic);
	}
	
	private void launchUARTDataWritingOnBackgroundThread() {
		STTrace.method("launchUARTDataWritingOnBackgroundThread");
		
		// take care of thread safety of self.isWriting
		synchronized (this._writeLock)
		{
			if (this._isWriting)
				return;
			
			this._isWriting = true;
		}
		
		Thread thread = new Thread( new Runnable() {
			@Override
			public void run() {
				TIOPeripheral.this.writeUARTDataBlocks();
			}
		});
		thread.start();
	}

	private void writeUARTDataBlocks() {
		STTrace.method("writeUARTDataBlocks");
		
		// we are in a background thread here and will loop sending as many data blocks as possible...
		do
		{
			// take care of thread safety of this._uartDataToBeWritten, this._isWriting and this._remoteUARTCreditsCount
			synchronized (this._writeLock)
			{
				if (!this._isConnected)
				{
					this._isWriting = false;
					break;
				}
				
				if (this._uartDataToBeWritten.length == 0)
				{
					// write buffer empty
					this.raiseUARTWriteBufferEmpty();
					this._isWriting = false;
					break;
				}
				if (this._remoteUARTCreditsCount == 0)
				{
					// no more UART credits for remote device
					this._isWriting = false;
					break;
				}

				// get next data block
				byte[] data = this.getNextUARTDataBlock();
				// write data to UART characteristic without expecting a response
				STTrace.line("writing " + data.length + "  bytes");
				this._lastNumberOfBytesWritten = data.length;
				this._uartRxCharacteristic.setValue(data);
				this._gatt.writeCharacteristic(this._uartRxCharacteristic);

				// wait for write callback from Android: it is running in a different thread!
				try {
					this._writeLock.wait(/*TIOPeripheral.WRITE_CALLBACK_TIMEOUT*/);
				} catch (InterruptedException ex) {
					STTrace.exception(ex);
					this._isWriting = false;
					break;
				}
			}
		} while (true);
	}

	private byte[] getNextUARTDataBlock() {
		// determine length of next data block
		int length = (this._uartDataToBeWritten.length > TIO.MAX_UART_DATA_SIZE) ? TIO.MAX_UART_DATA_SIZE : this._uartDataToBeWritten.length;
		
		// crop data to be written from buffer
		byte[] data = Arrays.copyOfRange(this._uartDataToBeWritten, 0, length);
		if (length == this._uartDataToBeWritten.length) {
			this._uartDataToBeWritten = new byte[0];
		} else {
			this._uartDataToBeWritten = Arrays.copyOfRange(this._uartDataToBeWritten, length, this._uartDataToBeWritten.length);
		}

		return data;
	} 	
	
	protected String createErrorMessage(int status) {
		String errorMessage = "";
		if (status != BluetoothGatt.GATT_SUCCESS) {
			errorMessage = "Android error# " + status;
		}
		return errorMessage;
	}
	
	
	//******************************************************************************
	// BluetoothGattCallback implementation 	
	//******************************************************************************
		
	protected class GattListener extends BluetoothGattCallback {
		
		//	Callback triggered as a result of a remote characteristic notification.
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			STTrace.method("onCharacteristicChanged");
			if (gatt != TIOPeripheral.this._gatt) {
				STTrace.error("invalid BluetoothGatt instance...");
				return;
			}
	
			if (characteristic.getUuid().equals(TIO.UART_TX_UUID)) {
				// the remote device has sent UART data
				TIOPeripheral.this.raiseDidReceiveUARTData(characteristic.getValue());
				
				// by sending this UART data packet, the remote device has consumed one of the granted local credits
				TIOPeripheral.this._localUARTCreditsCount--;
				TIOPeripheral.this.raiseDidUpdateLocalUARTCreditsCount(TIOPeripheral.this._localUARTCreditsCount);
				if (TIOPeripheral.this._localUARTCreditsCount <= TIOPeripheral.this._minLocalUARTCreditsCount)
				{
					// grant a reasonable amount of new credits before an underrun occurs on the remote device
					TIOPeripheral.this.grantLocalUARTCredits();
				}
			}
			else if (characteristic.getUuid().equals(TIO.UART_TX_CREDITS_UUID)) {
				// the remote device has granted additional UART credits
				// extract credits count from characteristic value
				byte creditCount = TIOPeripheral.this._uartTxCreditsCharacteristic.getValue()[0];
				// add received credits to counter taking care of write thread safety
				synchronized (TIOPeripheral.this._writeLock) {
					TIOPeripheral.this._remoteUARTCreditsCount += creditCount;
					if (TIOPeripheral.this._remoteUARTCreditsCount > TIO.MAX_UART_CREDITS_COUNT)
					{
						STTrace.error("invalid remote UART credit count " + TIOPeripheral.this._remoteUARTCreditsCount);
						TIOPeripheral.this._remoteUARTCreditsCount = TIO.MAX_UART_CREDITS_COUNT;
					}
					TIOPeripheral.this.raiseDidUpdateRemoteUARTCreditsCount(TIOPeripheral.this._remoteUARTCreditsCount);
				}
				// trigger writing if required
				TIOPeripheral.this.launchUARTDataWritingOnBackgroundThread();
			} 		
		}
	
		//	Callback indicating the result of a characteristic write operation.
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			STTrace.method("onCharacteristicWrite");
			if (gatt != TIOPeripheral.this._gatt) {
				STTrace.error("invalid BluetoothGatt instance...");
				return;
			}
			
			if (characteristic.getUuid().equals(TIO.UART_RX_CREDITS_UUID)) {
				if (status == BluetoothGatt.GATT_SUCCESS)
				{
					// granting of local credits to remote device was successful
					if (TIOPeripheral.this._isConnecting) {
						TIOPeripheral.this._didGrantInitialUARTRxCredits = true;
						TIOPeripheral.this.processTIOConnection();
					}
					
					TIOPeripheral.this._localUARTCreditsCount += TIOPeripheral.this._pendingLocalUARTCreditsCount;
					TIOPeripheral.this._pendingLocalUARTCreditsCount = 0;
					TIOPeripheral.this.raiseDidUpdateLocalUARTCreditsCount(TIOPeripheral.this._localUARTCreditsCount);
				} else {
					STTrace.error("error status " + status);
					if (TIOPeripheral.this._isConnecting) {
						TIOPeripheral.this.handleTIOConnectionError(true, "Failed to grant initial UART credits; " + TIOPeripheral.this.createErrorMessage(status));
					}
				}
			} else if (characteristic.getUuid().equals(TIO.UART_RX_UUID)) {
				synchronized (TIOPeripheral.this._writeLock) {
					// update remote credits
					TIOPeripheral.this._remoteUARTCreditsCount--;
					// notify upper layer
					TIOPeripheral.this.raiseDidWriteNumberOfUARTBytes(TIOPeripheral.this._lastNumberOfBytesWritten);
					TIOPeripheral.this.raiseDidUpdateRemoteUARTCreditsCount(TIOPeripheral.this._remoteUARTCreditsCount);
					TIOPeripheral.this._writeLock.notify();
				}
			}
		}
		
	//	Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			STTrace.method("onConnectionStateChange " + newState + "; status " + status);
			if (gatt != TIOPeripheral.this._gatt) {
				STTrace.error("invalid BluetoothGatt instance...");
				return;
			}
			
			switch (newState) {
	
				case BluetoothGatt.STATE_CONNECTED:
					if (status != BluetoothGatt.GATT_SUCCESS) {
						STTrace.error("error status " + status);
						TIOPeripheral.this.handleTIOConnectionError(false, "Failed to connect; " + TIOPeripheral.this.createErrorMessage(status));
					} else {
						TIOPeripheral.this.processTIOConnection();
					}
					break;
				
				case BluetoothGatt.STATE_DISCONNECTED:
					TIOPeripheral.this.handleDisconnect(TIOPeripheral.this.createErrorMessage(status));
					break;
	
				default:
					
					break;
			}
		}
		
		//	Callback indicating the result of a descriptor write operation.
		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			STTrace.method("onDescriptorWrite");
			if (gatt != TIOPeripheral.this._gatt) {
				STTrace.error("invalid BluetoothGatt instance...");
				return;
			}
	
			BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
			
			if (status != BluetoothGatt.GATT_SUCCESS) {
				STTrace.error("error status " + status);
				
				if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
					STTrace.error("insufficient authentication");
					
		            if (true /*this._gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE*/) {
					// setting the new Intentfilter here seems to be incorrect need rework
		                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		                if (TIOPeripheral.this._bondingReceiver == null) {
		                	TIOPeripheral.this._bondingReceiver = new BroadcastReceiver() {
			            	    @Override
			            	    public void onReceive(Context context, Intent intent) {
			            	        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			            	        if (!device.getAddress().equals(TIOPeripheral.this._gatt.getDevice().getAddress()))
			            	            return;
			            	        
			            	        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			            	        int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
			            	        STTrace.line("bond state " + previousBondState + " -> " + bondState);
	
			            	        if (bondState == BluetoothDevice.BOND_BONDED) {
			            	        	// proceed with TIO connection setup...
			            	        	TIOPeripheral.this.processTIOConnection();
			            	        	try {
				            	            TIOManager.sharedInstance().getApplicationContext().unregisterReceiver(this);
			            	        	} catch (Exception ex) {
			            	        		STTrace.exception(ex);
			            	        	}
			            	        }
			            	    }
			            	};	
		                }
		                TIOManager.sharedInstance().getApplicationContext().registerReceiver(TIOPeripheral.this._bondingReceiver, filter);
	//	            } else {
	//	                // should not occur...
	//					this.handleTIOConnectionError(true, "Failed to subscribe for " + characteristic.toString() + " due to bonding error; " + this.createErrorMessage(status));
		            }
				} else {
					TIOPeripheral.this.handleTIOConnectionError(true, "Failed to subscribe for " + characteristic.toString() + "; " + TIOPeripheral.this.createErrorMessage(status));
				}
	
				return;
			}
	
			if (characteristic.getUuid().equals(TIO.UART_TX_UUID))
			{
				STTrace.line("  subscribed to UART characteristic");
				TIOPeripheral.this._didSubscribeUARTTx = true;
			}
			else if (characteristic.getUuid().equals(TIO.UART_TX_CREDITS_UUID))
			{
				STTrace.line("  subscribed to UART_CREDITS characteristic");
				TIOPeripheral.this._didSubscribeUARTTxCredits = true;
			}
	
			// proceed with TIO connection establishment
			TIOPeripheral.this.processTIOConnection();
		}
	
		//	Callback reporting the RSSI for a remote device connection.
		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
	//		STTrace.method("onReadRemoteRssi");
			if (gatt != TIOPeripheral.this._gatt) {
				STTrace.error("invalid BluetoothGatt instance...");
				return;
			}
			
			if (status != BluetoothGatt.GATT_SUCCESS) {
				STTrace.error("Failed to read RSSI; " + TIOPeripheral.this.createErrorMessage(status));
				return;
			}
			
			TIOPeripheral.this._lastRSSI = rssi;
			TIOPeripheral.this.raiseDidUpdateRSSI(rssi);
			
			// Android BLE stack is not ready to handle a new readRemoteRSSI request before another approx. 1000ms have passed, so we defer calls to readRemoteRSSI() accordingly
			TIOPeripheral.this._rssiDeferralTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					TIOPeripheral.this._rssiDeferralPending = false;
				}
			}, TIOPeripheral.RSSI_DEFERRAL_INTERVAL);
		}
	
		//	Callback invoked when the list of remote services, characteristics and descriptors for the remote device have been updated, ie new services have been discovered.
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			STTrace.method("onServicesDiscovered status " + status);
			if (gatt != TIOPeripheral.this._gatt) {
				STTrace.error("invalid BluetoothGatt instance...");
				return;
			}
			
			TIOPeripheral.this._tioService = null;
			TIOPeripheral.this._uartTxCharacteristic = null;
			TIOPeripheral.this._uartTxCreditsCharacteristic = null;
			STTrace.line("found " + TIOPeripheral.this._gatt.getServices().size() + " services");
			for (BluetoothGattService service: TIOPeripheral.this._gatt.getServices()) {
				STTrace.line("found service " + service.getUuid().toString());
				
				if (service.getUuid().equals(TIO.SERVICE_UUID)) {
					// memorize Android service instance 
					TIOPeripheral.this._tioService = service;
					
					for (BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
						STTrace.line("   found characteristic " + characteristic.getUuid().toString());
						
						if (characteristic.getUuid().equals(TIO.UART_TX_CREDITS_UUID)) {
							STTrace.line("   found UARTTxCredits characteristic " + characteristic.getUuid().toString() + "; properties = " + characteristic.getProperties() + "; permissions = " + characteristic.getPermissions());
							// memorize Android characteristic instance 
							TIOPeripheral.this._uartTxCreditsCharacteristic = characteristic;
						}
						else if (characteristic.getUuid().equals(TIO.UART_TX_UUID)) {
							STTrace.line("   found UARTTx characteristic " + characteristic.getUuid().toString() + "; properties = " + characteristic.getProperties() + "; permissions = " + characteristic.getPermissions());
							// memorize Android characteristic instance 
							TIOPeripheral.this._uartTxCharacteristic = characteristic;
						}
						else if (characteristic.getUuid().equals(TIO.UART_RX_CREDITS_UUID)) {
							STTrace.line("   found UARTRxCredits characteristic " + characteristic.getUuid().toString() + "; properties = " + characteristic.getProperties() + "; permissions = " + characteristic.getPermissions());
							// memorize Android characteristic instance 
							TIOPeripheral.this._uartRxCreditsCharacteristic = characteristic;
						}
						else if (characteristic.getUuid().equals(TIO.UART_RX_UUID)) {
							STTrace.line("   found UARTRx characteristic " + characteristic.getUuid().toString() + "; properties = " + characteristic.getProperties() + "; permissions = " + characteristic.getPermissions());
							// memorize Android characteristic instance 
							TIOPeripheral.this._uartRxCharacteristic = characteristic;
							// set write mode without response for UART characteristic  
							TIOPeripheral.this._uartRxCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
						}
	
						for (BluetoothGattDescriptor descriptor: characteristic.getDescriptors()) {
							STTrace.line("       found descriptor " + descriptor.getUuid().toString());
						}
					}
				}
			}
			
			if (TIOPeripheral.this._tioService == null) {
				// the remote module does not provide the TIO service (should not occur...) 
				TIOPeripheral.this.handleTIOConnectionError(true, "TIO service not discovered.");
			} else {
				if (TIOPeripheral.this._uartTxCharacteristic == null || TIOPeripheral.this._uartTxCreditsCharacteristic == null
						|| TIOPeripheral.this._uartRxCharacteristic == null || TIOPeripheral.this._uartRxCreditsCharacteristic == null)
				{
					// the remote module does not provide mandatory TIO characteristics (should not occur...)
					TIOPeripheral.this.handleTIOConnectionError(true, "TIO characteristics missing.");
				}
				// TIO service and characteristics have been discovered; proceed with TIO connection establishment
				TIOPeripheral.this.processTIOConnection();
			}
		}
	}

	
	//******************************************************************************
	// Callback events  
	//******************************************************************************
	
	protected void raiseDidConnect() {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidConnect(TIOPeripheral.this);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidConnect(this);
		}
	}
	
	protected void raiseDidFailToConnect(final String errorMessage) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidFailToConnect(TIOPeripheral.this, errorMessage);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidFailToConnect(this, errorMessage);
		}
	}
	
	protected void raiseDidDisconnect(final String errorMessage) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidDisconnect(TIOPeripheral.this, errorMessage);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidDisconnect(this, errorMessage);
		}
	}
	
	protected void raiseDidReceiveUARTData(final byte[] data) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidReceiveUARTData(TIOPeripheral.this, data);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidReceiveUARTData(this, data);
		}
	}
	
	protected void raiseDidWriteNumberOfUARTBytes(final int bytesWritten) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral.this, bytesWritten);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidWriteNumberOfUARTBytes(this, bytesWritten);
		}
	}
	
	protected void raiseUARTWriteBufferEmpty() {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralUARTWriteBufferEmpty(TIOPeripheral.this);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralUARTWriteBufferEmpty(this);
		}
	}
	
	protected void raiseDidUpdateAdvertisement() {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidUpdateAdvertisement(TIOPeripheral.this);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidUpdateAdvertisement(this);
		}
	}
	
	protected void raiseDidUpdateRSSI(final int rssi) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidUpdateRSSI(TIOPeripheral.this, rssi);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidUpdateRSSI(this, rssi);
		}
	}
	
	protected void raiseDidUpdateLocalUARTCreditsCount(final int creditsCount) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidUpdateLocalUARTCreditsCount(TIOPeripheral.this, creditsCount);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidUpdateLocalUARTCreditsCount(this, creditsCount);
		}
	}
	
	protected void raiseDidUpdateRemoteUARTCreditsCount(final int creditsCount) {
		if (this._listener == null)
			return;
		
		if (this._listenerIsActivity) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					TIOPeripheral.this._listener.tioPeripheralDidUpdateRemoteUARTCreditsCount(TIOPeripheral.this, creditsCount);
				}
			};
			((Activity) this._listener).runOnUiThread(runnable);
		}
		else {
			this._listener.tioPeripheralDidUpdateRemoteUARTCreditsCount(this, creditsCount);
		}
	}
	
	
	//******************************************************************************
	// Internal interface towards TIOManager
	//******************************************************************************
	
	/**
//	 * @deprecated (exclude for javadoc documentation generation) 
	 * Internal method to be called by TIOManager only; do not call from application code.
	 */
	public static TIOPeripheral createFromScanResult(BluetoothDevice device, TIOAdvertisement advertisement) {
		return new TIOPeripheral(device, advertisement);		
	}
	
	/**
//	 * @deprecated (exclude for javadoc documentation generation) 
	 * Internal method to be called by TIOManager only; do not call from application code.
	 */
	public static TIOPeripheral createFromBluetoothDevice(BluetoothDevice device) {
		return new TIOPeripheral(device);
	}
	
	/**
//	 * @deprecated (exclude for javadoc documentation generation) 
	 * Internal method to be called by TIOManager only; do not call from application code.
	 */
	public boolean updateWithAdvertisement(TIOAdvertisement advertisement) {
		boolean result = false;
		if (this._advertisement == null || !this._advertisement.equals(advertisement))
		{
			this._advertisement = advertisement;
			result = true;
			this.raiseDidUpdateAdvertisement();
		}
		
		return result; 		
	}
	
}
