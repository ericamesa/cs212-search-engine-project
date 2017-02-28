import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Writes to specified file in JSON format
 */
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

	/**
	 * Writes a DoubleNestedObject to file in JSON format
	 *
	 * @param index
	 *            double nested object to write to file
	 * @param path
	 *            path to write to
	 */
	public static void asDoubleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write("{");
			writer.flush();
			writer.newLine();
			for (String word : index.keySet()) {
				writer.write(indent(1) + quote(word) + ": {");
				writer.flush();
				writer.newLine();
				TreeMap<String, TreeSet<Integer>> files = index.get(word);
				for (String file : files.keySet()) {
					writer.write(indent(2) + quote(file) + ": [");
					writer.flush();
					writer.newLine();
					TreeSet<Integer> positions = files.get(file);
					for (Integer position : positions) {
						writer.write(indent(3) + position.toString());

						if (positions.last() != position) {
							writer.write(",");
						}
						writer.flush();
						writer.newLine();
					}
					writer.write(indent(2) + "]");
					if (!files.lastKey().equals(file)) {
						writer.write(",");
					}
					writer.flush();
					writer.newLine();
				}
				writer.write(indent(1) + "}");
				if (!index.lastKey().equals(word)) {
					writer.write(",");
				}
				writer.flush();
				writer.newLine();
			}
			writer.write("}");
			writer.flush();
			writer.newLine();
			writer.close();
		}
	}
	
	/**
	 * Writes the set of elements as a JSON array to the path using UTF8.
	 *
	 * @param elements
	 *            elements to write as a JSON array
	 * @param path
	 *            path to write file
	 * @throws IOException
	 */
	public static void asArray(TreeSet<Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write("[");
			writer.newLine();
			int i = 1;
			for (Integer element : elements) {
				writer.write(indent(1) + element.toString());
				
				if (i < elements.size()){
					writer.write(",");
				}
				i++;
				writer.flush();
				writer.newLine();
			}
			writer.write("]");
			writer.close();
		}
	}
}
