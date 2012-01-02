package edu.sabanciuniv.dataMining.data.factory.text;

import java.security.InvalidParameterException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

import com.google.common.collect.Iterables;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;

/**
 * A factory for reviews based in a SQL database where a list of UUID's is provided.
 * @author Mus'ab Husaini
 */
public class SqlUuidListReviewFactory extends SqlReviewFactory {
	private Iterable<UUID> uuids;
	private Iterator<UUID> iterator;
	
	public SqlUuidListReviewFactory(Iterable<UUID> uuids) {
		super();
		
		if (uuids == null) {
			throw new InvalidParameterException("Must provide a list of id's.");
		}
		
		this.uuids = uuids;
		this.iterator = this.uuids.iterator();
	}

	public Iterable<UUID> getUuids() {
		return Iterables.unmodifiableIterable(this.uuids);
	}

	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		return this.getSqlConnection().prepareStatement("SELECT uuid, content FROM " + this.getTableName() + " WHERE uuid=?;");
	}

	@Override
	protected ResultSet executeNextQuery(PreparedStatement sqlStatement) throws SQLException {
		if (!this.iterator.hasNext()) {
			return null;
		}
		
		UUID uuid = this.iterator.next();
		if (uuid == null) {
			return null;
		}
		
		sqlStatement.setBytes(1, IdentifiableObject.getUuidBytes(uuid));
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