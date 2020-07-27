package com.tisawesomeness.minecord.database;

import com.tisawesomeness.minecord.config.serial.Config;
import com.tisawesomeness.minecord.util.RequestUtils;

import com.google.common.base.Splitter;
import lombok.Cleanup;
import lombok.Getter;
import org.sqlite.SQLiteDataSource;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The access point for the Minecord database backend.
 */
public class Database {

	private static final int VERSION = 1;

	@Getter private final DatabaseCache cache;
	private final DataSource source;

	/**
	 * Sets up the database connection pool and creates the caches.
	 * @param config The config file that provides the path to the database and sets up the cache
	 * @throws SQLException when either the initial read or creating a missing table fails
	 */
	public Database(Config config) throws SQLException {

		String url = "jdbc:sqlite:" + config.getDatabase().getPath();
		SQLiteDataSource ds = new SQLiteConnectionPoolDataSource();
		ds.setUrl(url);
		ds.setEncoding("UTF-8");
		source = ds;

		cache = new DatabaseCache(this, config);

		// For now, only creating the database is needed
		// In the future, every database change increments the version
		// and this code will run the correct upgrade scripts
		int version = getVersion();
		if (version == 0) {
			runScript("init.sql");
		} else if (version > VERSION) {
			String err = String.format("The database version is %s but the bot expects %s or lower!", version, VERSION);
			throw new IllegalStateException(err);
		}

		System.out.println("Database connected.");
		
	}

	/**
	 * Asks the connection pool for a connection.
	 * <br>The connection should be closed after the calling method is done with it.
	 * @return A connection to this database
	 * @throws SQLException If a database access error occurs
	 */
	public Connection getConnect() throws SQLException {
		return source.getConnection();
	}

	/**
	 * Gets the current Minecord database version, used to determine which upgrade scripts to use.
	 * @return A positive integer version, or 0 if the version is not tracked.
	 */
	private int getVersion() throws SQLException {
		@Cleanup Connection connect = getConnect();
		@Cleanup Statement st = connect.createStatement();
		// Query returns 1 result if table exists, 0 results if table does not exist
		ResultSet tableRS = st.executeQuery(
				"SELECT name FROM sqlite_master WHERE type='table' AND name='minecord';"
		);
		// The first next() call returns false if there are 0 results
		if (!tableRS.next()) {
			return 0;
		}
		ResultSet versionRS = st.executeQuery(
				"SELECT version FROM minecord;"
		);
		// Minecord table has only one row
		versionRS.next();
		return versionRS.getInt("version");
	}

	/**
	 * Runs a .sql script from resources.
	 * <br>This assumes that each statement in the script is separated by semicolons.
	 * @param resourceName The filename of the script in the resources folder.
	 * @throws SQLException If there is an error executing the script.
	 */
	private void runScript(String resourceName) throws SQLException {
		String initScript = RequestUtils.loadResource(resourceName);
		@Cleanup Connection connect = getConnect();
		@Cleanup Statement statement = connect.createStatement();
		for (String query : Splitter.on(";").omitEmptyStrings().split(initScript)) {
			statement.executeUpdate(query);
		}
	}

}
