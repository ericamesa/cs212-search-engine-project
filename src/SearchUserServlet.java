import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

@SuppressWarnings("serial")
public class SearchUserServlet extends BaseServlet {
	private static Logger log = Log.getRootLogger();

	private InvertedIndex index;
	private WebPageSnippets snippets;

	public SearchUserServlet(InvertedIndex index, WebPageSnippets snippets) {
		super();
		this.index = index;
		this.snippets = snippets;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String user = getUsername(request);
		
		if (user == null) {
			response.sendRedirect("/");
		}
		
		prepareResponse("Search", response);
		
		printForm(request, response);
		
		PrintWriter out = response.getWriter();
		
		if (request.getParameter("words") != null) {
			ArrayList<SearchResult> results = null;
			String word = request.getParameter("words");
			word = StringEscapeUtils.escapeHtml4(word);
			Status status = dbhandler.addSearchHistory(user, word);
			
			if (status == Status.OK) {
				log.debug("Words added to search history.");
			}
			else {
				log.debug("Words could not be added to search history.");
			}
			
			String[] words = WordParser.parseWords(word);
			results = index.partialSearch(words);
			if (results.isEmpty()) {
				out.print("Sorry. Could not find a source with any of those words.");
			}
			else {
				for (SearchResult result : results) {
					out.printf("<a href=/save?url=%s>%s</a><br>", result.path, result.path);
					out.printf("%s </p>", snippets.get(result.path));
				}
			}
		}	
		
		out.println();
		out.println();
		out.println("<p><a href=\"/settings\" class=\"btn btn-primary\" role=\"button\">Settings</a></p>");
		
		finishResponse(response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request, response);
	}

	private static void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		
		out.printf("<form method=\"get\" action=\"%s\" class=\"form-inline\">%n", request.getServletPath());
		
		out.println("<p>");
		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<input type=\"text\" name=\"words\" class=\"form-control\" id=\"user\" placeholder=\"Search\">");
		out.println("\t</div>\n");
		
		out.println("\t<button type=\"submit\" name=\"search\" class=\"btn btn-primary\">Search</button></p>");
		out.println();
		out.println();
	}
}
