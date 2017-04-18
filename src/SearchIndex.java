import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/*
 * TODO
 * Create a SearchIndexInterface
 * toJSON() method
 * addFromFile() method
 * 
 * Implement this interface in both SearchIndex and ThreadSafeSearchIndex
 */

/**
 * Data structure to store words and their SearchResults.
 */
public class SearchIndex {

	/**
	 * Stores a mapping of words to an ArrayList of SearchResults.
	 */
	private final TreeMap<String, ArrayList<SearchResult>> index;
	private final InvertedIndex invertedIndex;

	/**
	 * Initializes the index.
	 */
	public SearchIndex(InvertedIndex invertedIndex) {
		index = new TreeMap<>();
		this.invertedIndex = invertedIndex;
	}
	
	/**
	 * Adds words from given file to index and an ArrayList of its SearchResults.
	 *
	 * @param path
	 *            path to read words from
	 * @param invertedIndex
	 *            index to search through
	 * @param exact
	 *            true if searching for exact matches, false to search for partial matches
	 */
	public void addFromFile(Path path, Boolean exact) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			String[] words;
			while ((line = br.readLine()) != null) {
				words = WordParser.parseWords(line);
				Arrays.sort(words);
				
				if (words.length > 0) {
					ArrayList<SearchResult> results = exact ? invertedIndex.exactSearch(words) : invertedIndex.partialSearch(words); 
					index.put(String.join(" ", words), results);
				}
				
			}
		}
		
	}
	
	/**
	 * Writes index to specified path in JSON format.
	 *
	 * @param path
	 *            path to write to
	 */
	public void toJSON(Path path) throws IOException {
		JSONWriter.asNestedObject(index, path);
	}
}
