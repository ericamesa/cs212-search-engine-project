import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure to store words, their path, and their positions.
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

	/**
	 * Writes index to specified path in JSON format.
	 *
	 * @param path
	 *            path to write to
	 */
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
	
	/**
	 * Checks to see if index contains word given
	 *
	 * @param word
	 *            word to find in index
	 * @return true if index contains word, false otherwise
	 */
	public boolean containsWord(String word) {
		return index.containsKey(word);
	}
	
	/**
	 * Searches for partial matches of specified word given and a list of words 
	 *
	 * @param prefix
	 *            word to find partial searches for
	 * @return ArrayList of words
	 */
	public ArrayList<String> containsPartialWord(String prefix) {
		ArrayList<String> list = new ArrayList<>();
		for (String word : index.keySet()) {
			if (word.startsWith(prefix)) {
				list.add(word);
			}
		}
		return list;
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
	public boolean containsPath(String word, String path) {
		return (containsWord(word) && index.get(word).containsKey(path));
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
	public boolean containsPosition(String word, String path, Integer position) {
		return containsPath(word, path) && index.get(word).get(path).contains(position);
	}

	/**
	 * Searches for exact matches of specified words given and returns a list of SearchResults 
	 *
	 * @param searchWords
	 *            words to find exact searches for
	 * @return ArrayList of SearchResults
	 */
	public ArrayList<SearchResult> exactSearch(String[] searchWords) {
		// TODO rename this?
		ArrayList<SearchResult> foundWords = new ArrayList<>();
		
		// TODO HashMap<String, SearchResult> resultMap;
		
		for (String word : searchWords) {
			if (containsWord(word)) {
				for (String path : index.get(word).keySet()){
					boolean containsAlready = false;
					int frequency = index.get(word).get(path).size();
					int initialPosition = index.get(word).get(path).first();
					
					// TODO Linear search always means there is a better way
					
					/*
					 * Hey, resultMap, is this path a key already?
					 * If yes, get the result associated with that key, and update
					 * Else... add the path, result pair to the map AND the result to the list
					 */
					
					SearchResult searchResult;
					for (SearchResult result : foundWords) {
						if (result.hasPath(path)) {
							containsAlready = true;
							result.addToFrequency(frequency);
							result.updateInitialPosition(initialPosition);
							continue;
						}
					}
					if (!containsAlready) {
						searchResult = new SearchResult(frequency, initialPosition, path);
						foundWords.add(searchResult);
					}
				}
				
			}
		}
		
		// foundWords.addAll(resultMap.values());
		
		Collections.sort(foundWords);
		return foundWords;
	}
	
	// TODO Try to create a private helper method with the code common to both search  methods

	/**
	 * Searches for partial matches of specified words given and returns a list of SearchResults 
	 *
	 * @param searchWords
	 *            words to find partial searches for
	 * @return ArrayList of SearchResults
	 */
	public ArrayList<SearchResult> partialSearch(String[] searchWords) {
		ArrayList<SearchResult> foundWords = new ArrayList<>();
		for (String word : searchWords) {
			ArrayList<String> fullWords = containsPartialWord(word);
			
			// TODO Adopt this: https://github.com/usf-cs212-2017/lectures/blob/master/Data%20Structures/src/FindDemo.java#L144
			// for maps instead of sets
			/*(for (String key : index.keySet()) {
				if (key.startsWith(query)) {
					
				}
			}*/
			
			for (String fullWord : fullWords) {
				for (String path : index.get(fullWord).keySet()){
					boolean containsAlready = false;
					int frequency = index.get(fullWord).get(path).size();
					int initialPosition = index.get(fullWord).get(path).first();
					SearchResult searchResult;
					for (SearchResult result : foundWords) {
						if (result.hasPath(path)) {
							containsAlready = true;
							result.addToFrequency(frequency);
							result.updateInitialPosition(initialPosition);
						}
					}
					if (!containsAlready) {
						searchResult = new SearchResult(frequency, initialPosition, path);
						foundWords.add(searchResult);
					}
				}
			}
		}
		Collections.sort(foundWords);
		return foundWords;
	}
	
}





