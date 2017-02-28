import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
* Driver class which takes in args and adds words from -path argument to an InvertedIndex and outputs to -index
* argument
*/

public class Driver {

	public static void main(String[] args) {

		ArgumentMap argumentMap = new ArgumentMap(args);
		InvertedIndex index = new InvertedIndex();

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
		else {
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("index.json"), StandardCharsets.UTF_8)) {
				writer.write("");
				writer.close();
			} catch (IOException e) {
				System.out.println("Could not write to file.");
			}
			return;
		}

		if (argumentMap.hasFlag("-index")) {
			String output;
			if (!argumentMap.hasValue("-index")){
				output = "index.json";
			}
			else {
				output = argumentMap.getString("-index");
			}
			Path outputPath = Paths.get(output);
			try {
				index.toJSON(outputPath);
			} catch (IOException e) {
				System.out.println("Could not write to file.");
			}
			
		}
		
	}

}