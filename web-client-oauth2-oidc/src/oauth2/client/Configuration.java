package oauth2.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

	public static String CLIENT_ID;
	public static String CALLBACK_URI;
	public static String AUTHORIZATION_URI;

	public static String CLIENT_SECRET;
	public static String TOKEN_URI;
	public static String USER_INFO_URI;
	public static String TOKEN_KEY_URI;
	public static String SCOPE;
	public static String RESOURCE_NAME;

	static {
		loadConfiguration("uaa");
	}

	/**
	 * Carrega as propriedades a partir do arquivo properties definido na
	 * variável de ambiente CRONOS_HOME ou o cronos.properties do contexto da
	 * aplicação.
	 */
	public static void loadConfiguration(String resourceName) {
		try {

			InputStream resource = Configuration.class.getResourceAsStream("/" + resourceName + ".properties");

			Properties properties = new Properties();
			properties.load(resource);

			// 1)
			CLIENT_ID = properties.getProperty("CLIENT_ID");
			CALLBACK_URI = properties.getProperty("CALLBACK_URI");
			AUTHORIZATION_URI = properties.getProperty("AUTHORIZATION_URI");
			SCOPE = properties.getProperty("SCOPE");

			// 2)
			CLIENT_SECRET = properties.getProperty("CLIENT_SECRET");
			TOKEN_URI = properties.getProperty("TOKEN_URI");

			// 3) Adicional
			USER_INFO_URI = properties.getProperty("USER_INFO_URI");
			TOKEN_KEY_URI = properties.getProperty("TOKEN_KEY_URI");

			RESOURCE_NAME = resourceName;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
