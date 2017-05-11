import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// More XSS Prevention:
// https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet

// Apache Comments:
// http://commons.apache.org/proper/commons-lang/download_lang.cgi

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {
	private static final String TITLE = "Search";
	private static Logger log = Log.getRootLogger();

	private InvertedIndex index;

	public SearchServlet(InvertedIndex index) {
		super();
		this.index = index;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		log.info("SearchServlet ID " + this.hashCode() + " handling GET request.");

		
		PrintWriter out = response.getWriter();
		
		
		
		out.printf("<html>%n%n");
		out.printf("<head><title>%s</title></head>%n", TITLE);
		out.printf("<body>%n");

		out.printf("<h1>Search</h1>%n%n");

//		// Keep in mind multiple threads may access at once
//		synchronized (results) {
//			for (SearchResult result : results) {
//				out.printf("<p>%s</p>%n%n", result.toString());
//			}
//		}
		
		printForm(request, response);
		
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
					out.printf("<a href=%s>%s</a><br>", result.path, result.path);
				}
			}
		}		
		

		out.printf("%n</body>%n");
		out.printf("</html>%n");

		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request, response);
	}

	private static void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		out.printf("<form method=\"get\" action=\"%s\">%n", request.getServletPath());
		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap>Search:</td>%n");
		out.printf("\t<td>%n");
		out.printf("\t\t<input type=\"text\" name=\"words\" maxlength=\"50\" size=\"20\">%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n");
//		out.printf("<tr>%n");
//		out.printf("\t<td nowrap>Message:</td>%n");
//		out.printf("\t<td>%n");
//		out.printf("\t\t<input type=\"text\" name=\"message\" maxlength=\"100\" size=\"60\">%n");
//		out.printf("\t</td>%n");
//		out.printf("</tr>%n");
		out.printf("</table>%n");
		out.printf("<p><input type=\"submit\" value=\"Search\"></p>\n%n");
		out.printf("</form>\n%n");
	}

	private static String getDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
}