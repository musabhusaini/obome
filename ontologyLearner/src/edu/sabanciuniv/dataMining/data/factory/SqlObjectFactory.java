package edu.sabanciuniv.dataMining.data.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An abstract object factory that can create generic instances from a SQL database.
 * @author Mus'ab Husaini
 * @param <T> The type of objects that can be created.
 */
public abstract class SqlObjectFactory<T> extends AbstractObjectFactory<T> {
	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private ResultSet sqlResultSet;

	/**
	 * Creates an instance of {@link SqlObjectFactory}. 
	 */
	protected SqlObjectFactory() {
		this.reset();
	}
	
	/**
	 * Prepares a SQL statement from which necessary data will be retrieved to create the instance.
	 * @param sqlConnection The SQL connection to use.
	 * @return The {@link PreparedStatement} object representing the SQL statement.
	 * @throws SQLException Thrown when the statement cannot be created.
	 */
	protected abstract PreparedStatement prepareStatement(Connection sqlConnection) throws SQLException;
	
	/**
	 * Gets the prepared SQL statement lazily.
	 * @return The prepared statement.
	 * @throws SQLException Thrown when the statement cannot be retrieved.
	 */
	protected PreparedStatement getPreparedStatement() throws SQLException {
		if (this.sqlStatement == null) {
			this.sqlStatement = this.prepareStatement(this.sqlConnection);
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
			// Do nothing, it will return null naturally.
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
			
			this.sqlConnection = DriverManager.getConnection("jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=TripAdvisor;integratedSecurity=true");
			this.sqlResultSet = null;
			this.sqlStatement = null;
		} catch (SQLException e) {
			return false;
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
			// Do nothing.
		}
	}
}