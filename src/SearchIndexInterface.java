import java.io.IOException;
import java.nio.file.Path;

public interface SearchIndexInterface {
	
	/**
	 * Adds words from given file to index and an ArrayList of its SearchResults.
	 *
	 * @param path
	 *            path to read words from
	 * @param invertedIndex
	 *            index to search through
	 * @param exact
	 *            true if searching for exact matches, false to search for partial matches
	 */
	public void addFromFile(Path path, Boolean exact) throws IOException;
	
	/**
	 * Writes index to specified path in JSON format.
	 *
	 * @param path
	 *            path to write to
	 */
	public void toJSON(Path path) throws IOException;
	
}
