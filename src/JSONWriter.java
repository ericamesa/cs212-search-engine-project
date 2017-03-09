import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
	 * Writes the set of elements as a JSON array at the specified indent level.
	 *
	 * @param writer
	 *            writer to use for output
	 * @param elements
	 *            elements to write as JSON array
	 * @param level
	 *            number of times to indent the array itself
	 * @throws IOException
	 */
	private static void asArray(BufferedWriter writer, TreeSet<Integer> elements, int level) throws IOException {
		writer.write("[");
		writer.newLine();
		for (Integer element : elements) {
			writer.write(indent(level) + element.toString());
			
			if (elements.last() != element) {
				writer.write(",");
			}
			writer.flush();
			writer.newLine();
		}
		writer.write(indent(level - 1) + "]");
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
					writer.write(indent(2) + quote(file) + ": ");
					asArray(writer, files.get(file), 3);
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
	
	public static void asNestedObject(TreeMap<String, ArrayList<SearchResult>> index, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writer.write("[");
			writer.flush();
			writer.newLine();
			for (String query : index.keySet()) {
				writer.write(indent(1) + "{");
				writer.flush();
				writer.newLine();
				writer.write(indent(2) + quote("queries") + ": " + quote(query) + ",");
				writer.newLine();
				writer.write(indent(2) + quote("results") + ": [");
				writer.newLine();
				int i = index.get(query).size();
				for (SearchResult result : index.get(query)) {
					writer.write(indent(3) + "{");
					writer.flush();
					writer.newLine();
					
					writer.write(indent(4) + quote("where") + ": "+ quote(result.path) + ",");
					writer.newLine();
					
					writer.write(indent(4) + quote("count") + ": " + result.frequency() + ",");
					writer.newLine();
					
					writer.write(indent(4) + quote("index") + ": " + result.initialPosition());
					writer.newLine();
					
					writer.write(indent(3) + "}");
					if (i > 1) {
						writer.write(",");
					}
					writer.flush();
					writer.newLine();
					i--;
				}
				
				writer.write(indent(2) + "]");
				writer.newLine();
				writer.write(indent(1) + "}");
				if (!query.equals(index.lastKey())) {
					writer.write(",");
				}
				writer.flush();
				writer.newLine();
			}
			writer.write("]");
			writer.flush();
			writer.newLine();
			writer.close();
		}
	}
}




