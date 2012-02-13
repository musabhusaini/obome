package edu.sabanciuniv.dataMining.experiment.models.setcover.io;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCoverReview;

public class SetCoverIO {
	private Connection sqlConnection;
	
	public SetCoverIO(Connection sqlConnection) {
		if (sqlConnection == null) {
			throw new IllegalArgumentException("Must provide a connection.");
		}
		this.sqlConnection = sqlConnection;
	}
	
	public Iterable<SetCover> getAll() {
		try {
			return SetCover.getAllSetCovers(this.sqlConnection);
		} catch(SQLException e) {
			throw new IllegalStateException("Could not communicate with the databsae.");
		}
	}
	
	public SetCover get(UUID uuid) {
		try {
			return SetCover.createFromSql(uuid, this.sqlConnection);
		} catch(SQLException e) {
			throw new IllegalStateException("Could not communicate with the database.");
		}
	}
	
	public SetCover get(String name) {
		try {
			return SetCover.createFromSql(name, this.sqlConnection);
		} catch(SQLException e) {
			throw new IllegalStateException("Could not communicate with the database.");
		}
	}
	
	public List<SetCoverReview> populateReviews(SetCover setCover) {
		try {
			return setCover.populateReviewsFromSql(this.sqlConnection);
		} catch(SQLException e) {
			throw new IllegalStateException("Could not communicate with the database.");
		}
	}
}