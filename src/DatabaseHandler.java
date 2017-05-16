import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * Handles all database-related actions. Uses singleton design pattern.
 *
 * @see LoginServer
 */
public class DatabaseHandler {

	/** Use the Jetty logger for debugging (can easily replace with log4j2). */
	protected static final Logger log = Log.getLog();

	/** Makes sure only one database handler is instantiated. */
	private static DatabaseHandler singleton = new DatabaseHandler();

	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL =
			"SHOW TABLES LIKE 'login_users';";
	
	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SH_SQL =
			"SHOW TABLES LIKE 'search_history';";
	
	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_VR_SQL =
			"SHOW TABLES LIKE 'visited_results';";

	/** Used to create necessary tables for this example. */
	private static final String CREATE_SQL =
			"CREATE TABLE login_users (" +
			"userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
			"username VARCHAR(32) NOT NULL UNIQUE, " +
			"password CHAR(64) NOT NULL, " +
			"usersalt CHAR(32) NOT NULL);";

	/** Used to insert a new user into the database. */
	private static final String REGISTER_SQL =
			"INSERT INTO login_users (username, password, usersalt) " +
			"VALUES (?, ?, ?);";

	/** Used to determine if a username already exists. */
	private static final String USER_SQL =
			"SELECT username FROM login_users WHERE username = ?";

	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL =
			"SELECT usersalt FROM login_users WHERE username = ?";

	/** Used to authenticate a user. */
	private static final String AUTH_SQL =
			"SELECT username FROM login_users " +
			"WHERE username = ? AND password = ?";

	/** Used to remove a user from the database. */
	private static final String DELETE_SQL =
			"DELETE FROM login_users WHERE username = ?";
	
	private static final String CHANGE_PASS_SQL = 
			"UPDATE login_users " + 
			"SET password = ? " + 
			"WHERE username = ?;";
	
	private static final String AUTH_USERNAME_SQL =
			"SELECT username FROM login_users " +
			"WHERE username = ?";
	
	private static final String CREATE_SEARCH_HISTORY_SQL = 
			"CREATE TABLE search_history (" +
			"searchid INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, " + 
			"username VARCHAR(32) NOT NULL, " + 
			"words VARCHAR(64) NOT NULL);";
	
	private static final String ALTER_SEARCH_HISTORY_SQL = 
			"ALTER TABLE search_history " +
			"ADD FOREIGN KEY (username) " +
			"REFERENCES login_users (username);";
	
	private static final String INSERT_SEARCH_HISTORY_SQL = 
			"INSERT INTO search_history (username, words) " +
			"VALUES (?, ?);";
	
	private static final String DELETE_SEARCH_HISTORY_SQL = 
			"DELETE FROM search_history " + 
			"WHERE username = ?;";
	
	private static final String GET_SEARCH_HISTORY_SQL = 
			"SELECT words " +
			"FROM search_history " +
			"WHERE username = ?";
			
	private static final String CREATE_VISITED_RESULTS_SQL = 
			"CREATE TABLE visited_results (" +
			"visitedid INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, " + 
			"username VARCHAR(32) NOT NULL, " + 
			"link VARCHAR(64) NOT NULL);";
	
	private static final String ALTER_VISITED_RESULTS_SQL = 
			"ALTER TABLE visited_results " +
			"ADD FOREIGN KEY (username) " +
			"REFERENCES login_users (username);";
	
	private static final String INSERT_VISITED_RESULTS_SQL = 
			"INSERT INTO visited_results (username, link) " +
			"VALUES (?, ?);";
	
	private static final String DELETE_VISITED_RESULTS_SQL = 
			"DELETE FROM visited_results " + 
			"WHERE username = ?;";
	
	private static final String GET_VISITED_RESULTS_SQL = 
			"SELECT link " +
			"FROM visited_results " +
			"WHERE username = ?";

	/** Used to configure connection to database. */
	private DatabaseConnector db;

	/** Used to generate password hash salt for user. */
	private Random random;

