package txr.matchers;

// The state is held by the client because the server is stateless.
// However this is a black-box at the client end. Its only use is to send back to the server.
public class TxrState {
	
	public TxrState() {
		lineStates = new LineState[0];
	}
	
	public class LineState {
		public LineState(int txrLineNumber, int dataLineNumber) {
			this.txrLineNumber = txrLineNumber;
			this.dataLineNumber = dataLineNumber;
			this.showExtraUnmatched = false;
			this.showFailingMaybe = false;
			this.showAllFailuresInNone = false;
			
		}
		int txrLineNumber;
		int dataLineNumber;
		
		// If this flag is on, it means the user is expecting one or more collect matches
		// after the last collect match. The server will show the best failing match.
		boolean showExtraUnmatched;
		
		// If this flag is on, it means the user is expecting a maybe, or more specifically, one
		// of the clauses in a maybe to be a match.
		boolean showFailingMaybe;
		
		// If this flag is on, it means the user is expecting a matching @(none) to fail to match. In order words,
		// the user expects one of the sub-clauses to match when in fact none matched.
		boolean showAllFailuresInNone;
	}
	
	// Only one of these can exist for a given data line.
	LineState[] lineStates;
}