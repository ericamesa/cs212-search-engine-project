import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadSafeSearchIndex implements SearchIndexInterface {

	/**
	 * Stores a mapping of words to an ArrayList of SearchResults.
	 */
	private final TreeMap<String, ArrayList<SearchResult>> index;
	private final ThreadSafeInvertedIndex invertedIndex;
	private WorkQueue queue;
	private ReadWriteLock lock;
	Logger logger = LogManager.getLogger();

	/**
	 * Initializes the index.
	 */
	public ThreadSafeSearchIndex(ThreadSafeInvertedIndex invertedIndex, WorkQueue queue) {
		index = new TreeMap<>();
		this.invertedIndex = invertedIndex; 
		this.queue = queue;
		lock = new ReadWriteLock();
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
	@Override
	public void addFromFile(Path path, Boolean exact) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = br.readLine()) != null) {
				queue.execute(new Task(line, exact));
			}
		}
		// TODO Call queue.finish() here
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
		JSONWriter.asNestedObject(index, path);
		lock.unlockReadOnly();
	}
	
	private class Task implements Runnable {
		
		private String line;
		private boolean exact;
		
		public Task(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}
		
		@Override
		public void run() {
			logger.debug("Starting {}", line);
			String[] words = WordParser.parseWords(line);
			Arrays.sort(words);
			if (words.length > 0) {
				ArrayList<SearchResult> results = exact ? invertedIndex.exactSearch(words) : invertedIndex.partialSearch(words);
				lock.lockReadWrite();
				// TODO Move the String.join() outside the lock
				index.put(String.join(" ", words), results);
				lock.unlockReadWrite();
			}
			logger.debug("Finished {}", line);
			
		}
	}

	

}
