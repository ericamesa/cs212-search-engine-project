import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SearchHistoryServlet extends BaseServlet { 

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String user = getUsername(request);
		
		
		if (user != null) {

			if (request.getParameter("delete") != null) {

				Status status = dbhandler.deleteSearchHistory(user);

				if (status == Status.OK) {
					log.debug("Search history were deleted successfully");
				}
				else {
					log.debug("Search history could not be deleted");
				}
			}
			
			ResultSet results = dbhandler.getSearchHistory(user);

			prepareResponse("Search History", response);

			PrintWriter out = response.getWriter();
			
			if (results != null) {
				try {
					out.printf("<a href=/search?words=%s>%s</a><br>", results.getString("words"), results.getString("words"));
					while (results.next()) {
						out.printf("<a href=/search?words=%s>%s</a><br>", results.getString("words"), results.getString("words"));
					}
					out.println("<br>");
					
				} catch (SQLException e) {
					log.debug("Could not get visited results", e);
				}
			}
			else {
				out.println("There is no search history to display \n");
			}

			out.println("<p><a href=\"/searchhistory?delete\" class=\"btn btn-primary\" role=\"button\">Clear Search History</a></p>");
			out.println("<p><a href=\"/settings\" class=\"btn btn-primary\" role=\"button\">Back</a></p>");

			finishResponse(response);
		}
		else {
			log.debug("Could not find user");
			response.sendRedirect("/login");
		}
	}

}
