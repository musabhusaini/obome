package edu.sabanciuniv.dataMining.data.writer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.sampling.Category;

public class SqlDataWriter extends DataWriter {
	private String tableName;
	
	protected Connection sqlConn;
	protected PreparedStatement sqlStmt;
	
	public SqlDataWriter() throws SQLException {
		this("reviews");
	}
	
	public SqlDataWriter(String tableName) throws SQLException {
		if (!this.reset()) {
			throw new SQLException("Could not create connection.");
		}
		
		this.setTableName(tableName);
	}
	
	protected PreparedStatement prepareStatement() throws SQLException {
		return this.sqlConn.prepareStatement("UPDATE [" + this.getTableName() + "] SET [role]=? WHERE [uuid]=?;");
	}
	
	protected PreparedStatement getSqlStatement() throws SQLException {
		if (this.sqlStmt == null) {
			this.sqlStmt = this.prepareStatement();
		}
		
		return this.sqlStmt;
	}

	protected void setStatementParameters(IdentifiableObject obj, Category category) throws SQLException {
		this.getSqlStatement().setString(1, category.getName());
		this.getSqlStatement().setString(2, obj.getIdentifier().toString());
	}
	
	@Override
	public boolean write(IdentifiableObject obj, Category category) {
		try {
			this.setStatementParameters(obj, category);
			this.getSqlStatement().executeUpdate();
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return this.tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		if (tableName == null || tableName.equals("")) {
			throw new IllegalArgumentException("Table name must be non-empty");
		}

		this.tableName = tableName;
	}
	
	public boolean reset() {
		try {
			this.sqlConn = DriverManager.getConnection("jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=TripAdvisor;integratedSecurity=true");
			this.sqlStmt = null;
		} catch (SQLException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public void close() {
		try {
			if (!this.sqlConn.isClosed()) {
				this.sqlConn.close();
			}
		} catch (SQLException e) {
			// Do nothing.
		}
	}
}