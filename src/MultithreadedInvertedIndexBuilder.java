import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builds Inverted Index by going through directories and/or files and adds each word.
 */
public class MultithreadedInvertedIndexBuilder {
	
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
					queue.execute(new Task(path, index));
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
			String filename = path.toString();
			try {
				InvertedIndexBuilder.throughHTMLFile(path, filename, index);
			} catch (IOException e) {
				Thread.currentThread().interrupt();
			}
		}
		
	}
	
	
}
