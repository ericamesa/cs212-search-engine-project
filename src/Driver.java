import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
* Driver class which takes in args and adds words from -path argument to an InvertedIndex and outputs to -index
* argument. Parses through -query argument, if -exact flag is found searches for exact matches in the inverted index 
* else searches for partial matches and outputs to -results argument.
*/

public class Driver {

	/**
	 * Adds words from -path argument from args to an InvertedIndex and outputs to -index argument from args. Parses through 
	 * -query argument and adds either exact or partial matches, depending on whether a -exact flag was provided, to a SearchIndex
	 * and outputs to -results arguement. 
	 * 
	 * @param args
	 * 			  command line arguments
	 */
	public static void main(String[] args) {

		ArgumentMap argumentMap = new ArgumentMap(args);
		InvertedIndex index = new InvertedIndex();
		SearchIndex searchIndex = new SearchIndex();

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
				Path path = Paths.get(input);
				
				try {
					if (argumentMap.hasFlag("-exact")) {
						searchIndex.addFromFile(path, index, true);
					}
					else {
						searchIndex.addFromFile(path, index, false);
					}
					
				} catch (IOException e) {
					System.out.println("The path you provided could not be read through.");
					return;
				}
				
			}
			
		}
		
		if (argumentMap.hasFlag("-results")) {
			String output = argumentMap.getString("-results", "results.json");
			Path path = Paths.get(output);
			try {
				searchIndex.toJSON(path);
			} catch (IOException e) {
				System.out.println("Could not write to file.");
			}
		}
		
	}

}






