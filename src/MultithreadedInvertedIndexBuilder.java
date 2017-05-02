import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO Javadoc

/**
 * Builds Inverted Index by going through directories and/or files and adds each word.
 */
public class MultithreadedInvertedIndexBuilder {
	
	Logger logger = LogManager.getLogger();
	/**
	 * Streams through a directory
	 *
	 * @param path
	 *            path to stream through
	 * @param index
	 *            InvertedIndex to add to
	 */
	public static void throughDirectory(Path path, ThreadSafeInvertedIndex index, WorkQueue queue) throws IOException {
		MultithreadedInvertedIndexBuilder builder = new MultithreadedInvertedIndexBuilder(queue);
		builder.start(path, index);
		// TODO queue.finish(); and not in driver
	}

	private final WorkQueue queue;
	
	private MultithreadedInvertedIndexBuilder(WorkQueue queue) {
		this.queue = queue;
	}
	
	private void start(Path path, ThreadSafeInvertedIndex index) throws IOException {
		try (DirectoryStream<Path> directory = Files.newDirectoryStream(path);) {
			for (Path file : directory) {
				if (Files.isDirectory(file)) {
					start(file, index);
				} else {
					queue.execute(new Task(file, index));
				}
			}
			directory.close();
		}
	}
	
	private class Task implements Runnable {

		Path path;
		ThreadSafeInvertedIndex index;
		
		public Task(Path path, ThreadSafeInvertedIndex index) {
			this.path = path;
			this.index = index;
		}
		
		@Override
		public void run() {
			//logger.debug("Starting {}", path);
			String filename = path.toString();
			try {
				InvertedIndexBuilder.throughHTMLFile(path, filename, index);
				
				/* TODO
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.throughHTMLFile(path, filename, local);
				index.addAll(local);
				*/
				
			} catch (IOException e) {
				Thread.currentThread().interrupt();
			}
			//logger.debug("Finsihed {}", path);
		}
		
	}
	
	
}
