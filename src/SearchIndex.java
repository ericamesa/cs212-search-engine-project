import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Data structure to store words and their SearchResults.
 */
public class SearchIndex {

	/**
	 * Stores a mapping of words to an ArrayList of SearchResults.
	 */
	private final TreeMap<String, ArrayList<SearchResult>> index;
	// TODO private final InvertedIndex invertedIndex;

	/**
	 * Initializes the index.
	 */
	public SearchIndex() { // TODO init invertedIndex here
		index = new TreeMap<>();
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
	public void addFromFile(Path path, InvertedIndex invertedIndex, Boolean exact) throws IOException { // TODO Remove invertedIndex from here
		HashSet<String[]> set = new HashSet<>(); // TODO Remove
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