	/**
	 * Initializes a database handler for the Login example. Private constructor
	 * forces all other classes to use singleton.
	 */
	private DatabaseHandler() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());

		try {
			db = new DatabaseConnector("database.properties");
			status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
		}
		catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		}
		catch (IOException e) {
			status = Status.MISSING_VALUES;
		}

		if (status != Status.OK) {
			log.warn(status.message());
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static DatabaseHandler getInstance() {
		return singleton;
	}

	/**
	 * Checks to see if a String is null or empty.
	 *
	 * @param text
	 *            String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text) {
		return (text == null) || text.trim().isEmpty();
	}

	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes
	 *            byte array to encode
	 * @param length
	 *            desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password
	 *            password to hash
	 * @param salt
	 *            salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		}
		catch (Exception ex) {
			log.debug("Unable to properly hash password.", ex);
		}

		return hashed;
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to create
	 * it.
	 *
	 * @return {@link Status.OK} if table exists or create is successful
	 */
	private Status setupTables() {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection(); Statement statement = connection.createStatement();) {
			if (!statement.executeQuery(TABLES_SQL).next()) {
				// Table missing, must create
				log.debug("Creating tables...");
				statement.executeUpdate(CREATE_SQL);

				// Check if create was successful
				if (!statement.executeQuery(TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				log.debug("login_users Tables found.");
				status = Status.OK;
			}
			
			if (!statement.executeQuery(TABLES_SH_SQL).next()) {
				// Table missing, must create
				log.debug("Creating tables...");
				statement.executeUpdate(CREATE_SEARCH_HISTORY_SQL);
				statement.executeUpdate(ALTER_SEARCH_HISTORY_SQL);

				// Check if create was successful
				if (!statement.executeQuery(TABLES_SH_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				log.debug("search_history Tables found.");
				status = Status.OK;
			}
			
			if (!statement.executeQuery(TABLES_VR_SQL).next()) {
				// Table missing, must create
				log.debug("Creating tables...");
				statement.executeUpdate(CREATE_VISITED_RESULTS_SQL);
				statement.executeUpdate(ALTER_VISITED_RESULTS_SQL);

				// Check if create was successful
				if (!statement.executeQuery(TABLES_VR_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				log.debug("visited_results Tables found.");
				status = Status.OK;
			}
		}
		catch (Exception ex) {
			status = Status.CREATE_FAILED;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection
	 *            active database connection
	 * @param user
	 *            username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user) {

		assert connection != null;
		assert user != null;

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(USER_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database.
	 *
	 * @see #duplicateUser(Connection, String)
	 * @param user
	 *            username to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status duplicateUser(String user) {
		Status status = Status.ERROR;

		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, user);
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            username of new user
	 * @param newpass
	 *            password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	private Status registerUser(Connection connection, String newuser, String newpass) {

		Status status = Status.ERROR;

		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);

		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newpass, usersalt);

		try (PreparedStatement statement = connection.prepareStatement(REGISTER_SQL);) {
			statement.setString(1, newuser);
			statement.setString(2, passhash);
			statement.setString(3, usersalt);
			statement.executeUpdate();

			status = Status.OK;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and salt into
	 * the database if the username does not already exist.
	 *
	 * @param newuser
	 *            username of new user
	 * @param newpass
	 *            password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Status status = Status.ERROR;
		log.debug("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if (isBlank(newuser) || isBlank(newpass)) {
			status = Status.INVALID_LOGIN;
			log.debug(status.toString());
			return status;
		}

		// try to connect to database and test for duplicate user
		try (Connection connection = db.getConnection();) {
			status = duplicateUser(connection, newuser);

			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = registerUser(connection, newuser, newpass);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection
	 *            active database connection
	 * @param user
	 *            which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException
	 *             if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;

		String salt = null;

		try (PreparedStatement statement = connection.prepareStatement(SALT_SQL);) {
			statement.setString(1, user);

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				salt = results.getString("usersalt");
			}
		}

		return salt;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Requires an active database connection.
	 *
	 * @param username
	 *            username to authenticate
	 * @param password
	 *            password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 * @throws SQLException
	 */
	private Status authenticateUser(Connection connection, String username, String password) throws SQLException {

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(AUTH_SQL);) {
			String usersalt = getSalt(connection, username);
			String passhash = getHash(password, usersalt);

			statement.setString(1, username);
			statement.setString(2, passhash);

			ResultSet results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Checks if the provided username and password match what is stored in the
	 * database. Must retrieve the salt and hash the password to do the
	 * comparison.
	 *
	 * @param username
	 *            username to authenticate
	 * @param password
	 *            password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 */
	public Status authenticateUser(String username, String password) {
		Status status = Status.ERROR;

		log.debug("Authenticating user " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, password);
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            username to remove
	 * @param password
	 *            password of user
	 * @return {@link Status.OK} if removal successful
	 */
	private Status removeUser(Connection connection, String username, String password) {
		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL);) {
			statement.setString(1, username);

			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username
	 *            username to remove
	 * @param password
	 *            password of user
	 * @return {@link Status.OK} if removal successful
	 */
	public Status removeUser(String username, String password) {
		Status status = Status.ERROR;

		log.debug("Removing user " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, password);
			System.out.println(status);

			if (status == Status.OK) {
				status = deleteSearchHistory(connection, username);
				System.out.println(status);
				if (status == Status.OK) {
					status = deleteVisitedResults(connection, username);
					System.out.println(status);
					if (status == Status.OK) {
						status = removeUser(connection, username, password);
						System.out.println(status);
					}
				}
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}

		return status;
	}
	
	/**
	 * Checks if the provided username is in the
	 * database. Requires an active database connection.
	 *
	 * @param username
	 *            username to authenticate
	 * @param password
	 *            password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 * @throws SQLException
	 */
	private Status authenticateUsername(Connection connection, String username) throws SQLException {

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(AUTH_USERNAME_SQL);) {

			statement.setString(1, username);

			ResultSet results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.INVALID_USER;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Checks if the provided username is in the
	 * database. Must retrieve the salt and hash the password to do the
	 * comparison.
	 *
	 * @param username
	 *            username to authenticate
	 * @param password
	 *            password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 */
	public Status authenticateUsername(String username) {
		Status status = Status.ERROR;

		log.debug("Authenticating username " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUsername(connection, username);
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}

		return status;
	}
	
	private Status changePassword(Connection connection, String username, String newPassword) throws SQLException {

		Status status = Status.ERROR;

		try (PreparedStatement statement = connection.prepareStatement(CHANGE_PASS_SQL);) {
			String passHash = getHash(newPassword, getSalt(connection, username));
			
			statement.setString(1, passHash);
			statement.setString(2, username);

			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.SQL_EXCEPTION;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}

		return status;
	}

	/**
	 * Checks if the provided username is in the
	 * database. Must retrieve the salt and hash the password to do the
	 * comparison.
	 *
	 * @param username
	 *            username to authenticate
	 * @param password
	 *            password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 */
	public Status changePassword(String username, String password, String newPassword) {
		Status status = Status.ERROR;

		log.debug("Changing password " + username + ".");
		
		if (isBlank(newPassword)) {
			status = Status.MISSING_VALUES;
			log.debug(status.toString());
			return status;
		}

		try (Connection connection = db.getConnection();) {
			status = authenticateUser(connection, username, password);
			
			if (status == Status.OK) {
				status = changePassword(connection, username, newPassword);
			}
			
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}

		return status;
	}
	
	
	private Status addSearchHistory(Connection connection, String username, String words) {
		Status status = Status.ERROR;
		
		try (PreparedStatement statement = connection.prepareStatement(INSERT_SEARCH_HISTORY_SQL);) {

			statement.setString(1, username);
			statement.setString(2, words);

			int count = statement.executeUpdate();
			
			if (count == 1) {
				status = Status.OK;
			}
			else {
				status = Status.SQL_EXCEPTION;
			}
			
			
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	public Status addSearchHistory(String username, String words) {
		Status status = Status.ERROR;
		
		log.debug("Adding search history " + username + ".");
		
		if (isBlank(words)) {
			return Status.MISSING_VALUES;
		}

		try (Connection connection = db.getConnection();) {
			status = authenticateUsername(connection, username);
			
			if (status == Status.OK) {
				status = addSearchHistory(connection, username, words);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	private ResultSet getSearchHistory(Connection connection, String username) {
		Status status = Status.ERROR;
		ResultSet results = null;
		
		try (PreparedStatement statement = connection.prepareStatement(GET_SEARCH_HISTORY_SQL);) {

			statement.setString(1, username);

			results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.MISSING_VALUES;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}
		
		return results;
	}
	
	public ResultSet getSearchHistory(String username) {
		Status status = Status.ERROR;
		ResultSet results = null;
		
		log.debug("Adding search history " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUsername(connection, username);
			
			if (status == Status.OK) {
				results = getSearchHistory(connection, username);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}
		return results;
	}
	
	private Status deleteSearchHistory(Connection connection, String username) {
		Status status = Status.ERROR;
		
		try (PreparedStatement statement = connection.prepareStatement(DELETE_SEARCH_HISTORY_SQL);) {

			statement.setString(1, username);

			statement.executeUpdate();
			status = Status.OK;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	public Status deleteSearchHistory(String username) {
		Status status = Status.ERROR;
		
		log.debug("Deleting search history " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUsername(connection, username);
			
			if (status == Status.OK) {
				status = deleteSearchHistory(connection, username);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	private Status addVisitedResults(Connection connection, String username, String link) {
		Status status = Status.ERROR;
		
		try (PreparedStatement statement = connection.prepareStatement(INSERT_VISITED_RESULTS_SQL);) {

			statement.setString(1, username);
			statement.setString(2, link);

			statement.executeUpdate();
			status = Status.OK;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	public Status addVisitedResults(String username, String link) {
		Status status = Status.ERROR;
		
		log.debug("Adding visited results " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUsername(connection, username);
			
			if (status == Status.OK) {
				status = addVisitedResults(connection, username, link);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	private ResultSet getVisitedResults(Connection connection, String username) {
		Status status = Status.ERROR;
		ResultSet results = null;
		
		try (PreparedStatement statement = connection.prepareStatement(GET_VISITED_RESULTS_SQL);) {

			statement.setString(1, username);

			results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.MISSING_VALUES;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}
		
		return results;
	}
	
	public ResultSet getVisitedResults(String username) {
		Status status = Status.ERROR;
		ResultSet results = null;
		
		log.debug("Adding search history " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUsername(connection, username);
			
			if (status == Status.OK) {
				results = getVisitedResults(connection, username);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}
		
		return results;
	}
	
	private Status deleteVisitedResults(Connection connection, String username) {
		Status status = Status.ERROR;
		
		try (PreparedStatement statement = connection.prepareStatement(DELETE_VISITED_RESULTS_SQL);) {

			statement.setString(1, username);

			statement.executeUpdate();
			status = Status.OK;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	public Status deleteVisitedResults(String username) {
		Status status = Status.ERROR;
		
		log.debug("Deleting visited results " + username + ".");

		try (Connection connection = db.getConnection();) {
			status = authenticateUsername(connection, username);
			
			if (status == Status.OK) {
				status = deleteVisitedResults(connection, username);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status.toString(), ex);
		}
		
		return status;
	}
	
	
}





