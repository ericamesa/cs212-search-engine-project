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
	
	@Override
	public void addFromFile(Path path, Boolean exact) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = br.readLine()) != null) {
				queue.execute(new Task(line, exact));
			}
		}
		queue.finish();
	}
	
	@Override
	public void toJSON(Path path) throws IOException {
		lock.lockReadOnly();
		JSONWriter.asNestedObject(index, path);
		lock.unlockReadOnly();
	}
	
	/*
	 * Runnable task that finds and adds search results to a SearchIndex
	 */
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
				String stringwords = String.join(" ", words);
				lock.lockReadWrite();
				index.put(stringwords, results);
				lock.unlockReadWrite();
			}
			logger.debug("Finished {}", line);
		}
	}

	

}
