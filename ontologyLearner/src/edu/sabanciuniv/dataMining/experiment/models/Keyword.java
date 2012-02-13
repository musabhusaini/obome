package edu.sabanciuniv.dataMining.experiment.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;

public class Keyword extends IdentifiableObject {
	private static final String KEYWORD_TABLE_NAME = "aspects";
	
	Aspect aspect;
	String label;
	
	public Keyword(Aspect aspect) {
		this(UUID.randomUUID(), aspect, "");
	}
	
	public Keyword(Aspect aspect, String label) {
		this(UUID.randomUUID(), aspect, label);
	}

	public Keyword(UUID uuid, Aspect aspect, String label) {
		super(uuid);
		
		this.setAspect(aspect);
		this.setLabel(label);
	}
	
	public Aspect getAspect() {
		return aspect;
	}

	public String getLabel() {
		return label;
	}

	public void setAspect(Aspect aspect) {
		this.aspect = aspect;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean deleteFromSql(Connection sqlConnection) throws SQLException {
		int paramIndex = 1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("DELETE FROM " + KEYWORD_TABLE_NAME + " WHERE uuid=?");
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getIdentifier()));
		boolean result = sqlStmt.executeUpdate() >= 1;
		sqlStmt.close();
		return result;
	}
	
	public boolean updateInSql(Connection sqlConnection) throws SQLException {
		int paramIndex = 1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("UPDATE " + KEYWORD_TABLE_NAME + " SET aspect=?, keyword=? WHERE uuid=?");
		sqlStmt.setString(paramIndex++, this.getLabel());
		sqlStmt.setString(paramIndex++, this.getAspect().getLabel());
		boolean result = sqlStmt.executeUpdate() >= 1;
		sqlStmt.close();
		return result;		
	}
	
	public boolean insertToSql(Connection sqlConnection) throws SQLException {
		int paramIndex = 1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("INSERT INTO " + KEYWORD_TABLE_NAME + "(aspect,keyword,setcover_uuid) VALUES(?,?,?)");
		sqlStmt.setString(paramIndex++, this.getAspect().getLabel());
		sqlStmt.setString(paramIndex++, this.getLabel());
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getAspect().getSetCover().getIdentifier()));
		boolean result = sqlStmt.executeUpdate() >= 1;
		sqlStmt.close();
		return result;
	}
	
	public static Iterable<Keyword> getAllKeywords(Aspect aspect, Connection sqlConnection) throws SQLException {
		int paramIndex=1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + KEYWORD_TABLE_NAME + " WHERE aspect=? AND setcover_uuid=?");
		sqlStmt.setString(paramIndex++, aspect.getLabel());
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(aspect.getSetCover().getIdentifier()));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		
		List<Keyword> keywords = new ArrayList<>();
		while (sqlResultSet.next()) {
			keywords.add(createFromSql(aspect, sqlResultSet));
		}
		
		return keywords;
	}

	public static Keyword createFromSql(UUID uuid, Connection sqlConnection) throws SQLException {
		Aspect aspect = Aspect.createFromSql(uuid, sqlConnection);
		return createFromSql(aspect, uuid, sqlConnection);
	}
	
	public static Keyword createFromSql(Aspect aspect, UUID uuid, Connection sqlConnection) throws SQLException {
		int paramIndex=1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + KEYWORD_TABLE_NAME + " WHERE uuid=?");
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(uuid));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		if (sqlResultSet.next()) {
			return createFromSql(aspect, sqlResultSet);
		}
		
		return null;
	}

	public static Keyword createFromSql(Aspect aspect, String label, Connection sqlConnection) throws SQLException {
		int paramIndex=1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + KEYWORD_TABLE_NAME + " WHERE aspect=? AND keyword=? AND setcover_uuid=?");
		sqlStmt.setString(paramIndex++, aspect.getLabel());
		sqlStmt.setString(paramIndex++, label);
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(aspect.getSetCover().getIdentifier()));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		
		if (sqlResultSet.next()) {
			return createFromSql(aspect, sqlResultSet);
		}
		
		return null;
	}
	
	public static Keyword createFromSql(Aspect aspect, ResultSet sqlResultSet) throws SQLException {
		if (sqlResultSet == null) {
			throw new IllegalArgumentException("Must provide a sql result set.");
		}

		Keyword keyword = new Keyword(aspect);
		keyword.setIdentifier(IdentifiableObject.createUuid(sqlResultSet.getBytes("uuid")));
		keyword.setAspect(aspect);
		keyword.setLabel(sqlResultSet.getString("keyword"));
		return keyword;
	}
}