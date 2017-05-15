import java.util.HashMap;

public class WebPageSnippets {
	private HashMap<String, String> snippets;
	private ReadWriteLock lock;
	
	public WebPageSnippets() {
		snippets = new HashMap<>();
		lock = new ReadWriteLock();
	}
	
	public void add(String webPage, String html) {
		String snippet;
		
		if (html.length() < 1000) {
			snippet = html;
		}
		else {
			snippet = html.substring(0, 1000) + " ...";
		}
		
		lock.lockReadWrite();
		snippets.put(webPage, snippet);
		lock.unlockReadWrite();
	}
	
	public String get(String webPage) {
		lock.lockReadOnly();
		try {
			return snippets.get(webPage);
		} finally {
			lock.unlockReadOnly();
		}
		
	}
	
	
}
