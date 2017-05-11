/**
 * Data structure to store the frequency, initialPosition and path of a word.
 */
public class SearchResult implements Comparable<SearchResult> {

	/**
	 * Stores the frequency, initialPosition and path.
	 */
	private int frequency;
	private int initialPosition;
	public final String path;
	
	/**
	 * Initializes the frequency, initialPosition, and path.
	 */
	public SearchResult(int frequency, int initialPosition, String path) {
		this.frequency = frequency;
		this.initialPosition = initialPosition; 
		this.path = path;
	}
	
	/**
	 * Getter for frequency.
	 *
	 * @return frequency
	 */
	public int frequency() {
		return frequency;
	}
	
	/**
	 * Getter for initial position.
	 *
	 * @return initial position
	 */
	public int initialPosition() {
		return initialPosition;
	}
	
	public String path() {
		return path;
	}
	
	/**
	 * Adds to frequency specified value.
	 *
	 * @param add
	 * 			  number to add to frequency
	 */
	public void addToFrequency(int add) {
		frequency += add;
	}
	
	/**
	 * Updates initialPosition to specified value, if value is smaller than 
	 * current initial position
	 *
	 * @param position
	 * 			  value to compare to initial position
	 */
	public void updateInitialPosition(int position) {
		if (initialPosition > position) {
			initialPosition = position;
		}
	}
	
	/**
	 * Returns whether or not current path is equal to path given.
	 *
	 * @param path
	 * 			  path to compare to current path
	 * @return true if current path is equal to path given, false otherwise
	 */
	public boolean hasPath(String path) {
		if (this.path.equals(path)) {
			return true;
		}
		return false;
	}
	
	/**
	 * By default, {@link #SearchResult} objects will be sorted first by their frequency, 
	 * then their initial position, and lastly their path.
	 */
	@Override
	public int compareTo(SearchResult other) {
		if (Integer.compare(other.frequency, frequency) == 0) {
			if (Integer.compare(initialPosition, other.initialPosition) == 0) {
				return path.compareTo(other.path);
			}
			return Integer.compare(initialPosition, other.initialPosition);
		}
		return Integer.compare(other.frequency, frequency);
	}
	
	@Override
	public String toString() {
		return "Path: " + path + ", Frequency: " + frequency + ", Initial position: " + initialPosition; 
	}
}
