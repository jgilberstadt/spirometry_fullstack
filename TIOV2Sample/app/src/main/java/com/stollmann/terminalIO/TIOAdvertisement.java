package com.stollmann.terminalIO;

/**
 *  TIOAdvertisement instances represent a remote TerminalIO peripherals's advertisement data.
 *  The application retrieves TIOAdvertisement instances as the {@link TIOPeripheral#getAdvertisement() TIOPeripheral.getAdvertisement()} property.
 *  The application shall not create any TIOAdvertisement instances of its own.
 */ 
public class TIOAdvertisement {

	private String _localName = "";
	private TIO.OperationMode _operationMode = TIO.OperationMode.BondableFunctional;
	private boolean _connectionRequested;

	//******************************************************************************
	// Initialization 
	//******************************************************************************

	private TIOAdvertisement(String localName) {
		this._localName = localName;
	}
	
	
	//******************************************************************************
	// Properties
	//******************************************************************************
	
	/**
	 * Gets the peripheral's local name as contained within the peripheral's advertisement data.
	 * @return A String instance containing the local name as contained within the peripheral's advertisement data.
	 */
	public String getLocalName() {
		return this._localName;
	}
	
	/**
	 * Gets the peripheral's operation mode as contained within the peripheral's advertisement data.
	 * @return A {@link TIO.OperationMode TIO.OperationMode} value extracted from the peripheral's advertisement data.
	 */
	public TIO.OperationMode getOperationMode() {
		return this._operationMode;
	}
	
	/**
	 * Gets the peripheral's connection requested state as contained within the peripheral's advertisement data.
	 * @return <code>true</code> if the peripheral requests a connection, <code>false</code> otherwise. 
	 */
	public boolean isConnectionRequested() {
		return this._connectionRequested;
	}
	
	/**
	 * Gets a string representation of this instance containing local name, operation mode and connection request state. 
	 * @return A String instance containing local name, operation mode and connection request state.
	 */
	public String getDisplayString() {
		String description = this._localName + " [" + this._operationMode.toString();
		if (this._connectionRequested)
			description += " connection requested";
		description +=  "]";
		return description;
	}
	
	/**
	 * Gets a tring representation of this instance.
	 * @return A String instance representing this instance.
	 */
	@Override
	public String toString() {
		return "TIOAdvertisement [localName = " + this._localName + "; connectionRequested = " + this._connectionRequested + "; operationMode = " + this._operationMode.toString() + "]";
	}
	
	
	//******************************************************************************
	// Public methods
	//******************************************************************************
	
	/**
	 *  Compares this instance to another TIOAdvertisement instance for equality of contents	.
	 *  @param advertisement The TIOAdvertisement instance to compare this instance to.
	 *  @return <code>true</code>, if local name, operation mode and connection requested state are equal, <code>false</code> otherwise.
	 */ 
	public boolean equals(TIOAdvertisement advertisement) {
		if (!this._localName.equals(advertisement._localName))
			return false;
		if (!this._operationMode.equals(advertisement._operationMode))
			return false;
		if (!this._connectionRequested == advertisement._connectionRequested)
			return false;
		return true;
	}


	//******************************************************************************
	// Internal methods
	//******************************************************************************
	
	private boolean evaluateData(byte[] data) {
		boolean result = false;
		do
		{
			if (data.length < 7)
				break;
			
			// Check for mandatory TerminalIO specific values.
			if ((data[0] & 0xff) != 0x8f || (data[1] & 0xff) != 0x0 || (data[2] & 0xff) != 0x9 || (data[3] & 0xff) != 0xb0)
				break;
			
			// Extract peripheral state information.
			this._connectionRequested = (data[6] == 1);
			this._operationMode = TIO.OperationMode.fromValue(data[5]);
		
			result = true;
			
		} while (false);
		
		return result;
	}
	
	
	//******************************************************************************
	// Internal interface towards TIOManager
	//******************************************************************************
	
	/**
//	 * @deprecated (exclude for javadoc documentation generation) 
	 * Internal method to be called by TIOManager only; do not call from application code.
	 */
	public static final TIOAdvertisement createFromScanRecord(String localName, byte[] manufacturerSpecificData) {
		TIOAdvertisement advertisement = new TIOAdvertisement(localName);
		if (advertisement.evaluateData(manufacturerSpecificData) == false)
				return null;
		return advertisement;
	}

}
