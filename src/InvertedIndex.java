import java.util.TreeSet;
import java.util.TreeMap;

/**
 * Data structure to store strings and their positions.
 */
public class InvertedIndex {

	// TODO final
	/**
	 * Stores a mapping of words to the positions the words were found.
	 */
	private TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Initializes the index.
	 */
	public InvertedIndex() {
		index = new TreeMap<>();
	}

	/**
	 * Adds the word and the file it was found in and the position it was in the file to the index.
	 *
	 * @param word
	 *            word to clean and add to index
	 * @param position
	 *            position word was found
	 */
	public void add(String word, String file, int position) {
		
		// TODO Could reduce to less lines of code
		if (index.get(word) == null){
			TreeMap<String, TreeSet<Integer>> map = new TreeMap<>();
			TreeSet<Integer> positions = new TreeSet<>();
			positions.add(position);
			map.put(file, positions);
			index.put(word, map);
		}
		else {
			if (index.get(word).get(file) == null){
				TreeSet<Integer> positions = new TreeSet<>();
				positions.add(position);
				index.get(word).put(file, positions);
			}
			else {
				if (!index.get(word).get(file).contains(position)){
					index.get(word).get(file).add(position);
				}
			}
		}	

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
	
	// TODO Breaks encapsulation
	public TreeMap<String, TreeMap<String, TreeSet<Integer>>> getMap() {
		return index;
	}
	
	// TODO Add this
	/*
	public void toJSON(Path path) {
		JSONWriter.asDoubleNestedObject(index, path);
	}*/
	

	/**
	 * Returns a string representation of this index.
	 */
	@Override
	public String toString() {
		return index.toString();
	}
}
