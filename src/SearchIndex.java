import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * Data structure to store words and their SearchResults.
 */
public class SearchIndex implements SearchIndexInterface {

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
	
	@Override
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

	@Override
	public void toJSON(Path path) throws IOException {
		JSONWriter.asNestedObject(index, path);
	}
}
