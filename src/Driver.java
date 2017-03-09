import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;

/**
* Driver class which takes in args and adds words from -path argument to an InvertedIndex and outputs to -index
* argument
*/

public class Driver {

	/**
	 * Adds words from -path argument from args to an InvertedIndex and outputs to -index argument from args
	 * 
	 * @param args
	 * 			  command line arguments
	 */
	public static void main(String[] args) {

		ArgumentMap argumentMap = new ArgumentMap(args);
		InvertedIndex index = new InvertedIndex();
		TreeMap<String, ArrayList<SearchResult>> searchResults = new TreeMap<>();

		if (argumentMap.hasFlag("-path")) {
			if (!argumentMap.hasValue("-path")) {
				System.out.println("No Path Provided");
				return;
			}
			else {
				String input = argumentMap.getString("-path");
				Path path = Paths.get(input);
				try {
					if (Files.isDirectory(path)) {
						InvertedIndexBuilder.throughDirectory(path, index);
					} else {
						InvertedIndexBuilder.throughHTMLFile(path, input, index);

					}
				} catch (IOException e) {
					System.out.println("The path you provided could not be read through.");
					return;
				}
			}
			
		}

		if (argumentMap.hasFlag("-index")) {
			String output = argumentMap.getString("-index", "index.json");
			Path outputPath = Paths.get(output);
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
				HashSet<String[]> set = new HashSet<>();
				Path path = Paths.get(input);
				try {
					BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8);
					String line;
					String[] words;
					while ((line = br.readLine()) != null) {
						words = WordParser.parseWords(line);
						set.add(words);
					}
				} catch (IOException e) {
					System.out.println("The path you provided could not be read through.");
					return;
				}
				ArrayList<SearchResult> results;
				
				if (argumentMap.hasFlag("-exact")) {
					for (String[] elements : set) {
						Arrays.sort(elements);
						StringBuilder query = new StringBuilder();
						int i = elements.length;
						for (String element : elements) {
							query.append(element);
							if (i > 1){
								query.append(" ");
							}
							i--;
						}
						if (elements.length > 0) {
							results = index.exactSearch(elements);
							searchResults.put(query.toString(), results);
						}
						

					}

				}
				else {
					for (String[] elements : set) {
						Arrays.sort(elements);
						StringBuilder query = new StringBuilder();
						int i = elements.length;
						for (String element : elements) {
							query.append(element);
							if (i > 1){
								query.append(" ");
							}
							i--;
						}
						if (elements.length > 0) {
							results = index.partialSearch(elements);
							searchResults.put(query.toString(), results);
						}
					}
				}
				
			}
			
		}
		
		
		
		if (argumentMap.hasFlag("-results")) {
			String output = argumentMap.getString("-results", "results.json");
			Path path = Paths.get(output);
			try {
				JSONWriter.asNestedObject(searchResults, path);
			} catch (IOException e) {
				System.out.println("Could not write to file.");
			}
		}
		
	}

}






