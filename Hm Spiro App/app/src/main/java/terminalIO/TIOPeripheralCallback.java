package terminalIO;

/**
 *  A TIOPeripheral's event listener implements the TIOPeripheralCallback interface in order to monitor connection events, data exchange and peripheral property updates.
 */
public interface TIOPeripheralCallback {

	/**
	 *  Invoked when a TerminalIO connection has been successfully established.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 */
	void tioPeripheralDidConnect(TIOPeripheral peripheral);

	/**
	 *  Invoked when a TerminalIO connection establishment has failed.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 *  @param errorMessage A String containing information about the failure's cause.
	 */
	void tioPeripheralDidFailToConnect(TIOPeripheral peripheral, String errorMessage);

	/**
	 *  Invoked when an established TerminalIO connection is disconnected.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 *  @param errorMessage A String containing information about the disconnect's cause, or an empty String on intentional disconnects.
	 */
	void tioPeripheralDidDisconnect(TIOPeripheral peripheral, String errorMessage);

	/**
	 *  Invoked when UART data transmitted by the remote peripheral have been received.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 *  @param data A byte array containing the received UART data.
	 */
	void tioPeripheralDidReceiveUARTData(TIOPeripheral peripheral, byte[] data);

	/**
	 *  Invoked when a UART data block has been written to the remote device.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 *  @param bytesWritten The number of bytes written.
	 */
	void tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral peripheral, int bytesWritten);

	/**
	 *  Invoked when all available UART data have been written to the remote device.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 */
	void tioPeripheralUARTWriteBufferEmpty(TIOPeripheral peripheral);

	/**
	 *  Invoked when an updated advertisement for a known peripheral has been detected after calling {@link TIOManager#startScan() TIOManager.startScan()}.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 */
	void tioPeripheralDidUpdateAdvertisement(TIOPeripheral peripheral);

	/**
	 *  Invoked when an RSSI value is reported as a response to calling {@link TIOPeripheral#readRSSI() TIOPeripheral.readRSSI()}.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 *  @param rssi The latest RSSI value.
	 */
	void tioPeripheralDidUpdateRSSI(TIOPeripheral peripheral, int rssi);


	/**
	 *  Invoked when the number of local UART credits has changed due to received data or new credits granted to the remote peripheral.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 *  @param creditsCount The current number of local UART credits.
	 */
	void tioPeripheralDidUpdateLocalUARTCreditsCount(TIOPeripheral peripheral, int creditsCount);

	/**
	 *  Invoked when the number of remote UART credits has changed due to sent data or new credits granted by the remote peripheral.
	 *  @param peripheral The TIOPeripheral instance this event applies for.
	 *  @param creditsCount The current number of remote UART credits.
	 */
	void tioPeripheralDidUpdateRemoteUARTCreditsCount(TIOPeripheral peripheral, int creditsCount);

}
