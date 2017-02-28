import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure to store strings and their positions.
 */
public class InvertedIndex {
	/**
	 * Stores a mapping of words to the positions the words were found.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Initializes the index.
	 */
	public InvertedIndex() {
		index = new TreeMap<>();
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
	public void add(String word, String file, int position) {
		if (index.get(word) == null) {
			index.put(word, new TreeMap<>());
		} 
		if (index.get(word).get(file) == null) {
			index.get(word).put(file, new TreeSet<>());
		}
		index.get(word).get(file).add(position);
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
	public void addAll(String[] words, String filename, int start) {
		int i = start;
		for (String word : words) {
			add(word, filename, i);
			i++;
		}
	}

	// TOODO Javadoc
	public void toJSON(Path path) throws IOException {
		JSONWriter.asDoubleNestedObject(index, path);
	}

	/**
	 * Returns a string representation of this index.
	 */
	@Override
	public String toString() {
		return index.toString();
	}
	
	// TODO containsWord(String word)
	// TODO containsPath(String word, String path)
	// TODO etc...
	
	
}
