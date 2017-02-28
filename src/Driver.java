import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// TODO Javadoc

public class Driver {

	public static void main(String[] args) {

		ArgumentMap argumentMap = new ArgumentMap(args);

		InvertedIndex index = new InvertedIndex();

		String defaultPath = "index.json";
		String input = argumentMap.getString("-path", "NoFlag", "NoValue");
		Path path = Paths.get(input);
		String output = argumentMap.getString("-index", "NoFlag", defaultPath);

		if (input.equals("NoValue")) {
			System.out.println("No Path Provided");
		} else if (input.equals("NoFlag")) {
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(defaultPath), StandardCharsets.UTF_8)) {
				writer.write("");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (Files.isDirectory(path)) {
					InvertedIndexBuilder.throughDirectory(path, index);
				} else {
					InvertedIndexBuilder.throughHTMLFile(path, input, index);

				}

				if (!output.equals("")) {
					Path outputPath = Paths.get(output);
					index.toJSON(outputPath);
				}
			} catch (IOException e) {
				e.printStackTrace(); // TODO User-friendly exception output, no stack traces
			}

		}

		/* TODO 
		if (argumentMap.hasFlag("-path")) {
			if (argumentMap.hasValue("-path")) {
			
				String path = argumentMap.getString("-path");
			
			}
		}

		if (argumentMap.hasFlag("-index")) {
			String output = argumentMap.getString("-index", "NoFlag", defaultPath);
			Path outputPath = Paths.get(output);
			index.toJSON(outputPath);
		}
		*/
		
	}

}