import java.io.IOException;
import java.nio.file.Path;

public interface SearchIndexInterface {
	
	public void addFromFile(Path path, Boolean exact) throws IOException;
	
	public void toJSON(Path path) throws IOException;
	
}
