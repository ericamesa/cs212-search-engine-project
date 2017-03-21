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

	/**
	 * Initializes the index.
	 */
	public SearchIndex() {
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
	public void addFromFile(Path path, InvertedIndex invertedIndex, Boolean exact) throws IOException {
		HashSet<String[]> set = new HashSet<>();
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			String[] words;
			while ((line = br.readLine()) != null) {
				words = WordParser.parseWords(line);
				Arrays.sort(words);
				
				// TODO Can reduce loops and storage using this:
				// ArrayList<SearchResult> results = exact ? invertedIndex.exactSearch(words) : invertedIndex.partialSearch(words); 
				//index.put(String.join(" ", words), results);
				
				set.add(words);
			}
		}
		
		ArrayList<SearchResult> results;
		if (exact){
			for (String[] elements : set) {
				StringBuilder query = new StringBuilder();
				int i = elements.length;
				for (String element : elements) {
					query.append(element);
					if (i > 1){
						query.append(" ");
					}
					i--;
				}
				if (elements.length > 0) {
					results = invertedIndex.exactSearch(elements);
					index.put(query.toString(), results);
				}
			}
		}
		else {
			for (String[] elements : set) {
				StringBuilder query = new StringBuilder();
				int i = elements.length;
				for (String element : elements) {
					query.append(element);
					if (i > 1){
						query.append(" ");
					}
					i--;
				}
				if (elements.length > 0) {
					results = invertedIndex.partialSearch(elements);
					index.put(query.toString(), results);
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
