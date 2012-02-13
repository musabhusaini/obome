package edu.sabanciuniv.dataMining.experiment.models.setcover;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;

public class SetCover extends IdentifiableObject {
	
	public static final String SETCOVER_TABLE_NAME = "setcovers";
	
	private String name;
	private int coverOffset;
	private int coverSize;
	private List<SetCoverReview> reviews;
	private Date timestamp;
	
	public SetCover() {
		this("Setcover_" + new Random().nextInt());
	}
	
	public SetCover(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Must provide a name for the setcover.");
		}
		this.setName(name);
		this.reviews = new ArrayList<>();
		this.timestamp = new Date();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String setName(String name) {
		return this.name = name;
	}

	public int getCoverOffset() {
		return coverOffset;
	}

	public int setCoverOffset(int coverageOffset) {
		return this.coverOffset = coverageOffset;
	}

	public int getCoverSize() {
		return coverSize;
	}

	public int setCoverSize(int coverageSize) {
		return this.coverSize = coverageSize;
	}
	
	public Date getTimestamp() {
		return this.timestamp;
	}

	public List<SetCoverReview> getReviews() {
		return this.reviews;
	}
	
	public List<SetCoverReview> setReviews(Iterable<SetCoverReview> reviews) {
		return this.reviews = Lists.newArrayList(reviews);
	}
	
	public List<SetCoverReview> populateReviewsFromSql(Connection sqlConnection) throws SQLException {
		return this.setReviews(SetCoverReview.getSetCoverReviews(sqlConnection, this));
	}

	public boolean insertToSql(Connection sqlConnection) throws SQLException {
		int paramIndex = 1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("INSERT INTO " + SETCOVER_TABLE_NAME +
				"(uuid, name, cover_offset, cover_size) VALUES(?,?,?,?)");
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getIdentifier()));
		sqlStmt.setString(paramIndex++, this.getName());
		sqlStmt.setInt(paramIndex++, this.getCoverOffset());
		sqlStmt.setInt(paramIndex++, this.getCoverSize());
		sqlStmt.executeUpdate();
		sqlStmt.close();
		
		for (SetCoverReview scReview : this.getReviews()) {
			scReview.insertToSql(sqlConnection);
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return this.name + "(" + this.coverOffset + ", " + this.coverSize +  ")";
	}
	
	public static Iterable<SetCover> getAllSetCovers(Connection sqlConnection) throws SQLException {
		if (sqlConnection == null) {
			throw new IllegalArgumentException("Must provide a sql connection.");
		}

		List<SetCover> setCovers = new ArrayList<>();
		
		ResultSet sqlResultSet = sqlConnection.createStatement().executeQuery("SELECT * FROM " + SETCOVER_TABLE_NAME);
		while (sqlResultSet.next()) {
			setCovers.add(createFromSql(sqlResultSet));
		}
		sqlResultSet.close();
		return setCovers;
	}
	
	public static SetCover createFromSql(UUID uuid, Connection sqlConnection) throws SQLException {
		if (sqlConnection == null) {
			throw new IllegalArgumentException("Must provide a sql connection.");
		}
		if (uuid == null) {
			throw new IllegalArgumentException("Must provide a uuid.");
		}
		
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + SETCOVER_TABLE_NAME + " WHERE uuid=?");
		sqlStmt.setBytes(1, IdentifiableObject.getUuidBytes(uuid));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		if (!sqlResultSet.next()) {
			sqlStmt.close();
			return null;
		}
		
		SetCover setCover = createFromSql(sqlResultSet);
		sqlStmt.close();
		return setCover;
	}
	
	public static SetCover createFromSql(String name, Connection sqlConnection) throws SQLException {
		if (sqlConnection == null) {
			throw new IllegalArgumentException("Must provide a sql connection.");
		}
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("Must provide a name.");
		}
		
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + SETCOVER_TABLE_NAME + " WHERE name=?");
		sqlStmt.setString(1, name);
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		if (!sqlResultSet.next()) {
			sqlStmt.close();
			return null;
		}
		
		SetCover setCover = createFromSql(sqlResultSet);
		sqlStmt.close();
		return setCover;
	}
	
	public static SetCover createFromSql(ResultSet sqlResultSet) throws SQLException {
		if (sqlResultSet == null) {
			throw new IllegalArgumentException("Must provide a sql result set.");
		}
		
		SetCover setCover = new SetCover();
		setCover.setIdentifier(IdentifiableObject.createUuid(sqlResultSet.getBytes("uuid")));
		setCover.setName(sqlResultSet.getString("name"));
		setCover.setCoverOffset(sqlResultSet.getInt("cover_offset"));
		setCover.setCoverSize(sqlResultSet.getInt("cover_size"));
		setCover.timestamp = sqlResultSet.getTimestamp("timestamp");
		return setCover;
	}
}