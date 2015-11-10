package oauth2.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.OIDCAccessTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;

import net.minidev.json.JSONObject;

/**
 * OpenID Connect login callback target.
 */
@WebServlet("/callback")
public class Callback extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		// *** *** *** Process the authorisation response *** *** *** //

		// Get the URL query string which contains the encoded
		// authorisation response
		String queryString = req.getQueryString();

		PrintWriter out = resp.getWriter();

		out.println("<html>");
		out.println("<head><title>Nimbus OpenID Connect Test Client</title></head>");

		out.println("<body>");

		out.println("<pre>");

		out.println("URL query string with encoded authorization response: " + queryString + "\n\n");

		if (queryString == null || queryString.trim().isEmpty()) {

			out.println("Missing URL query string");
			return;
		}

		// Parse the authentication response
		AuthenticationResponse authResponse;

		try {
			authResponse = AuthenticationResponseParser
					.parse(new URL(Configuration.AUTHORIZATION_URI + "?" + queryString).toURI());
		} catch (Exception e) {

			out.println("Couldn't parse OpenID Connect authentication response: " + e.getMessage());
			return;
		}

		if (authResponse instanceof AuthenticationErrorResponse) {

			// The authorisation response indicates an error, print
			// it and return immediately
			AuthenticationErrorResponse authzError = (AuthenticationErrorResponse) authResponse;
			out.println("Authentication error: " + authzError.getErrorObject());
			return;
		}

		// Authentication success, retrieve the authorisation code
		AuthenticationSuccessResponse authzSuccess = (AuthenticationSuccessResponse) authResponse;

		out.println("Authorization success:");
		out.println("\tAuthorization code: " + authzSuccess.getAuthorizationCode());
		out.println("\tState: " + authzSuccess.getState() + "\n\n");

		AuthorizationCode code = authzSuccess.getAuthorizationCode();

		if (code == null) {
			out.println("Missing authorization code");
			return;
		}

		// *** *** *** Make a token endpoint request *** *** *** //

		// Compose an access token request, authenticating the client
		// app and exchanging the authorisation code for an ID token
		// and access token
		URL tokenEndpointURL = new URL(Configuration.TOKEN_URI);

		out.println("Sending access token request to " + tokenEndpointURL + "\n\n");

		// We authenticate with "client secret basic"
		ClientID clientID = new ClientID(Configuration.CLIENT_ID);
		Secret clientSecret = new Secret(Configuration.CLIENT_SECRET);
		ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

		Scope scope = new Scope();

		HTTPRequest httpRequest;

		try {

			AuthorizationCodeGrant authCodeGrant = new AuthorizationCodeGrant(code,
					new URI(Configuration.CALLBACK_URI));

			TokenRequest accessTokenRequest = new TokenRequest(tokenEndpointURL.toURI(), clientAuth, authCodeGrant,
					scope);

			httpRequest = accessTokenRequest.toHTTPRequest();

		} catch (SerializeException | URISyntaxException e) {

			out.println("Couldn't create access token request: " + e.getMessage());
			return;
		}

		HTTPResponse httpResponse;

		try {
			httpResponse = httpRequest.send();

		} catch (IOException e) {

			// The URL request failed
			out.println("Couldn't send HTTP request to token endpoint: " + e.getMessage());
			return;
		}

		TokenResponse tokenResponse;

		try {
			tokenResponse = OIDCTokenResponseParser.parse(httpResponse);

		} catch (Exception e) {
			out.println("Couldn't parse token response: " + e.getMessage());
			return;
		}

		if (tokenResponse instanceof TokenErrorResponse) {

			// The token response indicates an error, print it out
			// and return immediately
			TokenErrorResponse tokenError = (TokenErrorResponse) tokenResponse;
			out.println("Token error: " + tokenError.getErrorObject());
			return;
		}

		OIDCAccessTokenResponse tokenSuccess = (OIDCAccessTokenResponse) tokenResponse;
		BearerAccessToken accessToken = (BearerAccessToken) tokenSuccess.getAccessToken();
		RefreshToken refreshToken = tokenSuccess.getRefreshToken();
		SignedJWT idToken = (SignedJWT) tokenSuccess.getIDToken();

		out.println("Token response:");

		out.println("\tAccess token: " + accessToken.toJSONObject().toString());
		out.println("\tRefresh token: " + refreshToken);
		out.println("\n\n");

		out.println("<hr/>");

		// tokenKeyEndpointURL
		URL tokenKeyEndpointURL = new URL(Configuration.TOKEN_KEY_URI);
		JSONObject jwt = null;
		String token_key = null;
		try {

			UserInfoRequest tokenKeyRequest = new UserInfoRequest(tokenKeyEndpointURL.toURI(), accessToken);
			httpRequest = tokenKeyRequest.toHTTPRequest();

			// Aplica autenticação básica
			ClientSecretBasic basic = new ClientSecretBasic(clientID, clientSecret);
			basic.applyTo(httpRequest);

			httpResponse = httpRequest.send();
			out.println(tokenKeyEndpointURL.toString());
			jwt = httpResponse.getContentAsJSONObject();
			token_key = jwt.get("value").toString();
			out.println("<pre>" + jwt.toJSONString() + "</pre>");

		} catch (Exception e) {
			out.println(e.getMessage());
		}
		// tokenKeyEndpointURL

		// *** *** *** Process ID token which contains user auth information ***
		// *** *** //
		if (idToken != null) {

			out.println("ID token [raw]: " + idToken.getParsedString());

			out.println("jwt.header: " + idToken.getHeader());
			out.println("jwt.payload: " + idToken.getPayload());
			out.println("jwt.signature: " + idToken.getSignature());

			out.println("jwt.keyId: " + idToken.getHeader().getKeyID());
			out.println("jwt.algorithm: " + idToken.getHeader().getAlgorithm());

			byte[] sharedSecret = new byte[32];
			for (int i = 0; i < token_key.length(); i++)
				sharedSecret[i] = (byte) token_key.charAt(i);

			JWSVerifier verifier = new MACVerifier(sharedSecret);
			try {
				boolean valid = idToken.verify(verifier);
				out.println("valid: " + valid);
			} catch (Exception e) {
				out.println("Couldn't process ID token: " + e.getMessage());
			}

		}
		out.println("</pre>");

		out.println("<hr/>");

		// tokenKeyEndpointURL

		out.println("<hr/>");

		// *** *** *** Make a UserInfo endpoint request *** *** *** //

		// Note: The PayPal IdP uses an older OIDC draft version and
		// is at present not compatible with the Nimbus OIDC SDK so
		// we cannot use its helper call. We can however make a direct
		// call and simply display the raw data.

		URL userinfoEndpointURL = new URL(Configuration.USER_INFO_URI);

		try {
			// Append the access token to form actual request
			UserInfoRequest userInfoRequest = new UserInfoRequest(userinfoEndpointURL.toURI(), accessToken);

			httpResponse = userInfoRequest.toHTTPRequest().send();

			out.println(userinfoEndpointURL.toString());
			out.println("<pre>" + httpResponse.getContent() + "</pre>");

		} catch (Exception e) {

			// The URL request failed
			out.println("Couldn't send HTTP request to UserInfo endpoint: " + e.getMessage());
			return;
		}

		out.println("</body>");
		out.println("</html>");

	}
}