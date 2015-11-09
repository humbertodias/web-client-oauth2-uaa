package oauth2.client;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.id.*;
import com.nimbusds.openid.connect.sdk.*;

/**
 * OpenID Connect login start page.
 */
@WebServlet(urlPatterns = { "/", "/login", "/signin" })
public class SignIn extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private URL composeAuthzRequestURL() throws Exception {

		// Set the requested response_type (code, token and / or
		// id_token):
		// Use CODE for authorisation code flow
		// Use TOKEN for implicit flow
		ResponseType rt = new ResponseType("code");

		// Set the requested scope of access
		Scope scope = new Scope(Configuration.SCOPE);

		// Identify the client app by its registered ID
		ClientID clientID = new ClientID(Configuration.CLIENT_ID);

		// Set the redirect URL after successful OIDC login /
		// authorisation. This URL is typically registered in advance
		// with the OIDC server
		URL redirectURI = new URL(Configuration.CALLBACK_URI);

		// Generate random state value. It's used to link the
		// authorisation response back to the original request, also to
		// prevent replay attacks
		State state = new State();

		// Generate random nonce value.
		Nonce nonce = new Nonce();

		// Create the actual OIDC authorisation request object
		AuthenticationRequest authRequest = new AuthenticationRequest(redirectURI.toURI(), rt, scope, clientID,
				redirectURI.toURI(), state, nonce);

		// Get the resulting URL query string with the authorisation
		// request encoded into it
		String queryString = authRequest.toQueryString();

		// Set the base URL of the OIDC server authorisation endpoint
		URL authzEndpointURL = new URL(Configuration.AUTHORIZATION_URI);

		// Construct and output the final OIDC authorisation URL for
		// redirect
		URL authzURL = new URL(authzEndpointURL + "?" + queryString);

		return authzURL;
	}

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		PrintWriter out = resp.getWriter();

		out.println("<html>");
		out.println("<head><title>Nimbus OpenID Connect Test Client</title></head>");

		out.println("<body>");
		out.println("<h1>Login with OpenID Connect</h1>");

		URL authzURL;

		try {
			authzURL = composeAuthzRequestURL();

		} catch (Exception e) {

			out.println("<p>Couldn't compose OIDC authorisation request URL: " + e.getMessage() + "</p>");
			return;
		}

		// Redirect the user to the URL below for OIDC login /
		// authorisation, then get the response at the redirectURI
		// set above
		out.print("<a href=\"" + authzURL + "\">Click to login</a>");

		out.println("</body>");
		out.println("</html>");
	}
}