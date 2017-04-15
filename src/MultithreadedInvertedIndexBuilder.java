import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	/**
	 * Goes through specified HTML file and adds each word to index
	 *
	 * @param path
	 *            path to stream through
	 * @param filename
	 *            name of file to add to index
	 * @param index
	 *            InvertedIndex to add to
	 */
	public static void throughHTMLFile(Path path, String filename, ThreadSafeInvertedIndex index) throws IOException {
		Pattern p = Pattern.compile("(?i)\\.html?$");
		Matcher m = p.matcher(filename);
		if (m.find()) {
			StringBuilder wholeFile = new StringBuilder();
			try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				String line;
				String[] words;
				while ((line = br.readLine()) != null) {
					wholeFile.append(line + " ");
				}
				String file = wholeFile.toString();
				file = HTMLCleaner.stripHTML(file);
				words = WordParser.parseWords(file);
				index.addAll(words, filename);
			}
		}
	}

	private final WorkQueue queue;
	
	private MultithreadedInvertedIndexBuilder(WorkQueue queue) {
		this.queue = queue;
	}
	
	private void start(Path path, ThreadSafeInvertedIndex index) {
		queue.execute(new Task(path, index));
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
			try (DirectoryStream<Path> directory = Files.newDirectoryStream(path);) {
				for (Path file : directory) {
					if (Files.isDirectory(file)) {
						queue.execute(new Task(file, index));
					} else {
						String filename = file.toString();
						throughHTMLFile(file, filename, index);
					}
				}
				directory.close();
				
			} catch (IOException e) {
				System.out.println("The path you provided could not be read through.");
			}
			
			
		}
		
	}
	
	
}
