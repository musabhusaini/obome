package edu.sabanciuniv.dataMining.data.factory.text.opinion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.sabanciuniv.dataMining.data.factory.GenericSqlIdentifiableObjectFactory;
import edu.sabanciuniv.dataMining.data.text.opinion.RatedHotelReview;

public class HotelReviewFactory extends GenericSqlIdentifiableObjectFactory<RatedHotelReview> {
	private String role;
	
	public HotelReviewFactory() {
		this("training");
	}
	
	public HotelReviewFactory(String role) {
		super();
		
		if (role == null || role.equals("")) {
			throw new IllegalArgumentException("Must provide a role.");
		}
		
		this.role = role;
	}

	public String getRole() {
		return this.role;
	}
	
	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		int n = 10000;
		if ("training".equals(this.role)) {
			n *= 0.75;
		} else if ("testing".equals(this.role)) {
			n *= 0.25;
		}
		
		PreparedStatement statement = this.getSqlConnection().prepareStatement(
				"SELECT TOP " + n + " [uuid], [hotel_id], [author], [content], [date], [rating], [role] FROM [" + this.getTableName() +
				"] WHERE [role]=? AND [rating]>0;");
		
//		PreparedStatement statement = sqlConnection.prepareStatement(
//				"SELECT [uuid], [hotel_id], [author], [content], [date], [rating], [role] FROM [" + this.getTableName() +
//				"] WHERE [role]=? AND [rating]>0;");
		
		statement.setString(1, role);
		return statement;
	}
	
	@Override
	protected RatedHotelReview createObject(ResultSet sqlResultSet) throws SQLException {
		RatedHotelReview review = new RatedHotelReview();
		review.setIdentifier(sqlResultSet.getString("uuid"));
		review.setHotelId(sqlResultSet.getString("hotel_id"));
		review.setAuthor(sqlResultSet.getString("author"));
		review.setContent(sqlResultSet.getString("content"));
		review.setDate(sqlResultSet.getDate("date"));
		review.setRating(sqlResultSet.getShort("rating"));
		review.setDataRole(sqlResultSet.getString("role"));
		
		return review;
	}
}
