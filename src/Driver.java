import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

// TODO Address and remove old TODO comments

// TODO Anything that is generally useful should be a separate class
// TODO Only Driver should be project-specific

public class Driver {
	
	// TODO Can't throw exceptions from main, can throw EVERYWHERE else
	public static void main(String[] args) throws IOException {

		ArgumentMap arguMap = new ArgumentMap(args);
		
		InvertedIndex index = new InvertedIndex();
		
		String defaultPath = "index.json";
		String input = arguMap.getString("-path", "NoFlag", "NoValue");
		Path path = Paths.get(input);
		String output = arguMap.getString("-index", "NoFlag", defaultPath);
		
		if (input.equals("NoValue")) {
			System.out.println("No Path Provided");
		}
		else if (input.equals("NoFlag")) {
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(defaultPath), StandardCharsets.UTF_8)) {
				writer.write("");
				writer.close();
			}
			catch (IOException e){
				// TODO Never do this! 
			}
		}
		else {
			if (Files.isDirectory(path)){
				// TODO InvertedIndexBuilder.throughDirectory();
				// TODO rename the method to reflect that it is parsing through HTML files
				throughDirectory(path, index);
			}
			else {
				throughFile(path, input, index);

			}
			
			if (!output.equals("")){
				outputFile(index, output);
			}
		}

	}
	
	// TODO Create some sort of builder class
	
	public static void throughDirectory(Path path, InvertedIndex index) throws IOException{
		try (DirectoryStream<Path> directory = Files.newDirectoryStream(path);){
			for (Path file: directory) {
				if (Files.isDirectory(file)){
					throughDirectory(file, index);
				}
				else{
					
					String filename = file.toString();
					throughFile(file, filename, index);
				}
			}
			directory.close();
		}
	}
	
	public static void throughFile(Path path, String filename, InvertedIndex index) throws IOException {
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
	
	// TODO Consider adding back JSONWRiter and creating a method
	// TODO asDoubleNestedObject(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, Path path)
	public static void outputFile(InvertedIndex index, String output) throws IOException{
		Path path = Paths.get(output);
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			TreeMap<String, TreeMap<String, TreeSet<Integer>>> words = index.getMap();
			writer.write("{");
			writer.flush();
			writer.newLine();
			int i = 1;
			for (String word : words.keySet()) {
				writer.write(indent(1) + quote(word) + ": {");
				writer.flush();
				writer.newLine();
				TreeMap<String, TreeSet<Integer>> files = words.get(word);
				int j = 1;
				for (String file : files.keySet()) {
					writer.write(indent(2) + quote(file) + ": [");
					writer.flush();
					writer.newLine();
					TreeSet<Integer> positions = files.get(file);
					int k = 1;
					for (Integer position : positions) {
						writer.write(indent(3) + position.toString());
						
						if (k < positions.size()){
							writer.write(",");
						}
						k++;
						writer.flush();
						writer.newLine();
					}
					writer.write(indent(2) + "]");
					if (j < files.size()){
						writer.write(",");
					}
					j++;
					writer.flush();
					writer.newLine();
				}
				writer.write(indent(1) + "}");
				if (i < words.size()){
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
}


