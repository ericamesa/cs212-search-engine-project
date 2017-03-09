public class SearchResult implements Comparable<SearchResult> {

	private int frequency;
	private int initialPosition;
	public final String path;
	
	public SearchResult(int frequency, int initialPosition, String path) {
		this.frequency = frequency;
		this.initialPosition = initialPosition; 
		this.path = path;
	}
	
	public int frequency() {
		return frequency;
	}
	
	public int initialPosition() {
		return initialPosition;
	}
	
	public void addToFrequency(int add) {
		frequency += add;
	}
	
	public void updateInitialPosition(int position) {
		if (initialPosition > position) {
			initialPosition = position;
		}
	}
	
	public boolean hasPath(String path) {
		if (this.path.equals(path)) {
			return true;
		}
		return false;
	}
	
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
}
