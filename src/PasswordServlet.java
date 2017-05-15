import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class PasswordServlet extends BaseServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String user = getUsername(request);
		
		if (user != null) {
			
			PrintWriter out = response.getWriter();
			
			prepareResponse("Change Password", response);
			
			out.println("<p>Please enter your old password and the new password you wish to change it to.</p>");
			
			printForm(out, request);

			finishResponse(response);
			
		}
		else {
			response.sendRedirect("/welcome");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String user = getUsername(request);
		String oldpass = request.getParameter("oldpass");
		String newpass = request.getParameter("newpass");
		
		if (oldpass != null && newpass != null) {
			Status status = dbhandler.changePassword(user, oldpass, newpass);
			if (status == Status.OK) {
				response.sendRedirect("/settings?done");
			}
			else {
				response.sendRedirect("/settings?error=" + status.message());
			}
		}
	}
	
	private void printForm(PrintWriter out, HttpServletRequest request) {
		assert out != null;

		out.println();
		out.printf("<form method=\"post\" action=\"%s\" class=\"form-inline\">%n", request.getServletPath());

		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<label for=\"user\">Old Password:</label>");
		out.println("\t\t<input type=\"password\" name=\"oldpass\" class=\"form-control\" id=\"user\" placeholder=\"Old Password\">");
		out.println("\t</div>\n");

		out.println("\t<div class=\"form-group\">");
		out.println("\t\t<label for=\"pass\">New Password:</label>");
		out.println("\t\t<input type=\"password\" name=\"newpass\" class=\"form-control\" id=\"pass\" placeholder=\"New Password\">");
		out.println("\t</div>\n");

		out.println("\t<button type=\"submit\" class=\"btn btn-primary\">Submit</button>\n");
		out.println("</form>");
		out.println("<br/>\n");
	}
	
}
