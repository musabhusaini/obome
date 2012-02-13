package edu.sabanciuniv.dataMining.experiment.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

public class Aspect extends IdentifiableObject {
	
	private static final String ASPECTS_TABLE_NAME = "aspects";
	
	private String label;
	private SetCover setCover;

	private Aspect(SetCover setCover) {
		this(setCover, "");
	}
	
	public Aspect(SetCover setCover, String name) {
		this.setSetCover(setCover);
		this.setLabel(name);
	}
	
	public String getLabel() {
		return label;
	}

	public String setLabel(String name) {
		return this.label = name;
	}

	public SetCover getSetCover() {
		return setCover;
	}

	public SetCover setSetCover(SetCover setCover) {
		if (setCover == null) {
			throw new IllegalArgumentException("Must provide a set cover.");
		}
		return this.setCover = setCover;
	}

	public boolean deleteFromSql(Connection sqlConnection) throws SQLException {
		int paramIndex = 1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("DELETE FROM " + ASPECTS_TABLE_NAME + " WHERE aspect=? AND setcover_uuid=?");
		sqlStmt.setString(paramIndex++, this.getLabel());
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getSetCover().getIdentifier()));
		boolean result = sqlStmt.executeUpdate() >= 1;
		sqlStmt.close();
		return result;
	}
	
	public boolean updateInSql(Connection sqlConnection) throws SQLException {
		int paramIndex = 1;
		Aspect oldAspect = createFromSql(this.getIdentifier(), sqlConnection);
		if (oldAspect == null) {
			return false;
		}
		
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("UPDATE " + ASPECTS_TABLE_NAME + " SET aspect=? WHERE aspect=? AND setcover_uuid=?");
		sqlStmt.setString(paramIndex++, this.getLabel());
		sqlStmt.setString(paramIndex++, oldAspect.getLabel());
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getSetCover().getIdentifier()));
		boolean result = sqlStmt.executeUpdate() >= 1;
		sqlStmt.close();
		return result;		
	}
	
	public boolean insertToSql(Connection sqlConnection) throws SQLException {
		int paramIndex = 1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("INSERT INTO " + ASPECTS_TABLE_NAME + "(aspect,setcover_uuid) VALUES(?,?)");
		sqlStmt.setString(paramIndex++, this.getLabel());
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(this.getSetCover().getIdentifier()));
		boolean result = sqlStmt.executeUpdate() >= 1;
		sqlStmt.close();
		return result;
	}
	
	public static Iterable<Aspect> getAllAspects(SetCover setCover, Connection sqlConnection) throws SQLException {
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT DISTINCT aspect FROM " + ASPECTS_TABLE_NAME + " WHERE setcover_uuid=?");
		sqlStmt.setBytes(1, IdentifiableObject.getUuidBytes(setCover.getIdentifier()));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		
		List<Aspect> aspects = new ArrayList<>();
		while (sqlResultSet.next()) {
			String aspect = sqlResultSet.getString("aspect");
			int paramIndex=1;
			PreparedStatement sqlStmt1 = sqlConnection.prepareStatement("SELECT * FROM " + ASPECTS_TABLE_NAME + " WHERE aspect=? AND setcover_uuid=? LIMIT 1");
			sqlStmt1.setString(paramIndex++, aspect);
			sqlStmt1.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(setCover.getIdentifier()));
			ResultSet sqlResultSet1 = sqlStmt1.executeQuery();
			if (sqlResultSet1.next()) {
				aspects.add(createFromSql(setCover, sqlResultSet1));
			}
			sqlStmt1.close();
		}
		sqlStmt.close();
		return aspects;
	}

	public static Aspect createFromSql(UUID uuid, Connection sqlConnection) throws SQLException {
		int paramIndex=1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + ASPECTS_TABLE_NAME + " WHERE uuid=?");
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(uuid));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		
		if (sqlResultSet.next()) {
			SetCover setCover = SetCover.createFromSql(IdentifiableObject.createUuid(sqlResultSet.getBytes("setcover_uuid")), sqlConnection);
			return createFromSql(setCover, sqlResultSet);
		}
		
		return null;
	}

	public static Aspect createFromSql(SetCover setCover, String label, Connection sqlConnection) throws SQLException {
		int paramIndex=1;
		PreparedStatement sqlStmt = sqlConnection.prepareStatement("SELECT * FROM " + ASPECTS_TABLE_NAME + " WHERE aspect=? AND setcover_uuid=?");
		sqlStmt.setString(paramIndex++, label);
		sqlStmt.setBytes(paramIndex++, IdentifiableObject.getUuidBytes(setCover.getIdentifier()));
		ResultSet sqlResultSet = sqlStmt.executeQuery();
		
		if (sqlResultSet.next()) {
			return createFromSql(setCover, sqlResultSet);
		}
		
		return null;
	}
	
	public static Aspect createFromSql(SetCover setCover, ResultSet sqlResultSet) throws SQLException {
		if (sqlResultSet == null) {
			throw new IllegalArgumentException("Must provide a sql result set.");
		}

		Aspect aspect = new Aspect(setCover);
		aspect.setIdentifier(IdentifiableObject.createUuid(sqlResultSet.getBytes("uuid")));
		aspect.setLabel(sqlResultSet.getString("aspect"));
		return aspect;
	}
}