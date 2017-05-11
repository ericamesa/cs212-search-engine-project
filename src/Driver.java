import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
/**
* Driver class which takes in args and adds words from -path argument to an InvertedIndex and outputs to -index
* argument. Parses through -query argument, if -exact flag is found searches for exact matches in the inverted index 
* else searches for partial matches and outputs to -results argument.
*/

public class Driver {

	/**
	 * Adds words from -path argument from args to an InvertedIndex and outputs to -index argument from args. Parses through 
	 * -query argument and adds either exact or partial matches, depending on whether a -exact flag was provided, to a SearchIndex
	 * and outputs to -results arguement. 
	 * 
	 * @param args
	 * 			  command line arguments
	 */
	public static void main(String[] args) {
		ArgumentMap argumentMap = new ArgumentMap(args);
		InvertedIndex index = null;
		SearchIndexInterface searchIndex = null;
		WorkQueue queue = null;
		WebCrawler crawler = null;
		Logger logger = LogManager.getLogger();
		
		
		if (argumentMap.hasFlag("-threads") || argumentMap.hasFlag("-url") || argumentMap.hasFlag("-port")) {
			ThreadSafeInvertedIndex threadSafeIndex = new ThreadSafeInvertedIndex();
			index = threadSafeIndex;
			
			int num = argumentMap.getInteger("-threads", 5);
			if (num <= 0) {
				num = 5;
			}
			queue = new WorkQueue(num);
			searchIndex = new ThreadSafeSearchIndex(threadSafeIndex, queue);
			
			if (argumentMap.hasFlag("-url")) {
				crawler = new WebCrawler(threadSafeIndex);
			}
		}
		else {
			index = new InvertedIndex();
			searchIndex = new SearchIndex(index);
		}
		
		if (crawler != null) {
			if (!argumentMap.hasValue("-url")) {
				System.out.println("No Seed Provided");
				return;
			}
			else {
				int limit = argumentMap.getInteger("-limit", 50);
				URL seed;
				try {
					seed = new URL(argumentMap.getString("-url"));
					crawler.crawl(seed, limit);
				} catch (MalformedURLException e) {
					System.out.println("Could not create URL from seed provided");
				}
			}
		} 

		if (argumentMap.hasFlag("-path")) {
			if (!argumentMap.hasValue("-path")) {
				System.out.println("No Path Provided");
				return;
			}
			else {
				String input = argumentMap.getString("-path");
				Path path = Paths.get(input);
				try {
					if (queue != null) {
						if (Files.isDirectory(path)) {
							ThreadSafeInvertedIndex threadSafeIndex = (ThreadSafeInvertedIndex) index;
							MultithreadedInvertedIndexBuilder.throughDirectory(path, threadSafeIndex, queue);
						} else {
							InvertedIndexBuilder.throughHTMLFile(path, input, index);
						}
					}
					else {
						if (Files.isDirectory(path)) {
							InvertedIndexBuilder.throughDirectory(path, index);
						} else {
							InvertedIndexBuilder.throughHTMLFile(path, input, index);
						}
					}
					
				} catch (IOException e) {
					System.out.println("The path you provided could not be read through.");
					return;
				}
			}
		}
		
		if (argumentMap.hasFlag("-port")) {
			final int PORT = 8080;
			
			Server server = new Server(PORT);

			ServletHandler handler = new ServletHandler();
			handler.addServletWithMapping(new ServletHolder(new SearchServlet(index)), "/");

			server.setHandler(handler);
			try {
				server.start();
				server.join();
			} catch (Exception e) {
				System.out.println("Problem occured with Server");
			}
		}

		if (argumentMap.hasFlag("-index")) {
			String output = argumentMap.getString("-index", "index.json");
			Path outputPath = Paths.get(output);
			logger.debug("outputing to JSON");
			try {
				index.toJSON(outputPath);

			} catch (IOException e) {
				System.out.println("Could not write to file.");
			}
		}
		
		if (argumentMap.hasFlag("-query")) {
			if (!argumentMap.hasValue("-query")) {
				System.out.println("No Query Provided");
				return;
			}
			else {
				String input = argumentMap.getString("-query");
				Path path = Paths.get(input);
				
				try {
					searchIndex.addFromFile(path, argumentMap.hasFlag("-exact"));
					
				} catch (IOException e) {
					System.out.println("The path you provided could not be read through.");
					return;
				}
			}
		}
		
		if (argumentMap.hasFlag("-results")) {
			String output = argumentMap.getString("-results", "results.json");
			Path path = Paths.get(output);
			logger.debug("Outputing to JSON");
			try {
					searchIndex.toJSON(path);
				
			} catch (IOException e) {
				System.out.println("Could not write to file.");
			}
		}
		
		if (queue != null) {
			queue.shutdown();
		}
		
	}

}
