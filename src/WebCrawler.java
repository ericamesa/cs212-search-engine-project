import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebCrawler {
	private final ThreadSafeInvertedIndex index;
	private final WorkQueue queue;
	private final HashSet<URL> urls;
	private int max;
	Logger logger = LogManager.getLogger();
	
	public WebCrawler(ThreadSafeInvertedIndex index) {
		this.index = index;
		queue = new WorkQueue();
		urls = new HashSet<>();
		max = 50;
	}
	
	/**
	 * Starts the web crawl process
	 * 
	 * @param seed
	 * 			URL to starts the crawl with
	 * @param limit
	 * 			the limit to the number of URLs to crawl
	 */
	public void crawl(URL seed, int limit) {
		max = limit;
		urls.add(seed);
		
		queue.execute(new Task(seed));
		
		queue.finish();
		
	}
	
	/*
	 * Runnable task that goes through each URL in given seed and assign them each a task, which 
	 * adds each word from seed to InvertedIndex
	 */
	public class Task implements Runnable {

		URL seed;
		
		public Task(URL seed) {
			this.seed = seed;
		}
		
		@Override
		public void run() {
			String html = LinkParser.fetchHTML(seed);
			try {
				for (URL url : LinkParser.listLinks(seed, html)) {
					synchronized (urls) {
						if (urls.size() >= max) {
							break;
						}
						if (!urls.contains(url)) {
							urls.add(url);
							queue.execute(new Task(url));
						}
					}
				}
			} catch (MalformedURLException e) {
				logger.debug("Could not fetch links for {}", seed);
			}
			
			html = HTMLCleaner.stripHTML(html);
			String[] words = WordParser.parseWords(html);
			InvertedIndex local = new InvertedIndex();
			local.addAll(words, seed.toString());
			index.addAll(local);
			
		}
		
	}
	
	
	
	
}
