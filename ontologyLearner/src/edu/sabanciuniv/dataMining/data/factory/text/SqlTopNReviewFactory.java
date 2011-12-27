package edu.sabanciuniv.dataMining.data.factory.text;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A factory for top N reviews in a SQL database.
 * @author Mus'ab Husaini
 */
public class SqlTopNReviewFactory extends SqlReviewFactory {
	private int n;
	
	public SqlTopNReviewFactory(int n) throws SQLException {
		super();
		
		if (n < 1) {
			throw new InvalidParameterException("Must provide a non-zero positive number of records to get.");
		}
		
		this.n = n;
	}

	/**
	 * @return the n
	 */
	public int getN() {
		return this.n;
	}

	@Override
	protected PreparedStatement prepareStatement(Connection sqlConnection) throws SQLException {
		return sqlConnection.prepareStatement("SELECT TOP " + n + " [uuid], [content] FROM reviews;");
	}
}