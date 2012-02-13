package edu.sabanciuniv.dataMining.experiment.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocument;

public class Review extends IdentifiableObject {
	public static final String REVIEWS_TABLE_NAME = "reviews";
	
	private String hotelId;
	private String author;
	private String content;
	private Date date;
	private short rating;
	
	public String getHotelId() {
		return hotelId;
	}
	
	public String setHotelId(String hotelId) {
		return this.hotelId = hotelId;
	}

	public String getAuthor() {
		return author;
	}

	public String setAuthor(String author) {
		return this.author = author;
	}

	public String getContent() {
		return content;
	}

	public String setContent(String content) {
		return this.content = content;
	}

	public TextDocument getTaggedContent() {
		return this.getTaggedContent(new TextDocumentOptions());
	}
	
	public TextDocument getTaggedContent(TextDocumentOptions options) {
		TextDocument doc = new TextDocument(options);
		doc.setIdentifier(this.getIdentifier());
		doc.setText(this.getContent());
		return doc;
	}

	public Date getDate() {
		return date;
	}

	public Date setDate(Date date) {
		return this.date = date;
	}

	public short getRating() {
		return rating;
	}

	public short setRating(short rating) {
		return this.rating = rating;
	}
		
	@Override
	public String toString() {
		return String.format("%s (for %s on %s): %s",
				this.getAuthor(), this.getHotelId().trim(), DateFormat.getInstance().format(this.getDate()), this.getContent());
	}
	
	public static Review createFromSql(UUID uuid, Connection sqlConnection) throws SQLException {
		if (sqlConnection == null) {
			throw new IllegalArgumentException("Must provide a sql connection.");
		}
		if (uuid == null) {
			throw new IllegalArgumentException("Must provide a uuid.");
		}

		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + REVIEWS_TABLE_NAME + " WHERE uuid=?");
		sqlStmt.setBytes(1, IdentifiableObject.getUuidBytes(uuid));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		if (!sqlResultSet.next()) {
			return null;
		}
		return createFromSql(sqlResultSet);
	}
	
	public static Review createFromSql(ResultSet sqlResultSet) throws SQLException {
		if (sqlResultSet == null) {
			throw new IllegalArgumentException("Must provide a sql result set.");
		}

		Review review = new Review();
		review.setHotelId(sqlResultSet.getString("hotel_id"));
		review.setAuthor(sqlResultSet.getString("author"));
		review.setContent(sqlResultSet.getString("content"));
		review.setDate(sqlResultSet.getDate("date"));
		review.setRating(sqlResultSet.getShort("rating"));
		return review;
	}
}
