package edu.sabanciuniv.dataMining.data.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract object factory that can create generic instances from a SQL database.
 * @author Mus'ab Husaini
 * @param <T> The type of objects that can be created.
 */
public abstract class SqlObjectFactory<T> extends AbstractObjectFactory<T> {
	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private ResultSet sqlResultSet;
	private String sqlUrl = "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=TripAdvisor;integratedSecurity=true";
	private String sqlUserName = null;
	private String sqlPassword = null;

	/**
	 * Creates an instance of {@link SqlObjectFactory}.
	 */
	protected SqlObjectFactory() {
		// Do nothing.
	}
	
	/**
	 * Prepares a SQL statement from which necessary data will be retrieved to create the instance.
	 * @return The {@link PreparedStatement} object representing the SQL statement.
	 * @throws SQLException Thrown when the statement cannot be created.
	 */
	protected abstract PreparedStatement prepareStatement() throws SQLException;

	/**
	 * Gets the SQL connection.
	 * @return The SQL connection.
	 */
	protected Connection getSqlConnection() {
		return this.sqlConnection;
	}
	
	/**
	 * Gets the prepared SQL statement lazily.
	 * @return The prepared statement.
	 * @throws SQLException Thrown when the statement cannot be retrieved.
	 */
	protected PreparedStatement getPreparedStatement() throws SQLException {
		if (this.sqlStatement == null) {
			this.sqlStatement = this.prepareStatement();
		}
		return this.sqlStatement;
	}

	/**
	 * Sets any parameters required for the SQL statement.
	 * @param sqlStatement The {@link PreparedStatement} to set parameters on.
	 * @throws SQLException Thrown when the parameters cannot be set.
	 */
	protected void setStatementParameters(PreparedStatement sqlStatement) throws SQLException {
		// Do nothing.
	}

	/**
	 * Executes the next query operation.
	 * @param sqlStatement The {@link PreparedStatement} containing the SQL statement to execute.
	 * @return The {@link ResultSet} object containing the result of the query.
	 * @throws SQLException Thrown when the query fails.
	 */
	protected ResultSet executeNextQuery(PreparedStatement sqlStatement) throws SQLException {
		if (this.sqlResultSet == null) {
			this.setStatementParameters(sqlStatement);
			return sqlStatement.executeQuery();
		}
		
		return this.sqlResultSet;
	}

	/**
	 * Creates a new object from the {@link ResultSet} object containing the result of the last operation.
	 * @param sqlResultSet The result set.
	 * @return The new object.
	 * @throws SQLException Thrown when this object cannot be created from this result.
	 */
	protected abstract T createObject(ResultSet sqlResultSet) throws SQLException;

	/**
	 * Gets the URL used to connect to the SQL database.
	 * @return URL of the SQL database.
	 */
	public String getSqlUrl() {
		return this.sqlUrl;
	}
	
	/**
	 * Sets the URL used to connect to the SQL database.
	 * @param url URL of the SQL database.
	 */
	public void setSqlUrl(String url) {
		if (url == null || url.equals("")) {
			throw new IllegalArgumentException("URL cannot be null or empty.");
		}
		
		if (this.sqlUrl.equals(url)) {
			return;
		}
		
		this.sqlUrl = url;
	}
	
	/**
	 * Gets the user name used to connect to SQL.
	 * @return The user name.
	 */
	protected String getSqlUserName() {
		return this.sqlUserName;
	}
	
	/**
	 * Sets the user name for connecting to SQL.
	 * @param sqlUserName The user name.
	 */
	public void setSqlUserName(String sqlUserName) {
		this.sqlUserName = sqlUserName;
	}
	
	/**
	 * Gets the password for connecting to SQL.
	 * @return The password.
	 */
	protected String getSqlPassword() {
		return this.sqlPassword;
	}
	
	/**
	 * Sets the password for connecting to SQL.
	 * @param sqlPassword The password.
	 */
	public void setSqlPassword(String sqlPassword) {
		this.sqlPassword = sqlPassword;
	}
	
	/**
	 * Initializes the factory.
	 * @return Flag indicating whether the factory was initialized or not. 
	 */
	public boolean initialize() {
		try {
			if (this.sqlConnection != null && !this.sqlConnection.isClosed()) {
				return false;
			}
			
			this.reset();
		} catch (SQLException e) {
			Logger.getLogger(SqlObjectFactory.class.getName()).log(Level.WARNING, "Error querying SQL connection.", e);
			return false;
		}
		
		return true;
	}
	
	@Override
	public T create() {
		try {
			this.sqlResultSet = this.executeNextQuery(this.getPreparedStatement());
			if (this.sqlResultSet == null) {
				return null;
			}
	
			if (this.sqlResultSet.next()) {
				T obj = this.createObject(this.sqlResultSet);
				return obj;
			}
		} catch (SQLException e) {
			Logger.getLogger(SqlObjectFactory.class.getName()).log(Level.WARNING, "Error retrieving object from the database.", e);
		}
		
		return null;
	}
	
	@Override
	public boolean reset() {
		if (!super.reset()) {
			return false;
		}
		
		try {
			if (this.sqlConnection != null && !this.sqlConnection.isClosed()) {
				this.sqlConnection.close();
			}
			
			if (this.sqlUserName == null || this.sqlPassword == null) {
				this.sqlConnection = DriverManager.getConnection(this.sqlUrl);
			} else {
				this.sqlConnection = DriverManager.getConnection(this.sqlUrl, this.sqlUserName, this.sqlPassword);
			}
			
			this.sqlResultSet = null;
			this.sqlStatement = null;
		} catch (SQLException e) {
			Logger.getLogger(SqlObjectFactory.class.getName()).log(Level.WARNING, "Error communicating with the database.", e);
		}
		
		return true;
	}
	
	@Override
	public void close() {
		try {
			if (!this.sqlConnection.isClosed()) {
				this.sqlConnection.close();
			}
		} catch (SQLException e) {
			Logger.getLogger(SqlObjectFactory.class.getName()).log(Level.WARNING, "Error closing the database.", e);
		}
	}
}