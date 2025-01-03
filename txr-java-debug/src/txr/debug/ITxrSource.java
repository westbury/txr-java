package txr.debug;

public interface ITxrSource {

	boolean isEditable();

	/**
	 * The implementation should cache the lines only if
	 * the TXR resource is immutable.
	 * 
	 * @return
	 */
	String[] readLines();

	void writeChanges(String[] txrLines);

}
