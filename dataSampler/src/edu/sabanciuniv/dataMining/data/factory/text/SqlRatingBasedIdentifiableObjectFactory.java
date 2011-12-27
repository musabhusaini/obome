package edu.sabanciuniv.dataMining.data.factory.text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.sabanciuniv.dataMining.data.factory.SqlIdentifiableObjectFactory;

public class SqlRatingBasedIdentifiableObjectFactory extends SqlIdentifiableObjectFactory {
	private short rating;
	
	public SqlRatingBasedIdentifiableObjectFactory() {
		this(null);
	}
	
	public SqlRatingBasedIdentifiableObjectFactory(short rating) {
		this(null, rating);
	}
	
	public SqlRatingBasedIdentifiableObjectFactory(String tableName) {
		this(tableName, (short)0);
	}
	
	public SqlRatingBasedIdentifiableObjectFactory(String tableName, short rating) {
		super(tableName);
		this.rating = rating;
	}
	
	@Override
	protected PreparedStatement prepareStatement(Connection sqlConnection) throws SQLException {
		PreparedStatement stmt = sqlConnection.prepareStatement("SELECT [uuid], [content] FROM [" + this.getTableName() + "] WHERE [rating]=?;");
		stmt.setShort(1, this.rating);
		return stmt;
	}
}