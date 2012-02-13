package edu.sabanciuniv.dataMining.experiment.models.setcover;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.Review;

public class SetCoverReview extends IdentifiableObject {
	public static final String SETCOVER_REVIEWS_TABLE_NAME = "setcover_reviews";
	
	private UUID reviewUuid;
	private SetCover setCover;
	private int memberCount;
	private boolean seen;
	
	public SetCoverReview() {
		this(new SetCover());
	}
	
	public SetCoverReview(SetCover setCover) {
		this.setSetCover(setCover);
	}
	
	public UUID getReviewUuid() {
		return reviewUuid;
	}
	
	public UUID setReviewUuid(UUID reviewUuid) {
		return this.reviewUuid = reviewUuid;
	}
	
	public SetCover getSetCover() {
		return setCover;
	}

	public SetCover setSetCover(SetCover setCover) {
		return this.setCover = setCover;
	}

	public int getMemberCount() {
		return memberCount;
	}
	
	public int setMemberCount(int memberCount) {
		return this.memberCount = memberCount;
	}
	
	public boolean isSeen() {
		return seen;
	}
	
	public boolean setSeen(boolean seen) {
		return this.seen = seen;
	}
	
	public Review getReview(Connection sqlConnection) throws SQLException {
		return Review.createFromSql(this.getReviewUuid(), sqlConnection);
	}

	public boolean insertToSql(Connection sqlConnection) throws SQLException {
		int paramIndex=1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("INSERT INTO " + SETCOVER_REVIEWS_TABLE_NAME +
				"(uuid, review_uuid, setcover_uuid, member_count, seen) VALUE(?,?,?,?,?)");
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getIdentifier()));
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getReviewUuid()));
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getSetCover().getIdentifier()));
		sqlStmt.setInt(paramIndex++, this.getMemberCount());
		sqlStmt.setBoolean(paramIndex++, this.isSeen());
		sqlStmt.executeUpdate();
		sqlStmt.close();
		
		return true;
	}
	
	public static Iterable<SetCoverReview> getSetCoverReviews(Connection sqlConnection) throws SQLException {
		return getSetCoverReviews(sqlConnection, null, null);
	}
	
	public static Iterable<SetCoverReview> getSetCoverReviews(Connection sqlConnection, Boolean seen) throws SQLException {
		return getSetCoverReviews(sqlConnection, null, seen);
	}
	
	public static Iterable<SetCoverReview> getSetCoverReviews(Connection sqlConnection, SetCover setCover) throws SQLException {
		return getSetCoverReviews(sqlConnection, setCover, null);
	}
	
	public static Iterable<SetCoverReview> getSetCoverReviews(Connection sqlConnection, SetCover setCover, Boolean seen) throws SQLException {
		if (sqlConnection == null) {
			throw new IllegalArgumentException("Must provide a sql connection.");
		}
		
		PreparedStatement sqlStmt;
		String sql = "SELECT * FROM " + SETCOVER_REVIEWS_TABLE_NAME;
		if (setCover != null && setCover.getIdentifier() != null) {
			sql += " WHERE setcover_uuid=?";
			
			if (seen != null) {
				sql += " AND seen=?";
				sqlStmt = sqlConnection.prepareStatement(sql);
				sqlStmt.setBoolean(2, seen);
			} else {
				sqlStmt = sqlConnection.prepareStatement(sql);
			}
			
			sqlStmt.setBytes(1, IdentifiableObject.getUuidBytes(setCover.getIdentifier()));
		} else if (seen != null) {
			sql += " WHERE seen=?";
			sqlStmt = sqlConnection.prepareStatement(sql);
			sqlStmt.setBoolean(1, seen);
		} else {
			sqlStmt = sqlConnection.prepareStatement(sql);
		}

		List<SetCoverReview> setCoverReviews = new ArrayList<>();
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		while (sqlResultSet.next()) {
			SetCoverReview scReview = createFromSql(sqlResultSet);
			
			if (setCover != null && setCover.getIdentifier() != null) {
				scReview.setSetCover(setCover);
			}
			setCoverReviews.add(scReview);
		}
		
		sqlStmt.close();
		return setCoverReviews;
	}
	
	public static SetCoverReview createFromSql(UUID uuid, Connection sqlConnection) throws SQLException {
		if (sqlConnection == null) {
			throw new IllegalArgumentException("Must provide a sql connection.");
		}
		if (uuid == null) {
			throw new IllegalArgumentException("Must provide a uuid.");
		}

		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + SETCOVER_REVIEWS_TABLE_NAME + " WHERE uuid=?");
		sqlStmt.setBytes(1, IdentifiableObject.getUuidBytes(uuid));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		if (!sqlResultSet.next()) {
			sqlStmt.close();
			return null;
		}
		
		SetCoverReview scReview = createFromSql(sqlResultSet);
		sqlStmt.close();
		return scReview;
	}
	
	public static SetCoverReview createFromSql(ResultSet sqlResultSet) throws SQLException {
		if (sqlResultSet == null) {
			throw new IllegalArgumentException("Must provide a sql result set.");
		}

		SetCoverReview setCoverReview = new SetCoverReview();
		setCoverReview.setIdentifier(IdentifiableObject.createUuid(sqlResultSet.getBytes("uuid")));
		setCoverReview.setReviewUuid(IdentifiableObject.createUuid(sqlResultSet.getBytes("review_uuid")));
		setCoverReview.setMemberCount(sqlResultSet.getInt("member_count"));
		setCoverReview.setSeen(sqlResultSet.getBoolean("seen"));
		
		return setCoverReview;
	}
}