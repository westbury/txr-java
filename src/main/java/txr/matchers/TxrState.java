package txr.matchers;

// The state is held by the client because the server is stateless.
// However this is a black-box at the client end. Its only use is to send back to the server.
public class TxrState {
	
	public TxrState() {
		collectStates = new CollectState[0];
	}
	
	public class CollectState {
		public CollectState(int txrLineNumber, int dataLineNumber, boolean showExtraUnmatched) {
			this.txrLineNumber = txrLineNumber;
			this.dataLineNumber = dataLineNumber;
			this.showExtraUnmatched = showExtraUnmatched;
			
		}
		int txrLineNumber;
		int dataLineNumber;
		
		// If this flag is on, it means the user is expecting one or more collect matches
		// after the last collect match. The server will show the best failing match.
		boolean showExtraUnmatched;
	}
	
	// Only one of these can exist for a given data line.
	CollectState[] collectStates;
}