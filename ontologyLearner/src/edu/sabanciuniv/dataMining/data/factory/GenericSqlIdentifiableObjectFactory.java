package edu.sabanciuniv.dataMining.data.factory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;

/**
 * An object factory for creating objects that derive from {@link IdentifiableObject} backed by a SQL database.
 * @author Mus'ab Husaini
 * @param <T> Types of objects that can be created; must extend {@link IdentifiableObject}.
 */
public abstract class GenericSqlIdentifiableObjectFactory<T extends IdentifiableObject> extends SqlObjectFactory<T> {
	private boolean suppressConsoleOutput;
	private String tableName = "reviews";

	/**
	 * Creates a new instance of {@link SqlAbstractIdentifiableObjectFactory}.
	 */
	public GenericSqlIdentifiableObjectFactory() {
		// Do nothing.
	}

	/**
	 * Prepares the SQL statement that will be executed. 
	 */
	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		return this.getSqlConnection().prepareStatement("SELECT uuid FROM " + this.getTableName() + ";");
	}
	
	/**
	 * Gets the table name.
	 * @return The table name.
	 */
	public String getTableName() {
		return this.tableName;
	}
	
	/**
	 * Sets the table name.
	 * @param tableName The table name.
	 */
	public void setTableName(String tableName) {
		if (tableName == null || tableName.equals("")) {
			throw new IllegalArgumentException("Table name must be non-empty");
		}
		
		this.tableName = tableName;
	}
	
	/**
	 * Returns a string representation of the provided object.
	 * @param obj The object.
	 * @return A string representation.
	 */
	protected String objToString(T obj) {
		return obj.toString();
	}

	@Override
	public T create() {
		T obj = super.create();
		if (obj != null && !this.suppressConsoleOutput) {
			System.out.println(this.setCount(this.getCount() + 1) + ". " + obj.getIdentifier());
			System.out.println(this.objToString(obj));
		}
		return obj;
	}
	
	/**
	 * Gets a flag indicating if console output should be suppressed.
	 * @return A flag indicating if console output should be suppressed.
	 */
	public boolean isSuppressConsoleOutput() {
		return suppressConsoleOutput;
	}

	/**
	 * Gets a flag indicating if console output should be suppressed.
	 * @param suppressConsoleOutput A flag indicating if console output should be suppressed.
	 */
	public void setSuppressConsoleOutput(boolean suppressConsoleOutput) {
		this.suppressConsoleOutput = suppressConsoleOutput;
	}
}