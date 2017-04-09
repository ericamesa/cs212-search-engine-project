import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class ThreadSafeSearchIndex {

	/**
	 * Stores a mapping of words to an ArrayList of SearchResults.
	 */
	private final TreeMap<String, ArrayList<SearchResult>> index;
	private final ThreadSafeInvertedIndex invertedIndex;

	/**
	 * Initializes the index.
	 */
	public ThreadSafeSearchIndex(ThreadSafeInvertedIndex invertedIndex) {
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
	public void addFromFile(Path path, Boolean exact, WorkQueue queue) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			String[] words;
			while ((line = br.readLine()) != null) {
				words = WordParser.parseWords(line);
				Arrays.sort(words);
				
				queue.execute(new Task(invertedIndex, index, words, exact));
				
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
	
	
	private static class Task implements Runnable {

		ThreadSafeInvertedIndex invertedIndex;
		TreeMap<String, ArrayList<SearchResult>> index;
		private String[] words;
		private boolean exact;
		
		public Task(ThreadSafeInvertedIndex invertedIndex, TreeMap<String, ArrayList<SearchResult>> index, String[] words, boolean exact) {
			this.invertedIndex = invertedIndex;
			this.index = index;
			this.words = words;
			this.exact = exact;
		}
		
		@Override
		public void run() {
			if (words.length > 0) {
				ArrayList<SearchResult> results = exact ? invertedIndex.exactSearch(words) : invertedIndex.partialSearch(words);
				synchronized (index) {
					index.put(String.join(" ", words), results);
				}
			}
			
		}
	}

}