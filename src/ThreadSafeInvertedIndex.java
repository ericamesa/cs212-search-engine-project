import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure to store words, their path, and their positions.
 */
public class ThreadSafeInvertedIndex extends InvertedIndex{
	/**
	 * Stores a mapping of words to the positions the words were found.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	private ReadWriteLock lock;

	/**
	 * Initializes the index.
	 */
	public ThreadSafeInvertedIndex() {
		index = new TreeMap<>();
		lock = new ReadWriteLock();
	}

	/**
	 * Adds the word and the file it was found in and the position it was in the
	 * file to the index.
	 *
	 * @param word
	 *            word to clean and add to index
	 * @param position
	 *            position word was found
	 */
	@Override
	public void add(String word, String file, int position) {
		lock.lockReadWrite();

		if (index.get(word) == null) {
			index.put(word, new TreeMap<>());
		} 
		if (index.get(word).get(file) == null) {
			index.get(word).put(file, new TreeSet<>());
		}
		index.get(word).get(file).add(position);

		lock.unlockReadWrite();
	}

	/**
	 * Adds the array of words at once, assuming the first word in the array is
	 * at position 1.
	 *
	 * @param words
	 *            array of words to add
	 *
	 * @see #addAll(String[], int)
	 */
	@Override
	public void addAll(String[] words, String filename) {
		addAll(words, filename, 1);
	}

	/**
	 * Adds the array of words at once, assuming the first word in the array is
	 * at the provided starting position
	 *
	 * @param words
	 *            array of words to add
	 * @param start
	 *            starting position
	 */
	@Override
	public void addAll(String[] words, String filename, int start) {
		int i = start;
		for (String word : words) {
			add(word, filename, i);
			i++;
		}
	}

	/**
	 * Writes index to specified path in JSON format.
	 *
	 * @param path
	 *            path to write to
	 */
	@Override
	public void toJSON(Path path) throws IOException {
		lock.lockReadOnly();
		
		JSONWriter.asDoubleNestedObject(index, path);
		
		lock.unlockReadOnly();
	}

	/**
	 * Returns a string representation of this index.
	 */
	@Override
	public String toString() {
		lock.lockReadOnly();

		try {
			return index.toString();
		} finally {

			lock.unlockReadOnly();
		}
	}

	/**
	 * Checks to see if index contains word given
	 *
	 * @param word
	 *            word to find in index
	 * @return true if index contains word, false otherwise
	 */
	@Override
	public boolean containsWord(String word) {
		lock.lockReadOnly();
		try {
			return index.containsKey(word);
		} finally {
			lock.unlockReadOnly();
		}
		
	}

	/**
	 * Checks to see if index contains path given for the specified word
	 *
	 * @param word
	 *            word to find in index
	 * @param path
	 *            path to find in index
	 * @return true if index contains word, false otherwise
	 */
	@Override
	public boolean containsPath(String word, String path) {
		lock.lockReadOnly();
		try {
			return (containsWord(word) && index.get(word).containsKey(path));
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Checks to see if index contains position given for the specified word and path
	 *
	 * @param word
	 *            word to find in index
	 * @param path
	 *            path to find in index
	 * @param position
	 *            position to find in index
	 * @return true if index contains word, false otherwise
	 */
	@Override
	public boolean containsPosition(String word, String path, Integer position) {
		lock.lockReadOnly();
		try {
			return containsPath(word, path) && index.get(word).get(path).contains(position);
		} finally {
			lock.unlockReadOnly();
		}
	}

	/**
	 * Helper method for exact and partial search, which goes through each path of word in index and adds the 
	 * path, frequency, and initial position of word and if path is already in map, adds to frequency and updates
	 * initial position if necessary.
	 *
	 * @param word
	 *            word to search for
	 * @param resultMap
	 * 			  map to add to or update
	 */
	private void wordSearch(String word, HashMap<String, SearchResult> resultMap, ArrayList<SearchResult> words) {
		for (String path : index.get(word).keySet()) {
			int frequency = index.get(word).get(path).size();
			int initialPosition = index.get(word).get(path).first();

			if (!resultMap.containsKey(path)) {
				resultMap.put(path, new SearchResult(frequency, initialPosition, path));
				words.add(resultMap.get(path));
			}
			else {
				SearchResult searchResult = resultMap.get(path);
				searchResult.addToFrequency(frequency);
				searchResult.updateInitialPosition(initialPosition);
			}
		}
	}

	/**
	 * Searches for exact matches of specified words given and returns a list of SearchResults 
	 *
	 * @param searchWords
	 *            words to find exact searches for
	 * @return ArrayList of SearchResults
	 */
	@Override
	public ArrayList<SearchResult> exactSearch(String[] searchWords) {
		ArrayList<SearchResult> words = new ArrayList<>();
		HashMap<String, SearchResult> resultMap = new HashMap<>();

		lock.lockReadOnly();
		for (String word : searchWords) {
			if (containsWord(word)) {
				wordSearch(word, resultMap, words);
			}
		}
		lock.unlockReadOnly();

		Collections.sort(words);
		return words;
	}

	/**
	 * Searches for partial matches of specified words given and returns a list of SearchResults 
	 *
	 * @param searchWords
	 *            words to find partial searches for
	 * @return ArrayList of SearchResults
	 */
	@Override
	public ArrayList<SearchResult> partialSearch(String[] searchWords) {
		ArrayList<SearchResult> words = new ArrayList<>();
		HashMap<String, SearchResult> resultMap = new HashMap<>();
		lock.lockReadOnly();
		for (String word : searchWords) {
			for (String key : index.tailMap(word, true).navigableKeySet()) {
				if (key.startsWith(word)) {
					wordSearch(key, resultMap, words);
				}
				else {
					break;
				}
			}
		}
		lock.unlockReadOnly();

		Collections.sort(words);
		return words;
	}
	
}









