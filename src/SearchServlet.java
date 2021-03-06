import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

// More XSS Prevention:
// https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet

// Apache Comments:
// http://commons.apache.org/proper/commons-lang/download_lang.cgi

@SuppressWarnings("serial")
public class SearchServlet extends BaseServlet {

	private InvertedIndex index;
	private WebPageSnippets snippets;

	public SearchServlet(InvertedIndex index, WebPageSnippets snippets) {
		super();
		this.index = index;
		this.snippets = snippets;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (request.getParameter("login") != null) {
			response.sendRedirect("/login");
		}
		
		prepareResponse("Search", response);
		
		printForm(request, response);
		
		PrintWriter out = response.getWriter();
		
		if (request.getParameter("words") != null) {
			ArrayList<SearchResult> results = null;
			String word = request.getParameter("words");
			word = StringEscapeUtils.escapeHtml4(word);
			String[] words = WordParser.parseWords(word);
			results = index.partialSearch(words);
			if (results.isEmpty()) {
				out.print("Sorry. Could not find a source with any of those words.");
			}
			else {
				for (SearchResult result : results) {
					out.printf("<p><a href=%s>%s</a><br>", result.path, result.path);
					out.printf("%s </p>", snippets.get(result.path));
				}
			}
		}		
		
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
		out.println("<p>\t<button type=\"submit\" name=\"login\" class=\"btn btn-primary\">Login</button></p>");
	}
}