package terminalIO;

import java.util.UUID;

/**
 *  The TIO class is purely static and provides TerminalIO specific UUID instances and constants.
 */ 
public class TIO {
	
	/**
	 *  TerminalIO peripheral operation mode constants.
	 */ 	
	public enum OperationMode {
		/**
		 *  The peripheral is (temporarily) in bonding operation mode. A central shall perform the specified bonding procedure when connecting.
		 */ 
		BondingOnly(0x0),
		/**
		 *  The peripheral is in functional operation mode. A central may connect in order to transmit and receive UART data (or GPIO states, if supported).
		 */ 
		Functional(0x1),
		/**
		 *  The peripheral is in bondable and functional mode. A central may connect and perform the specified bonding procedure or transmit and receive UART data (or GPIO states, if supported).
		 */ 		
		BondableFunctional(0x10);
		
		private int _value;
		private OperationMode(int value) { this._value = value; }
		
		/**
		 * Creates a TIO.OperationMode value from an integer value.
		 * @param value The Integer to create a TIO.OperationMode value from.
		 * @return The requested TIO.OperationMode value.
		 */
		public static OperationMode fromValue(int value) {
			if (value == BondingOnly._value)
				return BondingOnly;
			if (value == Functional._value)
				return Functional;
			return BondableFunctional;
		}
		
		/**
		 * Gets this value's Integer value.
		 * @return An Integer containing this value's Integer value. 
		 */
		public int getValue() { return this._value; }
		
		/**
		 * Compares this value an another value for equality.
		 * @param operationMode TIO.OperationMode value to compare this value to.
		 * @return <code>true</code> if both value are equal, <code>false</code> otherwise.
		 */
		public boolean equals(OperationMode operationMode) { return this._value == operationMode._value; }
	}
	
	/**
	 *  The TerminalIO service UUID.
	 *  @return A UUID instance containing the TerminalIO service UUID.
	 */ 	
	public static final UUID SERVICE_UUID = UUID.fromString("0000FEFB-0000-1000-8000-00805F9B34FB");
	
	/**
	 *  The TerminalIO UART RX characteristic UUID.
	 *  This characteristic is by the central to transmit UART data to the peripheral.
	 *  @return A UUID instance containing the TerminalIO UART characteristic UUID.
	 */ 	
	public static final UUID UART_RX_UUID = UUID.fromString("00000001-0000-1000-8000-008025000000");
	
	/**
	 *  The TerminalIO UART TX characteristic UUID.
	 *  This characteristic is by the peripheral to transmit UART data to the central.
	 *  @return A UUID instance containing the TerminalIO UART characteristic UUID.
	 */ 	
	public static final UUID UART_TX_UUID = UUID.fromString("00000002-0000-1000-8000-008025000000");
	
	/**
	 *  The TerminalIO UART RX credits characteristic UUID.
	 *  This characteristic is used by the central to grant UART credits to the peripheral.
	 *  @return A UUID instance containing the TerminalIO UART credits characteristic UUID.
	 */ 	
	public static final UUID UART_RX_CREDITS_UUID = UUID.fromString("00000003-0000-1000-8000-008025000000");

	/**
	 *  The TerminalIO UART TX credits characteristic UUID.
	 *  This characteristic is used by the peripheral to grant UART credits to the central.
	 *  @return A UUID instance containing the TerminalIO UART credits characteristic UUID.
	 */ 	
	public static final UUID UART_TX_CREDITS_UUID = UUID.fromString("00000004-0000-1000-8000-008025000000");
	
	
	/**
	 * The maximum number of bytes contained within the UART characteristic's value. 
	 */
	public static final int MAX_UART_DATA_SIZE = 20;
	
	/**
	 * The maximum number of UART credits according to the TerminalIO specification.
	 */
	public static final int MAX_UART_CREDITS_COUNT = 255; 	

	
	
	private TIO() { }
}
