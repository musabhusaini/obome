package edu.sabanciuniv.dataMining.data.factory.text;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.sabanciuniv.dataMining.data.factory.SqlIdentifiableObjectFactory;

public class SqlRatingBasedIdentifiableObjectFactory extends SqlIdentifiableObjectFactory {
	private short rating;

	public SqlRatingBasedIdentifiableObjectFactory() {
		this((short)0);
	}
	
	
	public SqlRatingBasedIdentifiableObjectFactory(short rating) {
		this.rating = rating;
	}
	
	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		PreparedStatement stmt = this.getSqlConnection().prepareStatement("SELECT [uuid], [content] FROM [" + this.getTableName() + "] WHERE [rating]=?;");
		stmt.setShort(1, this.rating);
		return stmt;
	}
}