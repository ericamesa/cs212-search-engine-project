import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SettingsServlet extends BaseServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String user = getUsername(request);
		
		if (user != null) {
			
			prepareResponse("Settings", response);

			PrintWriter out = response.getWriter();
			
			if (request.getParameter("error") != null) {
				out.println("<p class=\"alert alert-danger\"> Error! " + request.getParameter("error") + " Could not change password </p>");
			}
			
			if (request.getParameter("done") != null) {
				out.println("<p class=\"alert alert-success\">Successfully changed password.</p>");
			}
			
			if (request.getParameter("delete") != null) {
				out.println("<p class=\"alert alert-danger\"> Could not delete account! </p>");
			}
			
			
			out.println("<p>Hello " + user + "!</p>");
			out.println("<p><a href=\"/searchhistory\" class=\"btn btn-primary\" role=\"button\">Search History</a></p>");
			out.println("<p><a href=\"/visitedresults\" class=\"btn btn-primary\" role=\"button\">Visited Results</a></p>");
			out.println("<p><a href=\"/changepass\" class=\"btn btn-primary\" role=\"button\">Change Password</a></p>");
			out.println("<p><a href=\"/login?logout\" class=\"btn btn-primary\" role=\"button\">Logout</a></p>");
			out.println("<p><a href=\"/delete\" class=\"btn btn-primary\" role=\"button\">Delete Account</a></p>");
			out.println("<p><a href=\"/welcome\" class=\"btn btn-primary\" role=\"button\">Back</a></p>");

			finishResponse(response);
		}
		else {
			response.sendRedirect("/welcome");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request, response);
	}
}
