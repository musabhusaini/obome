package edu.sabanciuniv.dataMining.data.factory.text;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import com.google.common.collect.Iterables;

/**
 * A factory for reviews based in a SQL database where a list of UUID's is provided.
 * @author Mus'ab Husaini
 */
public class SqlUuidListReviewFactory extends SqlReviewFactory {
	private Iterable<String> uuids;
	private Iterator<String> iterator;
	
	public SqlUuidListReviewFactory(Iterable<String> uuids) {
		super();
		
		if (uuids == null) {
			throw new InvalidParameterException("Must provide a list of id's.");
		}
		
		this.uuids = uuids;
		this.iterator = this.uuids.iterator();
	}

	public Iterable<String> getUuids() {
		return Iterables.unmodifiableIterable(this.uuids);
	}

	@Override
	protected PreparedStatement prepareStatement(Connection sqlConnection) throws SQLException {
		return sqlConnection.prepareStatement("SELECT [uuid], [content] FROM reviews WHERE [uuid]=?;");
	}

	@Override
	protected ResultSet executeNextQuery(PreparedStatement sqlStatement) throws SQLException {
		if (!this.iterator.hasNext()) {
			return null;
		}
		
		String uuid = this.iterator.next();
		if (uuid == null) {
			return null;
		}
		
		sqlStatement.setString(1, uuid);
		return sqlStatement.executeQuery();
	}
	
	@Override
	public boolean reset() {
		if (!super.reset()) {
			return false;
		}
		
		if (this.uuids != null) {
			this.iterator = this.uuids.iterator();
		}
		
		return true;
	}
}