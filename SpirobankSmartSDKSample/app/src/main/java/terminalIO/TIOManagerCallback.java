package terminalIO;

/**
 *  The TIOManager's event listener implements the TIOManagerCallback interface in order to monitor system Bluetooth availability and to manage information about TIOPeripheral objects.
 */
public interface TIOManagerCallback {
	
	/**
	 *  Invoked when a TerminalIO peripheral has been newly discovered.
	 *
	 *  This method is invoked when a TerminalIO peripheral currently not contained within the TIOManager peripherals list has been discovered during a scan procedure,
	 *  i.e. after having called the TIOManager method {@link TIOManager#startScan() TIOManager.startScan()}.
	 *  The peripheral will then be added to the TIOManager peripherals list, and this method will not be invoked again for this specific peripheral.
	 *
	 *  If a known peripheral with an updated advertisement is detected, the {@link #tioManagerDidUpdatePeripheral(TIOPeripheral) tioManagerDidUpdatePeripheral()} method will be invoked.
	 *  @param manager	The TIOManager singleton instance.
	 *  @param peripheral A TIOPeripheral instance representing the discovered TerminalIO peripheral.
	 */ 
	void tioManagerDidDiscoverPeripheral(TIOPeripheral peripheral);
	 
	/**
	 *  Invoked when an update of a TerminalIO peripheral's advertisement has been detected.
	 *
	 *  This method is invoked when a known TerminalIO peripheral with a changed advertisement is discovered
	 *  after having started a new scan procedure by calling {@link TIOManager#startScan() TIOManager.startScan()}.
	 *  @param manager	The TIOManager singleton instance.
	 *  @param peripheral A TIOPeripheral instance representing the TerminalIO peripheral having updated its advertisement.
	 */ 
	void tioManagerDidUpdatePeripheral(TIOPeripheral peripheral);
}
