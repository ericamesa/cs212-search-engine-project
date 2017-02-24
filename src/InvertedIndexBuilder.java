import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvertedIndexBuilder {

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

	public static void throughHTMLFile(Path path, String filename, InvertedIndex index) throws IOException {
		if (filename.endsWith(".html") || filename.endsWith(".htm") || filename.endsWith(".HTML")) {
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
