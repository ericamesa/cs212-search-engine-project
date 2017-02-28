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
public class InvertedIndexBuilder {

	/**
	 * Streams through a directory
	 *
	 * @param path
	 *            path to stream through
	 * @param index
	 *            InvertedIndex to add to
	 */
	public static void throughDirectory(Path path, InvertedIndex index) throws IOException {
		try (DirectoryStream<Path> directory = Files.newDirectoryStream(path);) {
			for (Path file : directory) {
				if (Files.isDirectory(file)) {
					throughDirectory(file, index);
				} else {

					String filename = file.toString();
					throughHTMLFile(file, filename, index);
				}
			}
			directory.close();
		}
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
	public static void throughHTMLFile(Path path, String filename, InvertedIndex index) throws IOException {
		Pattern p = Pattern.compile("(?i)\\.html?$");
		Matcher m = p.matcher(filename);
		if (m.find()) {
			String wholeFile = "";
			try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				String line;
				String[] words;
				while ((line = br.readLine()) != null) {
					wholeFile += line + " ";
				}
				wholeFile = HTMLCleaner.stripHTML(wholeFile);
				words = WordParser.parseWords(wholeFile);
				index.addAll(words, filename);
			}
		}
	}
}
