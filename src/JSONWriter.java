import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

public class JSONWriter {

	/**
	 * Returns a String with the specified number of tab characters.
	 *
	 * @param times
	 *            number of tab characters to include
	 * @return tab characters repeated the specified number of times
	 */
	public static String indent(int times) {
		char[] tabs = new char[times];
		Arrays.fill(tabs, '\t');
		return String.valueOf(tabs);
	}

	/**
	 * Returns a quoted version of the provided text.
	 *
	 * @param text
	 *            text to surround in quotes
	 * @return text surrounded by quotes
	 */
	public static String quote(String text) {
		return String.format("\"%s\"", text);
	}

	public static void asDoubleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write("{");
			writer.flush();
			writer.newLine();
			int i = 1;
			for (String word : index.keySet()) {
				writer.write(indent(1) + quote(word) + ": {");
				writer.flush();
				writer.newLine();
				TreeMap<String, TreeSet<Integer>> files = index.get(word);
				int j = 1;
				for (String file : files.keySet()) {
					writer.write(indent(2) + quote(file) + ": [");
					writer.flush();
					writer.newLine();
					TreeSet<Integer> positions = files.get(file);
					int k = 1;
					for (Integer position : positions) {
						writer.write(indent(3) + position.toString());

						if (k < positions.size()) {
							writer.write(",");
						}
						k++;
						writer.flush();
						writer.newLine();
					}
					writer.write(indent(2) + "]");
					if (j < files.size()) {
						writer.write(",");
					}
					j++;
					writer.flush();
					writer.newLine();
				}
				writer.write(indent(1) + "}");
				if (i < index.size()) {
					writer.write(",");
				}
				i++;
				writer.flush();
				writer.newLine();
			}
			writer.write("}");
			writer.flush();
			writer.newLine();
			writer.close();
		}
	}
}
