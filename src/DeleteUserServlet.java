import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DeleteUserServlet extends BaseServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String user = getUsername(request);
		
		if (user != null) {
			
			if (request.getParameter("password") != null) {
				
				prepareResponse("Please enter your password to delete your account.", response);

				printForm(request, response);

				finishResponse(response);
				
				
				assert request.getParameter("password") == null || request.getParameter("password").equals("true");
				
				if (!request.getParameter("password").equals("")) {
					Status status = dbhandler.authenticateUser(user, request.getParameter("password"));
					if (status == Status.OK) {
						dbhandler.removeUser(user, request.getParameter("password"));
						response.sendRedirect("/login");
					}
					else {
						prepareResponse(status.message(), response);
					}
				}
				
				
			}
			else {
				prepareResponse("Are you sure you want to delete your account?", response);

				PrintWriter out = response.getWriter();
				out.println("<p>Hello " + user + "!</p>");
				out.println("<p><a href=\"/delete?password\" class=\"btn btn-primary\" role=\"button\">Yes</a></p>");
				out.println("<p><a href=\"/settings\" class=\"btn btn-primary\" role=\"button\">No</a></p>");

				finishResponse(response);
			}
			
		}
		else {
			response.sendRedirect("/settings?delete");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String user = getUsername(request);
		String pass = request.getParameter("pass");
		
		if (user != null && pass != null) {
			Status status = dbhandler.removeUser(user, pass);
			if (status == Status.OK) {
				response.sendRedirect("/login?delete");
			}
			else {
				response.sendRedirect("/settings?delete");
			}
		}
		else {
			response.sendRedirect("/settings?delete");
		}
		
	}
	
	private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		out.println();
		out.printf("<form method=\"post\" action=\"%s\" class=\"form-inline\">%n", request.getServletPath());

		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<label for=\"pass\">Password:</label>");
		out.println("\t\t<input type=\"password\" name=\"pass\" class=\"form-control\" id=\"pass\" placeholder=\"Password\">");
		out.println("\t</div>\n");
		
		out.println("\t<button type=\"submit\" class=\"btn btn-primary\">Submit</button>\n");
		out.println("</form>");
		out.println("<br/>\n");
	}
}
