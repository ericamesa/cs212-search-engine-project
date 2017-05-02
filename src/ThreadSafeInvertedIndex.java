import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Data structure to store words, their path, and their positions.
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	/**
	 * Stores a mapping of words to the positions the words were found.
	 */
	private ReadWriteLock lock;
	
	/**
	 * Initializes the index.
	 */
	public ThreadSafeInvertedIndex() {
		super();
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

		super.add(word, file, position);

		lock.unlockReadWrite();
	}

	// TODO Remove method
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
		lock.lockReadWrite();
		super.addAll(words, filename, 1);
		lock.unlockReadWrite();
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
	public void addAll(String[] words, String filename, int start)  {
		lock.lockReadWrite();
		
		super.addAll(words, filename, start);
		
		lock.unlockReadWrite();
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
		
		super.toJSON(path);
		
		lock.unlockReadOnly();
	}

	/**
	 * Returns a string representation of this index.
	 */
	@Override
	public String toString() {
		lock.lockReadOnly();

		try {
			return super.toString();
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
			return super.containsWord(word);
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
			return super.containsPath(word, path);
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
			return super.containsPosition(word, path, position);
		} finally {
			lock.unlockReadOnly();
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
		lock.lockReadOnly();
		try {
			return super.exactSearch(searchWords);
		}
		finally {
			lock.unlockReadOnly();
		}
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
		lock.lockReadOnly();
		try {
			return super.partialSearch(searchWords);
		}
		finally {
			lock.unlockReadOnly();
		}
	}
	
}