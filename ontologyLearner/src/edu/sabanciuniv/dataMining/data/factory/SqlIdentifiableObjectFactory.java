package edu.sabanciuniv.dataMining.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;

/**
 * An object factory for creating objects of type {@link IdentifiableObject} backed by a SQL database.
 * @author Mus'ab Husaini
 */
public class SqlIdentifiableObjectFactory extends GenericSqlIdentifiableObjectFactory<IdentifiableObject> {

	/**
	 * Creates a new instance of {@link SqlIdentifiableObjectFactory}.
	 */
	public SqlIdentifiableObjectFactory() {
		this(null);
	}
	
	/**
	 * Creates a new instance of {@link SqlIdentifiableObjectFactory}.
	 * @param tableName The table from which to get the instances.
	 */
	public SqlIdentifiableObjectFactory(String tableName) {
		super(tableName);
	}
	
	@Override
	protected IdentifiableObject createObject(ResultSet sqlRs) throws SQLException {
		return new IdentifiableObject(sqlRs.getString("uuid"));
	}
}