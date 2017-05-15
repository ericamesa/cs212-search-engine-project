import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class VisitedResultsServlet extends BaseServlet { 

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String user = getUsername(request);
		String url = request.getParameter("url");
		
		if (user != null) {
			
			if (url != null) {
				dbhandler.addVisitedResults(user, url);
				response.sendRedirect(url);
				
			}
			else {
				if (request.getParameter("delete") != null) {
					
					Status status = dbhandler.deleteVisitedResults(user);
					
					if (status == Status.OK) {
						log.debug("Visited results were deleted successfully");
					}
					else {
						log.debug("Visited results could not be deleted");
					}
				}
				
				ResultSet results = dbhandler.getVisitedResults(user);
				
				prepareResponse("Visited Results", response);
				
				PrintWriter out = response.getWriter();
				
				if (results != null) {
					try {
						out.printf("<a href=%s>%s</a><br>", results.getString("link"), results.getString("link"));
						while (results.next()) {
							out.printf("<a href=%s>%s</a><br>", results.getString("link"), results.getString("link"));
						}
						out.println("<br>");
						
					} catch (SQLException e) {
						log.debug("Could not get visited results", e);
					}
				}
				else {
					out.println("There are no visited results to display \n");
				}
				
				
				out.println("<p><a href=\"/visitedresults?delete\" class=\"btn btn-primary\" role=\"button\">Clear Visited Results</a></p>");
				out.println("<p><a href=\"/settings\" class=\"btn btn-primary\" role=\"button\">Back</a></p>");
				
				finishResponse(response);
			}
			
		}
		else {
			log.debug("Could not find user");
			response.sendRedirect(url);
		}
	}
	
	
}
